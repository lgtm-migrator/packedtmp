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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeMap;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentScope;
import app.packed.component.Wirelet;
import app.packed.container.ExtensionConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.container.ContainerSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;
import packed.internal.util.CollectionUtil;
import packed.internal.util.ThrowableUtil;

/** Abstract build-time setup of a component. Exposed to end-users as {@link ComponentConfigurationContext}. */
public abstract class ComponentSetup implements ComponentConfigurationContext {

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** Children of this node (lazily initialized) in insertion order. */
    @Nullable
    LinkedHashMap<String, ComponentSetup> children;

    /** The container this component is a part of. A container is a part of it self. */
    public final ContainerSetup container;

    /** The depth of the component in the tree. */
    protected final int depth;

    /** The modifiers of this component. */
    protected final int modifiers;

    /** The name of this component. */
    protected String name;

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    boolean nameInitializedWithWirelet;

    /** An action that, if present, must be called whenever the component has been completely wired. */
    @Nullable
    protected Consumer<? super ComponentMirror> onWire;

    /** The parent of this component, or null for a root component. */
    @Nullable
    protected final ComponentSetup parent;

    /** This component's constant pool. */
    public final ConstantPoolSetup pool;

    /** The realm this component is a part of. */
    public final RealmSetup realm;

    /** Wirelets that was specified when creating the component. */
    // Alternativ er den ikke final.. men bliver nullable ud eftersom der ikke er flere wirelets
    @Nullable
    public final WireletWrapper wirelets;

    /**
     * Create a new component.
     * 
     * @param application
     *            the application this component is a part of
     * @param realm
     *            the realm this component is part of
     * @param driver
     *            the driver of the component
     * @param parent
     *            any parent this component might have
     */
    protected ComponentSetup(ApplicationSetup application, RealmSetup realm, PackedComponentDriver<?> driver, @Nullable ComponentSetup parent,
            Wirelet[] wirelets) {
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;

        this.application = requireNonNull(application);
        this.realm = requireNonNull(realm);
        this.container = this instanceof ContainerSetup container ? container : parent.container;

        // Various
        if (/* is root container */ parent == null) {
            this.modifiers = application.build.modifiers | driver.modifiers;
            this.pool = application.constantPool;
        } else {
            this.modifiers = driver.modifiers;
            this.pool = driver.modifiers().hasRuntime() ? new ConstantPoolSetup() : parent.pool;
            this.onWire = parent.onWire;
        }

        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            this.wirelets = null;
        } else {
            // If it is the root
            Wirelet[] ws;
            if (PackedComponentModifierSet.isApplication(modifiers)) {
                if (application.driver.wirelet == null) {
                    ws = WireletArray.flatten(wirelets);
                } else {
                    ws = WireletArray.flatten(application.driver.wirelet, Wirelet.combine(wirelets));
                }
            } else {
                ws = WireletArray.flatten(wirelets);
            }

            this.wirelets = new WireletWrapper(ws);

            // May initialize the component's name, onWire, ect
            // Do we need to consume internal wirelets???
            // Maybe that is what they are...
            int unconsumed = 0;
            for (Wirelet w : ws) {
                if (w instanceof InternalWirelet bw) {
                    // Maaske er alle internal wirelets first passe
                    bw.onBuild(this);
                } else {
                    unconsumed++;
                }
            }
            if (unconsumed > 0) {
                this.wirelets.unconsumed = unconsumed;
            }

            if (nameInitializedWithWirelet && parent != null) {

                // addChild(child, name);
            }
        }
    }

    final AttributeMap attributes() {
        // Det er ikke super vigtigt at den her er hurtig paa configurations tidspunktet...
        // Maaske er det simpelthen et view...
        // Hvor vi lazily fx calculere EntrySet (og gemmer i et felt)
        DefaultAttributeMap dam = new DefaultAttributeMap();
        attributesAdd(dam);
        return dam;
    }

    protected void attributesAdd(DefaultAttributeMap dam) {}

    public final void checkIsWiring() {
        if (realm.current() != this) {
            String errorMsg;
            if (realm.root == this) {
                errorMsg = "This operation must be called as the first thing in Assembly#build()";
            } else {
                errorMsg = "This operation must be called immediately after the component has been wired";
            }
            throw new IllegalStateException(errorMsg);
        }
    }

    //
    public final String getName() {
        // Dette kunne ogsaa wire componenten?
        return name;
    }

    protected final void initializeNameWithPrefix(String name) {
        String n = name;
        if (parent != null) {
            LinkedHashMap<String, ComponentSetup> c = parent.children;
            if (c == null) {
                c = parent.children = new LinkedHashMap<>();
                c.put(name, this);
            } else {
                int counter = 1;
                while (c.putIfAbsent(n, this) != null) {
                    n = name + counter++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test adding 1
                                          // million of the same component type
                }
            }
        }
        this.name = n;
    }

    /**
     * Tests that this component is in the same specified scope as another component.
     * 
     * @param scope
     *            the scope to test
     * @param other
     *            the other component to test
     * @return true if in the same scope, otherwise false
     */
    public final boolean isInSame(ComponentScope scope, ComponentSetup other) {
        requireNonNull(scope, "scope is null");
        requireNonNull(other, "other is null");
        return switch (scope) {
        case APPLICATION -> application == other.application;
        case BUILD -> application.build == other.application.build;
        case COMPONENT -> this == other;
        case CONTAINER -> container == other.container;
        case NAMESPACE -> application.build.namespace == other.application.build.namespace;
        };
    }

    /**
     * Links a new assembly.
     * 
     * @param assembly
     *            the assembly to link
     * @param realm
     *            realm
     * @param wirelets
     *            optional wirelets
     * @return the component that was linked
     * @see ComponentConfigurationContext#link(Assembly, Wirelet...)
     * @see ExtensionConfiguration#link(Assembly, Wirelet...)
     */
    public final ComponentMirror link(Assembly<?> assembly, RealmSetup realm, Wirelet... wirelets) {
        // Extract the component driver from the assembly
        PackedComponentDriver<?> driver = PackedComponentDriver.getDriver(assembly);

        // Create the new realm that should be used for linking
        RealmSetup newRealm = realm.link(driver, this, assembly, wirelets);

        // Create the component configuration that is needed by the assembly
        ComponentConfiguration configuration = driver.toConfiguration(newRealm.root);

        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        try {
            RealmSetup.MH_ASSEMBLY_DO_BUILD.invoke(assembly, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the new realm again after the assembly has been successfully linked
        newRealm.close();

        return newRealm.root.mirror();
    }

    /** {@inheritDoc} */
    public final ComponentMirror link(Assembly<?> assembly, Wirelet... wirelets) {
        return link(assembly, realm, wirelets);
    }

    /** {@inheritDoc} */
    public final ComponentMirror mirror() {
        return new ComponentMirrorAdaptor(this);
    }

    /** {@inheritDoc} */
    public final PackedComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(modifiers);
    }
    
    /** {@inheritDoc} */
    public final void named(String name) {
        checkComponentName(name); // Check if the name is valid
        checkIsWiring();

        // If a name has been set using a wirelet it cannot be overridden
        if (nameInitializedWithWirelet) {
            return;
        } else if (name.equals(this.name)) {
            return;
        }

        // maybe assume s==0

        if (parent != null) {
            parent.children.remove(this.name);
            if (parent.children.putIfAbsent(name, this) != null) {
                throw new IllegalArgumentException("A component with the specified name '" + name + "' already exists");
            }
        }
        this.name = name;
    }

    protected final void onWired() {
        if (onWire != null) {
            onWire.accept(mirror());
        }
    }

    /** {@return the path of this component} */
    public final NamespacePath path() {
        return PackedTreePath.of(this);
    }

    public final <T> void setRuntimeAttribute(Attribute<T> attribute, T value) {
        requireNonNull(attribute, "attribute is null");
        requireNonNull(value, "value is null");
        // check realm.open + attribute.write
    }

    public final <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, RealmSetup realm, Wirelet... wirelets) {
        PackedComponentDriver<C> realDriver = (PackedComponentDriver<C>) requireNonNull(driver, "driver is null");

        // Wire a new component
        ComponentSetup component = realm.wire(realDriver, this, wirelets);

        // Return a component configuration to the user
        return realDriver.toConfiguration(component);
    }

    public final <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return wire(driver, realm, wirelets);
    }

    /**
     * Checks the name of the component.
     * 
     * @param name
     *            the name to check
     * @return the name if valid
     */
    public static String checkComponentName(String name) {
        requireNonNull(name, "name is null");
        if (name != null) {

        }
        return name;
    }

    /** An mirror adaptor for {@link ComponentSetup}. */
    private record ComponentMirrorAdaptor(ComponentSetup component) implements ComponentMirror {

        /** {@inheritDoc} */
        @Override
        public AttributeMap attributes() {
            return component.attributes();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ComponentMirror> children() {
            LinkedHashMap<String, ComponentSetup> m = component.children;
            return m == null ? List.of() : CollectionUtil.unmodifiableView(m.values(), c -> c.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return component.depth;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasModifier(ComponentModifier modifier) {
            return PackedComponentModifierSet.isSet(component.modifiers, modifier);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentModifierSet modifiers() {
            return component.modifiers();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return component.name;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<ComponentMirror> parent() {
            ComponentSetup parent = component.parent;
            return parent == null ? Optional.empty() : Optional.of(parent.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
            return component.path();
        }

        /** {@inheritDoc} */
        @Override
        public Relation relationTo(ComponentMirror other) {
            requireNonNull(other, "other is null");
            return ComponentSetupRelation.of(component, ((ComponentMirrorAdaptor) other).component);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentMirror resolve(CharSequence path) {
            LinkedHashMap<String, ComponentSetup> map = component.children;
            if (map != null) {
                ComponentSetup cs = map.get(path.toString());
                if (cs != null) {
                    return cs.mirror();
                }
            }
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentMirrorStream stream(ComponentMirrorStream.Option... options) {
            return new PackedComponentStream(stream0(component, true, PackedComponentStreamOption.of(options)));
        }

        private Stream<ComponentMirror> stream0(ComponentSetup origin, boolean isRoot, PackedComponentStreamOption option) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Collection<ComponentMirrorAdaptor> c = (Collection) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, component)) {
                    Stream<ComponentMirror> s = c.stream().flatMap(co -> co.stream0(origin, false, option));
                    return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
                }
                return Stream.empty();
            } else {
                return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isInSame(ComponentScope scope, ComponentMirror other) {
            requireNonNull(other, "other is null");
            return component.isInSame(scope, ((ComponentMirrorAdaptor) other).component);
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return component.application.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentMirror root() {
            ComponentSetup c = component;
            while (c.parent != null) {
                c = c.parent;
            }
            return c == component ? this : c.mirror();
        }
    }
}
