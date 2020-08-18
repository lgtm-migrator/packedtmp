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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.artifact.ArtifactSource;
import app.packed.attribute.AttributeSet;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import packed.internal.artifact.InstantiationContext;
import packed.internal.artifact.PackedAssemblyContext;
import packed.internal.component.PackedComponentDriver.SingletonComponentDriver;
import packed.internal.component.PackedComponentDriver.StatelessComponentDriver;
import packed.internal.component.wirelet.InternalWirelet.ComponentNameWirelet;
import packed.internal.component.wirelet.WireletModel;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.component.wirelet.WireletPipelineContext;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.PackedContainerRole;
import packed.internal.container.PackedRealm;
import packed.internal.inject.ConfigSiteInjectOperations;

/** The build time representation of a component. */
public final class ComponentNodeConfiguration implements ComponentConfigurationContext {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The assembly this component is a part of. */
    private final PackedAssemblyContext assembly;

    /** The driver used to create this component. */
    private final PackedComponentDriver<?> driver;

    /** The realm the component belongs to. */
    private final PackedRealm realm;

    /** Any wirelets that was specified by the user when creating this configuration. */
    @Nullable
    public final WireletPack wirelets;

    /** The pod the component belongs to. */
    final PackedPodConfigurationContext pod;

    /** The name of the component. */
    String name;

    /** The depth of the component in the hierarchy (including any parent artifacts). */
    final int depth;

    /** Any container this component is part of. A container is part of it self */
    @Nullable
    final PackedContainerRole container;

    /** Children of this node (lazily initialized). Order maintained by {@link #nextSibling} and friends. */
    @Nullable
    HashMap<String, ComponentNodeConfiguration> children;

    /** The first child of this component. */
    @Nullable
    public ComponentNodeConfiguration firstChild;

    /**
     * The latest inserted child of this component. Or null if this component has no children. Is exclusively used to help
     * maintain {@link #nextSibling}.
     */
    @Nullable
    private ComponentNodeConfiguration lastChild;

    /** The next sibling, in insertion order */
    @Nullable
    public ComponentNodeConfiguration nextSibling;

    /** The parent of this component, or null for a root component. */
    @Nullable
    final ComponentNodeConfiguration parent;

    /**************** See how much of this we can get rid of. *****************/

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    private boolean finalState = false;

    int nameState;

    private static final int NAME_INITIALIZED_WITH_WIRELET = 1 << 18; // set atomically with DONE
    private static final int NAME_SET = 1 << 17; // set atomically with ABNORMAL
    private static final int NAME_GET = 1 << 16; // true if joiner waiting
    private static final int NAME_GET_PATH = 1 << 15; // true if joiner waiting
    private static final int NAME_CHILD_GOT_PATH = 1 << 14; // true if joiner waiting

    private static final int NAME_GETSET_MASK = NAME_SET + NAME_GET + NAME_GET_PATH + NAME_CHILD_GOT_PATH;

    public static ComponentNodeConfiguration newAssembly(PackedAssemblyContext assembly, PackedComponentDriver<?> driver, ConfigSite configSite,
            PackedRealm realm, Wirelet... wirelets) {
        return new ComponentNodeConfiguration(assembly, realm, null, driver, configSite, wirelets);
    }

    @Nullable
    public ComponentNodeConfiguration parentOrNull() {
        return parent;
    }

    public ComponentNodeConfiguration newChild(PackedComponentDriver<?> driver, ConfigSite configSite, PackedRealm realm, Wirelet... wirelets) {
        return new ComponentNodeConfiguration(assembly, realm, this, driver, configSite, wirelets);
    }

    /**
     * Creates a new instance of this class
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    private ComponentNodeConfiguration(PackedAssemblyContext assembly, PackedRealm realm, @Nullable ComponentNodeConfiguration parent,
            PackedComponentDriver<?> driver, ConfigSite configSite, Wirelet... wirelets) {
        this.assembly = requireNonNull(assembly);
        this.realm = requireNonNull(realm);

        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth + 1;

        this.driver = requireNonNull(driver);
        this.pod = parent == null || driver.isGuest() ? new PackedPodConfigurationContext(this) : parent.pod;
        this.container = driver.isContainer() ? new PackedContainerRole(this) : parent.container;

        this.configSite = requireNonNull(configSite);
        this.wirelets = WireletPack.from(this, wirelets);

        setName0(null); // initialize name
    }

    /**
     * Returns the container this component is a part of. Or null if this component is the top level container.
     * 
     * @return the container this component is a part of
     */
    @Nullable
    public PackedContainerRole container() {
        return container;
    }

    public PackedRealm realm() {
        return realm;
    }

    public ComponentNodeConfiguration closeAssembly() {
        finalState = true;
        if (container != null) {
            container.advanceTo(PackedContainerRole.LS_3_FINISHED);
        }
        return this;
    }

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements {@link ArtifactSource}.
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
     * {@link ConfigSite#UNKNOWN}.
     * 
     * @param operation
     *            the operation
     * @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
     * @see StackWalker
     */
    // TODO add stuff about we also ignore non-concrete container sources...
    public ConfigSite captureStackFrame(String operation) {
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        // Vi kan spoerge "if context.captureStackFrame() ...."

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private boolean captureStackFrameIgnoreFilter(StackFrame frame) {
        Class<?> c = frame.getDeclaringClass();
        // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
        // Syntes bare vi filtrer app.packed.base modulet fra...
        // Kan vi ikke checke om imod vores container source.

        // ((PackedExtensionContext) context()).container().source
        // Nah hvis man koere fra config er det jo fint....
        // Fra config() paa en bundle er det fint...
        // Fra alt andet ikke...

        // Dvs ourContainerSource
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ArtifactSource.class.isAssignableFrom(c));
    }

    public int depth() {
        return depth;
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (finalState) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    public ComponentNode instantiateTree(InstantiationContext ic) {
        return new ComponentNode(null, this, ic);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configSite;
    }

    /**
     * Returns the driver of this component.
     * 
     * @return the driver of this component
     */
    public PackedComponentDriver<?> driver() {
        return driver;
    }

    @SuppressWarnings("unchecked")
    public Optional<Class<? extends Extension>> extension() {
        return Extension.class.isAssignableFrom(realm.type()) ? Optional.empty() : Optional.of((Class<? extends Extension>) realm.type());
    }

    private void setName0(String newName) {
        String n = newName;
        if (newName == null) {
            if (wirelets != null) {
                ComponentNameWirelet cwn = wirelets.nameWirelet();
                if (cwn != null) {
                    nameState = NAME_INITIALIZED_WITH_WIRELET;
                    n = cwn.name;
                }
            }
        }

        boolean isFree = false;

        if (n == null) {
            n = driver.defaultName(realm);
            isFree = true;
        } else if (n.endsWith("?")) {
            n = n.substring(0, n.length() - 1);
            isFree = true;
        }

        // maybe just putIfAbsent, under the assumption that we will rarely need to override.
        if (parent != null) {
            if (parent != null && parent.children != null && parent.children.containsKey(n)) {
                // If name exists. Lets keep a counter (maybe if bigger than 5). For people trying to
                // insert a given component 1 million times...
                if (!isFree) {
                    throw new RuntimeException("Name already exist " + n);
                }
                int counter = 1;
                String prefix = n;
                do {
                    n = prefix + counter++;
                } while (parent.children.containsKey(n));
            }

            if (newName != null) {
                // TODO check if changed name...
                parent.children.remove(name);
                parent.children.put(n, this);
            } else {
                name = n;
                if (parent.children == null) {
                    parent.children = new HashMap<>();
                    parent.firstChild = parent.lastChild = this;
                } else {
                    parent.lastChild.nextSibling = this;
                    parent.lastChild = this;
                }
                parent.children.put(n, this);
            }
        }
        name = n;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        int anyPathMask = NAME_GET_PATH + NAME_CHILD_GOT_PATH;
        if ((nameState & anyPathMask) != 0) {
            ComponentNodeConfiguration p = parent;
            while (p != null && ((p.nameState & anyPathMask) == 0)) {
                p.nameState = (p.nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
            }
        }
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
        return PackedComponentPath.of(this); // show we weak intern them????
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        // First lets check the name is valid
        ComponentNameWirelet.checkName(name);
        int s = nameState;

        checkConfigurable();

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

    // Previously this method returned the specified bundle. However, to encourage people to configure the bundle before
    // calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have void return
    // type. Maybe in the future LinkedBundle<- (LinkableContainerSource)
    // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
    // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
    // loop situations, for example, if a bundle recursively links itself which fails by throwing
    // java.lang.StackOverflowError instead of an infinite loop.
    @Override
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");

        // Extract the driver from the bundle
        PackedComponentDriver<?> driver = BundleConfiguration.driverOf(bundle);

        // check if container

        if (driver.isContainer()) {
            // IDK do we want to progress to next stage just in case...
            if (container.containerState == PackedContainerRole.LS_0_MAINL) {
                container.advanceTo(PackedContainerRole.LS_1_LINKING);
            } else if (container.containerState == PackedContainerRole.LS_2_HOSTING) {
                throw new IllegalStateException("Was hosting");
            } else if (container.containerState == PackedContainerRole.LS_3_FINISHED) {
                throw new IllegalStateException("Was Assembled");
            }
        }
        // Create the child node
        // ConfigSite cs = ConfigSiteSupport.captureStackFrame(configSite(), ConfigSiteInjectOperations.INJECTOR_OF);
        ConfigSite cs = ConfigSite.UNKNOWN;
        ComponentNodeConfiguration p = driver().isExtension() ? parent : this;
        ComponentNodeConfiguration newNode = p.newChild(driver, cs, PackedRealm.fromBundle(bundle), wirelets);

        // Invoke Bundle::configure
        BundleConfiguration.configure(bundle, driver.forBundleConf(newNode));

        newNode.finalState = true;
    }

    public <T> SingletonConfiguration<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        ConfigSite configSite = captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);
        SingletonComponentDriver scd = new SingletonComponentDriver(realm, instance);

        ComponentNodeConfiguration conf = newChild(scd, configSite, realm);
        return scd.toConf(conf);
    }

    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        ConfigSite configSite = captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);
        SingletonComponentDriver scd = new SingletonComponentDriver(realm, factory);

        ComponentNodeConfiguration conf = newChild(scd, configSite, realm);
        return scd.toConf(conf);
    }

    public StatelessConfiguration installStateless(Class<?> implementation) {
        StatelessComponentDriver scd = new StatelessComponentDriver(realm, implementation);

        ConfigSite configSite = captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);

        ComponentNodeConfiguration conf = newChild(scd, configSite, realm);
        return scd.toConf(conf);
    }

    /** {@inheritDoc} */
    @Override
    public <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        PackedComponentDriver<C> d = (PackedComponentDriver<C>) driver;

        ConfigSite configSite = captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);

        ComponentNodeConfiguration conf = newChild(d, configSite, realm);
        // return d.toConf(conf);
        System.out.println(conf);
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public <W extends Wirelet> Optional<W> wireletAny(Class<W> type) {
        WireletModel wm = WireletModel.of(type);
        boolean inherited = wm.inherited();
        Object wop = null;
        if (wirelets != null) {
            wop = wirelets.getWireletOrPipeline(type);
        }
        if (wop == null && inherited) {
            ComponentNodeConfiguration acc = parent;
            while (acc != null) {
                if (acc.wirelets != null) {
                    wop = acc.wirelets.getWireletOrPipeline(type);
                    if (wop != null) {
                        break;
                    }
                }
                acc = acc.parent;
            }
        }
        if (wop instanceof WireletPipelineContext) {
            wop = ((WireletPipelineContext) wop).instance;
            requireNonNull(wop);// Maybe not instantiated yet???
        }
        return wop == null ? Optional.empty() : Optional.ofNullable((W) wop);
    }

    @SuppressWarnings("unchecked")
    public <W extends Wirelet> Optional<W> assemblyWirelet(Class<W> type) {
        WireletModel wm = WireletModel.of(type);
        if (!wm.requireAssemblyTime) {
            throw new IllegalStateException("Wirelet of type " + type + " does not have assemblytime = true");
        }
        return wirelets == null ? Optional.empty() : Optional.ofNullable((W) wirelets.getWireletOrPipeline(type));
    }

    /**
     * Returns a {@link Component} adaptor of this node.
     * 
     * @return a component adaptor
     */
    public Component adaptToComponent() {
        return new ComponentAdaptor(this);
    }

    /** An adaptor of the {@link Component} interface from a {@link ComponentNodeConfiguration}. */
    public static final class ComponentAdaptor implements Component {

        /** The component configuration to wrap. */
        public final ComponentNodeConfiguration conf;

        private ComponentAdaptor(ComponentNodeConfiguration c) {
            this.conf = requireNonNull(c);
        }

        /** {@inheritDoc} */
        @Override
        public AttributeSet attributes() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            int size = conf.children == null ? 0 : conf.children.size();
            if (size == 0) {
                return List.of();
            } else {
                ArrayList<Component> result = new ArrayList<>(size);
                for (ComponentNodeConfiguration acc = conf.firstChild; acc != null; acc = acc.nextSibling) {
                    result.add(acc.adaptToComponent());
                }
                return result;
            }
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return conf.configSite(); // We might need to rewrite this for image...
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return conf.depth;
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return conf.getName();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> parent() {
            ComponentNodeConfiguration p = conf.parent;
            return p == null ? Optional.empty() : Optional.of(p.adaptToComponent());
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath path() {
            return conf.path();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentRelation relationTo(Component other) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentStream stream(ComponentStream.Option... options) {
            return new PackedComponentStream(stream0(conf, true, PackedComponentStreamOption.of(options)));
        }

        private Stream<Component> stream0(ComponentNodeConfiguration origin, boolean isRoot, PackedComponentStreamOption option) {
            // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
            children(); // lazy calc
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<ComponentAdaptor> c = (List) children();
            if (c != null && !c.isEmpty()) {
                if (option.processThisDeeper(origin, this.conf)) {
                    Stream<Component> s = c.stream().flatMap(co -> co.stream0(origin, false, option));
                    return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
                }
                return Stream.empty();
            } else {
                return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T containerUse(Class<T> extensionType) {
        if (container == null || container.node != this) {
            throw new UnsupportedOperationException("This method can only be used by a component has ComponentDriver.Option.container() enabled");
        }
        return container.use(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> containerExtensions() {
        if (container == null || container.node != this) {
            throw new UnsupportedOperationException("This method can only be used by a component has ComponentDriver.Option.container() enabled");
        }
        return container.extensions();
    }
}
