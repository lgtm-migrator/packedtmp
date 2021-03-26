/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeMap;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.Wirelet;
import packed.internal.application.BuildSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.component.InternalWirelet.SetComponentNameWirelet;
import packed.internal.component.source.ClassSourceSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;
import packed.internal.util.ThrowableUtil;

/** A setup class for a component. Exposed to end-users as {@link ComponentConfigurationContext}. */
public class ComponentSetup extends OpenTreeNode<ComponentSetup> {

    /** Wirelets that was specified when creating the component. */
    @Nullable
    public final WireletWrapper wirelets;

    /** The modifiers of this component. */
    protected final int modifiers;

    /* *************** Setup **************** */

    /** The constant pool this component is a part of. */
    public final ConstantPoolSetup pool;

    /** The realm this component belongs to. */
    public final RealmSetup realm;

    /** The container this component is a member of. A container is a member of it self. */
    public final ContainerSetup container;

    /** The build this component is part of. */
    public final BuildSetup build;

    /**************** See how much of this we can get rid of. *****************/

    boolean isClosed = false;

    int nameState;

    Consumer<? super Component> onWire;

    static final int NAME_INITIALIZED_WITH_WIRELET = 1 << 18; // set atomically with DONE
    static final int NAME_SET = 1 << 17; // set atomically with ABNORMAL
    static final int NAME_GET = 1 << 16; // true if joiner waiting
    static final int NAME_GET_PATH = 1 << 15; // true if joiner waiting
    static final int NAME_CHILD_GOT_PATH = 1 << 14; // true if joiner waiting

    static final int NAME_GETSET_MASK = NAME_SET + NAME_GET + NAME_GET_PATH + NAME_CHILD_GOT_PATH;

    /**
     * Creates a new instance of this class
     * 
     * @param parent
     *            the parent of the component
     */
    public ComponentSetup(BuildSetup build, RealmSetup realm, PackedComponentDriver<?> driver, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(parent);
        this.build = requireNonNull(build);
        // Setup Realm
        this.realm = realm;
        ComponentSetup previous = realm.current;
        if (previous != null) {
            previous.fixCurrent();
        }
        realm.current = this;

        // Various
        if (parent == null) {
            this.modifiers = build.modifiers | driver.modifiers;
            this.pool = build.constantPool;
            this.wirelets = WireletWrapper.forApplication(build.application.driver, driver, wirelets);
        } else {
            this.modifiers = driver.modifiers;
            this.pool = driver.modifiers().hasRuntime() ? new ConstantPoolSetup() : parent.pool;
            this.wirelets = WireletWrapper.forComponent(driver, wirelets);
            this.onWire = parent.onWire;
        }

        // May initialize the component's name, onWire, ect
        if (this.wirelets != null) {
            for (Wirelet w : this.wirelets.wirelets) {
                if (w instanceof InternalWirelet bw) {
                    bw.firstPass(this);
                }
            }
        }

        // Setup container
        this.container = this instanceof ContainerSetup container ? container : parent.container;

        // Setup Runtime
        if (modifiers().hasRuntime()) {
            pool.reserve(); // reserve a slot to an instance of PackedApplicationRuntime
        }
    }

    /**
     * Constructor used by {@link ExtensionSetup}.
     * 
     * @param container
     *            the extension's container (parent)
     * @param extensionModel
     *            a model of the extension
     */
    protected ComponentSetup(ContainerSetup container, RealmSetup realm) {
        super(container);
        this.build = container.build;
        this.container = container;
        this.modifiers = PackedComponentModifierSet.I_EXTENSION;
        this.realm = realm;
        this.realm.current = this; // IDK Den er jo ikke runtime...
        this.pool = container.pool;
        this.wirelets = null; // cannot specify wirelets to extension
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public Component adaptor() {
        return new ComponentAdaptor(this);
    }

    protected void addAttributes(DefaultAttributeMap dam) {}

    AttributeMap attributes() {
        // Det er ikke super vigtigt at den her er hurtig paa configurations tidspunktet...
        // Maaske er det simpelthen et view...
        // Hvor vi lazily fx calculere EntrySet (og gemmer i et felt)
        DefaultAttributeMap dam = new DefaultAttributeMap();
        addAttributes(dam);
        return dam;
    }

    public BuildSetup build() {
        return build;
    }

    public void checkConfigurable() {
        if (isClosed) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    public void checkCurrent() {
        if (realm.current != this) {
            throw new IllegalStateException("This operation must be called immediately after wiring of the component");
        }
    }

    void fixCurrent() {
        if (name == null) {
            setName(null);
        }
        if (onWire != null) {
            onWire.accept(adaptor());
        }
        // run onWiret
        // finalize name
    }

    /**
     * Returns the container this component is a part of. Or null if this component is the top level container.
     * 
     * @return the container this component is a part of
     */
    @Nullable
    public ContainerSetup getMemberOfContainer() {
        return container;
    }

    public String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
    }

    @Nullable
    public ComponentSetup getParent() {
        return treeParent;
    }

    public Component link(Assembly<?> assembly, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        PackedComponentDriver<?> driver = PackedComponentDriver.getDriver(assembly);

        // If this component is an extension, we add it to the extension's container instead of the extension
        // itself, as the extension component is not retained at runtime
        ComponentSetup parent = this instanceof ExtensionSetup ? treeParent : this;

        // Create a new component and a new realm
        ComponentSetup component = driver.newComponent(build, new RealmSetup(assembly), parent, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = driver.toConfiguration((ComponentConfigurationContext) component);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Closes the the linked realm, no further configuration of it is possible after Assembly::build has been invoked
        component.realmClose();

        return new ComponentAdaptor(this);
    }

    public PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }

    public NamespacePath path() {
        int anyPathMask = NAME_GET_PATH + NAME_CHILD_GOT_PATH;
        if ((nameState & anyPathMask) != 0) {
            ComponentSetup p = treeParent;
            while (p != null && ((p.nameState & anyPathMask) == 0)) {
                p.nameState = (p.nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
            }
        }
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
        return PackedTreePath.of(this); // show we weak intern them????
    }

    /**
     * Closes the realm that this belongs component belongs to.
     * <p>
     * This method must only be called on a realms root component (we do not check explicitly this)
     * 
     * @see ComponentSetup#link(Assembly, Wirelet...)
     */
    public void realmClose() {
        if (realm.current != null) {
            realm.current.fixCurrent();
        }
        realmClose0(realm);
    }

    /**
     * Called whenever a realm is closed on the top component in the realm.
     * 
     * @param realm
     *            the realm that was closed.
     */
    private void realmClose0(RealmSetup realm) {
        // Closes all components in the same realm depth first
        for (ComponentSetup component = treeFirstChild; component != null; component = component.treeNextSibling) {
            // child components with a different realm, is either:
            // in an another user realm that already been closed
            // in an extension realm that is closed in container.close
            if (component.realm == realm) {
                component.realmClose0(realm);
            }
        }
        // If this component represents container close the container
        if (this instanceof ContainerSetup container) {
            container.close(pool);
        }
        isClosed = true;
    }

    public void setName(String name) {
        // First lets check the name is valid
        SetComponentNameWirelet.checkName(name);
        int s = nameState;

        checkConfigurable();
        checkCurrent();

        // maybe assume s==0

        if ((s & NAME_SET) != 0) {
            throw new IllegalStateException("#setName(String) can only be called once");
        }

        if ((s & NAME_GET) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #getName() has been invoked");
        }

        if ((s & NAME_GET_PATH) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #path() has been invoked");
        }

        if ((s & NAME_CHILD_GOT_PATH) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #path() has been invoked on a child component");
        }

        // Maaske kan vi godt saette to gange...
        nameState |= NAME_SET;

        if ((s & NAME_INITIALIZED_WITH_WIRELET) != 0) {
            return;// We never set override a name set by a wirelet
        }

        setName0(name);
    }

    protected void setName0(String newName) {
        String n = newName;
        if (newName == null) {
            if (nameState == NAME_INITIALIZED_WITH_WIRELET) {
                n = name;
            }
        }

        boolean isFree = false;

        if (n == null) {
            ClassSourceSetup src = this instanceof SourceComponentSetup bcs ? bcs.source : null;
            if (src != null) {
                n = src.model.simpleName();
            } else if (this instanceof ContainerSetup container) {
                // I think try and move some of this to ComponentNameWirelet
                @Nullable
                Class<?> source = realm.realmType();
                if (Assembly.class.isAssignableFrom(source)) {
                    String nnn = source.getSimpleName();
                    if (nnn.length() > 8 && nnn.endsWith("Assembly")) {
                        nnn = nnn.substring(0, nnn.length() - 8);
                    }
                    if (nnn.length() > 0) {
                        // checkName, if not just App
                        // TODO need prefix
                        n = nnn;
                    }
                    if (nnn.length() == 0) {
                        n = "Assembly";
                    }
                }
                // TODO think it should be named Artifact type, for example, app, injector, ...
            } else if (this instanceof ExtensionSetup nes) {
                n = nes.model().nameComponent;
            }
            if (n == null) {
                n = "Unknown";
            }
            isFree = true;
        } else if (n.endsWith("?")) {
            n = n.substring(0, n.length() - 1);
            isFree = true;
        }

        // maybe just putIfAbsent, under the assumption that we will rarely need to override.
        if (treeParent != null) {
            if (treeParent.treeChildren != null && treeParent.treeChildren.containsKey(n)) {
                // If name exists. Lets keep a counter (maybe if bigger than 5). For people trying to
                // insert a given component 1 million times...
                if (!isFree) {
                    throw new RuntimeException("Name already exist " + n);
                }
                int counter = 1;
                String prefix = n;
                do {
                    n = prefix + counter++;
                } while (treeParent.treeChildren.containsKey(n));
            }

            if (newName != null) {
                // TODO check if changed name...
                treeParent.treeChildren.remove(name);
                treeParent.treeChildren.put(n, this);
            } else {
                name = n;
                if (treeParent.treeChildren == null) {
                    treeParent.treeChildren = new HashMap<>();
                    treeParent.treeFirstChild = treeParent.treeLastChild = this;
                } else {
                    treeParent.treeLastChild.treeNextSibling = this;
                    treeParent.treeLastChild = this;
                }
                treeParent.treeChildren.put(n, this);
            }
        }
        name = n;
    }

    public <T> void setRuntimeAttribute(Attribute<T> attribute, T value) {
        requireNonNull(attribute, "attribute is null");
        requireNonNull(value, "value is null");
        // check realm.open + attribute.write
    }

    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        PackedComponentDriver<C> d = (PackedComponentDriver<C>) requireNonNull(driver, "driver is null");

        // When an extension adds new components they are added to the container (the extension's parent)
        // Instead of the extension, because the extension itself is removed at runtime.
        ComponentSetup parent = this instanceof ExtensionSetup ? treeParent : this;

        // Wire the component
        ComponentSetup component = d.newComponent(build, realm, parent, wirelets);

        // Create a component configuration object and return it to the user
        return d.toConfiguration((ComponentConfigurationContext) component);
    }

    // This should only be called by special methods
    // We just take the lookup to make sure caller think twice before calling this method.
    public static ComponentSetup unadapt(Lookup caller, Component component) {
        if (component instanceof ComponentAdaptor ca) {
            return ca.compConf;
        }
        throw new IllegalStateException("This method must be called before a component is instantiated");
    }

    /** An adaptor of the {@link Component} interface from a {@link ComponentSetup}. */
    private static final class ComponentAdaptor implements Component {

        /** The component configuration to wrap. */
        private final ComponentSetup compConf;

        private ComponentAdaptor(ComponentSetup compConf) {
            this.compConf = requireNonNull(compConf);
        }

        /** {@inheritDoc} */
        @Override
        public AttributeMap attributes() {
            return compConf.attributes();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            return compConf.toList(ComponentSetup::adaptor);
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return compConf.treeDepth;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasModifier(ComponentModifier modifier) {
            return PackedComponentModifierSet.isSet(compConf.modifiers, modifier);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentModifierSet modifiers() {
            return compConf.modifiers();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return compConf.getName();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> parent() {
            ComponentSetup cc = compConf.treeParent;
            return cc == null ? Optional.empty() : Optional.of(cc.adaptor());
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
            return compConf.path();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentRelation relationTo(Component other) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Component resolve(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentStream stream(ComponentStream.Option... options) {
            return new PackedComponentStream(stream0(compConf, true, PackedComponentStreamOption.of(options)));
        }

        private Stream<Component> stream0(ComponentSetup origin, boolean isRoot, PackedComponentStreamOption option) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            children(); // lazy calc
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<ComponentAdaptor> c = (List) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, this.compConf)) {
                    Stream<Component> s = c.stream().flatMap(co -> co.stream0(origin, false, option));
                    return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
                }
                return Stream.empty();
            } else {
                return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
            }
        }
    }
}
