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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.component.Install;
import app.packed.container.AnyBundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.Container;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorExtension;
import app.packed.inject.InstantiationMode;
import app.packed.util.Nullable;
import packed.internal.classscan.DescriptorFactory;
import packed.internal.componentcache.ComponentClassDescriptor;
import packed.internal.componentcache.ComponentLookup;
import packed.internal.componentcache.ContainerConfiguratorCache;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.buildtime.DependencyGraph;
import packed.internal.inject.buildtime.OldDefaultComponentConfiguration;

/** The default implementation of {@link ContainerConfiguration}. */
public final class DefaultContainerConfiguration extends AbstractNamedConfiguration implements ContainerConfiguration {

    /** The lookup object. We default to public access */
    public DescriptorFactory accessor = DescriptorFactory.PUBLIC;

    /** The bundle we created this configuration from. Or null if we using a configurator. */
    @Nullable
    public final AnyBundle bundle;

    /** The configurator cache. */
    final ContainerConfiguratorCache ccc;

    /** All child containers, in order of wiring order. */
    // final LinkedHashMap<String, AbstractNamedConfiguration> children = new LinkedHashMap<>();

    /** All registered components. */
    public final LinkedHashMap<String, DefaultComponentConfiguration> components = new LinkedHashMap<>();

    /** The configuration site of this object. */
    private final InternalConfigurationSite configurationSite;

    /** All extensions that are currently used by this configuration, ordered by first use. */
    private final LinkedHashMap<Class<? extends Extension<?>>, Extension<?>> extensions = new LinkedHashMap<>();

    private boolean isConfigured;

    /** All child containers, in order of wiring order. */
    final LinkedHashMap<String, DefaultContainerConfiguration> links = new LinkedHashMap<>();

    private ComponentLookup lookup;

    /** Any parent container configuration. */
    @Nullable
    final DefaultContainerConfiguration parent;

    /** A list of wirelets used when creating this configuration. */
    private final WireletList wirelets;

    /** The type of wiring. */
    final WiringType wiringType;

    DefaultContainerConfiguration(DefaultContainerConfiguration parent, WiringType wiringType, Class<?> configuratorType, @Nullable AnyBundle bundle,
            Wirelet... wirelets) {
        super(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF), parent);
        this.configurationSite = InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF);
        this.lookup = this.ccc = ContainerConfiguratorCache.of(configuratorType);
        this.bundle = bundle;
        this.parent = parent;
        this.wiringType = requireNonNull(wiringType);
        this.wirelets = WireletList.of(wirelets);
    }

    public Container buildContainer() {
        configure();
        finish();
        new DependencyGraph(this).instantiate();
        return new InternalContainer(this, use(InjectorExtension.class).builder.publicInjector);
    }

    public void buildDescriptor(BundleDescriptor.Builder builder) {
        configure();
        finish();
        // TODO DependencyGraph move to extension....
        DependencyGraph injectorBuilder = new DependencyGraph(this);
        injectorBuilder.analyze();

        builder.setBundleDescription(description);
        builder.setName(name);
        for (Extension<?> e : extensions.values()) {
            e.buildBundle(builder);
        }
    }

    public Injector buildInjector() {
        finish();
        new DependencyGraph(this).instantiate();
        return use(InjectorExtension.class).builder.publicInjector;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends AnyBundle> T link(T bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");

        // Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
        // has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
        // loop situations, for example, if a bundle recursively links itself which fails by throwing
        // java.lang.StackOverflowError instead.

        DefaultContainerConfiguration dcc = new DefaultContainerConfiguration(this, WiringType.LINK, bundle.getClass(), bundle, wirelets);
        dcc.configure();
        links.put(dcc.name, dcc);// name has already been verified via configure()->finalizeName()
        return bundle;
    }

    public void configure() {
        if (isConfigured) {
            return;
        }
        if (bundle != null) {
            if (bundle.getClass().isAnnotationPresent(Install.class)) {
                install(bundle);
            }
            bundle.doConfigure(this);
        }
        finalizeName();
        isConfigured = true;
    }

    public void finish() {
        for (Extension<?> e : extensions.values()) {
            e.onFinish();
        }
    }

    private void finalizeName() {
        // See if we have any wirelet that overrides the name (wirelet name has already been verified)
        wirelets.consumeLast(OverrideNameWirelet.class, w -> name = w.name);

        if (name == null) {
            name = finalizeNameWithPrefix(ccc.defaultPrefix());
        } else {
            String n = name;
            if (n.endsWith("?")) {
                name = finalizeNameWithPrefix(n.substring(0, n.length() - 1));
            } else if (parent.components.containsKey(n)) {
                throw new IllegalStateException();
            } else if (parent.links.containsKey(n)) {
                throw new IllegalStateException();
            }
        }
        // TODO make name unmodifiable
    }

    private String finalizeNameWithPrefix(String prefix) {
        if (parent == null) {
            return prefix;
        } else {
            String newName = prefix;
            int counter = 0;
            for (;;) {
                if (!parent.components.containsKey(newName) && !parent.links.containsKey(newName)) {
                    return newName;
                }
                // Maybe now keep track of the counter... In a prefix hashmap, Its probably benchmarking code though
                // But it could also be a host???
                newName = prefix + counter++;
            }
        }
    }

    @Override
    public final void checkConfigurable() {

    }

    /** {@inheritDoc} */
    @Override
    public InternalConfigurationSite configurationSite() {
        return configurationSite;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> extensions() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    public <W> void forEachWirelet(Class<W> wireletType, Consumer<? super W> action) {
        requireNonNull(wireletType, "wireletType is null");
        requireNonNull(action, "action is null");
    }

    @Override
    void freezeName() {

    }

    /**
     * Returns an extension of the specified type if installed, otherwise null.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type if installed, otherwise null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Extension<T>> T getExtension(Class<T> extensionType) {
        return (T) extensions.get(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    public ComponentConfiguration install(Class<?> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    public ComponentConfiguration install(Factory<?> factory) {
        return new OldDefaultComponentConfiguration(this, factory, InstantiationMode.SINGLETON);
    }

    public ComponentConfiguration install(Object instance) {
        requireNonNull(instance, "instance is null");
        ComponentClassDescriptor ccd = lookup.componentDescriptorOf(instance.getClass());
        OldDefaultComponentConfiguration dcc = new OldDefaultComponentConfiguration(this, instance);
        ccd.initialize(this, dcc);
        return dcc;
    }

    public ComponentConfiguration installStatic(Class<?> implementation) {
        return new OldDefaultComponentConfiguration(this, Factory.findInjectable(implementation), InstantiationMode.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? ccc : ccc.withLookup(lookup);
        this.accessor = DescriptorFactory.get(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public DefaultContainerConfiguration setDescription(String description) {
        checkConfigurable();
        this.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@Nullable String name) {
        checkConfigurable();
        this.name = checkName(name);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension<T>> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return (T) extensions.computeIfAbsent(extensionType, k -> {
            checkConfigurable(); // we can use extensions that have already been installed, but not add new ones
            Extension<?> e = ExtensionClassCache.newInstance(extensionType);
            AppPackedContainerSupport.invoke().initializeExtension(e, this);
            return e;
        });
    }

    /** {@inheritDoc} */
    @Override
    public List<Wirelet> wirelets() {
        return wirelets.list();
    }

    /**
     * Checks the name of the container or component.
     * 
     * @param name
     *            the name to check
     * @return the name if valid
     */
    private static String checkName(String name) {
        if (name != null) {

        }
        return name;
    }

    /** A wiring option that overrides any existing container name. */
    public static class OverrideNameWirelet extends Wirelet {

        /** The (checked) name to override with. */
        private final String name;

        /**
         * Creates a new option
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWirelet(String name) {
            this.name = checkName(name);
        }
    }
}
//
// // Maybe should be able to define a namig strategy, to avoid reuse? Mostly for distributed
// // Lazy initialized... Maybe this is part of the Specification/ContainerConfigurationProvider
// final ConcurrentHashMap<String, AtomicLong> autoGeneratedComponentNames = new ConcurrentHashMap<>();

// public void newOperation() {
// AbstractFreezableNode c = currentNode;
// if (c != null) {
// c.freeze();
// }
// currentNode = null;
// }
//
// final void checkConfigurable() {
//
// }
//
// public <T extends AbstractFreezableNode> T newOperation(T node) {
// AbstractFreezableNode c = currentNode;
// if (c != null) {
// c.freeze();
// }
// currentNode = node;
// return node;
// }