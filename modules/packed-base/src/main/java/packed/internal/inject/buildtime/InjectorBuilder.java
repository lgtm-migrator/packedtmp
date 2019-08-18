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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.WireletList;
import app.packed.feature.FeatureKey;
import app.packed.inject.Factory;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.InjectorContract;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDependency;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.DefaultComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.DefaultInjector;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.invoke.FunctionHandle;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

/** This class records all service related information for a single box. */
public final class InjectorBuilder {

    public static FeatureKey<BuildServiceNodeDefault<?>> FK = new FeatureKey<>() {};

    public boolean autoRequires = true;

    /** A map of all nodes that are exported out from the box. */
    public final ServiceNodeMap exports;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<Entry<BuildServiceNode<?>, ServiceDependency>> missingDependencies;

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BuildServiceNode<?>, List<ServiceDependency>> unresolvedDependencies;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final ServiceNodeMap nodes;

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> optional = new HashSet<>();

    /** A set of all explicitly registered required service keys. */
    public final HashSet<Key<?>> required = new HashSet<>();

    public final ArrayList<BuildServiceNodeExported<?>> exportedNodes = new ArrayList<>();

    public DefaultInjector privateInjector;

    public DefaultInjector publicInjector;

    public ArrayList<BuildServiceNode<?>> nodes2 = new ArrayList<>();

    final PackedContainerConfiguration containerConfiguration;

    public InjectorBuilder(PackedContainerConfiguration containerConfiguration) {
        this.containerConfiguration = requireNonNull(containerConfiguration);
        boolean exportNodes = true;
        // System.out.println(exportNodes);
        if (exportNodes) {
            nodes = new ServiceNodeMap();
            exports = new ServiceNodeMap();
        } else {
            // dont know, maybe just not put things in exported...
            nodes = exports = new ServiceNodeMap();
        }
    }

    /**
     * Adds the specified key to the list of optional services.
     * 
     * @param key
     *            the key to add
     */
    public void addOptional(Key<?> key) {
        requireNonNull(key, "key is null");
        optional.add(key);
    }

    /**
     * Adds the specified key to the list of required services.
     * 
     * @param key
     *            the key to add
     */
    public void addRequired(Key<?> key) {
        requireNonNull(key, "key is null");
        required.add(key);
    }

    public <T> ServiceConfiguration<T> exportKey(Key<T> key, InternalConfigSite cs) {
        BuildServiceNodeExported<T> bn = new BuildServiceNodeExported<>(this, cs);
        bn.as(key);
        exportedNodes.add(bn);
        return new PackedServiceConfiguration<>(containerConfiguration, bn);
    }

    public void importAll(Injector injector, WireletList wirelets) {
        new InjectorImporter(containerConfiguration, this, injector, wirelets).importAll();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FunctionHandle<T> function) {
        BuildServiceNodeDefault<?> sc = cc.features().get(FK);
        if (sc == null) {
            sc = new BuildServiceNodeDefault<>(this, cc, InstantiationMode.SINGLETON, containerConfiguration.lookup.readable(function),
                    (List) factory.dependencies());
        }
        sc.as((Key) factory.key());
        nodes2.add(sc);
        return new PackedProvidedComponentConfiguration<>((DefaultComponentConfiguration) cc, (BuildServiceNodeDefault) sc);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have installed a node via @Provides annotations.
        BuildServiceNodeDefault<?> sc = cc.features().get(InjectorBuilder.FK);
        if (sc == null) {
            sc = new BuildServiceNodeDefault<T>(this, (InternalConfigSite) cc.configSite(), instance);
        }

        sc.as((Key) Key.of(instance.getClass()));
        nodes2.add(sc);
        return new PackedProvidedComponentConfiguration<>((DefaultComponentConfiguration) cc, (BuildServiceNodeDefault) sc);
    }

    public void buildContract(InjectorContract.Builder builder) {
        // Why do we need that list
        // for (ServiceBuildNode<?> n : exportedNodes) {
        // if (n instanceof ServiceBuildNodeExposed) {
        // builder.addProvides(n.getKey());
        // }
        // }
        if (optional != null) {
            optional.forEach(k -> {
                // We remove all optional dependencies that are also mandatory.
                if (required == null || !required.contains(k)) {
                    builder.addOptional(k);
                }
            });
        }
        if (required != null) {
            required.forEach(k -> builder.addRequires(k));
        }
    }

    public void checkForMissingDependencies() {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<BuildServiceNode<?>, ServiceDependency> e : missingDependencies) {
                if (!e.getValue().isOptional() && !e.getKey().autoRequires) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    List<InternalDependencyDescriptor> dependencies = e.getKey().dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                    ServiceDependency dependency = e.getValue();
                    sb.append("parameter on ");
                    if (dependency.variable() != null) {

                        InternalExecutableDescriptor ed = (InternalExecutableDescriptor) ((InternalParameterDescriptor) dependency.variable().get())
                                .declaringExecutable();
                        sb.append(ed.descriptorTypeName()).append(": ");
                        sb.append(ed.getDeclaringClass().getCanonicalName());
                        if (ed instanceof MethodDescriptor) {
                            sb.append("#").append(((MethodDescriptor) ed).getName());
                        }
                        sb.append("(");
                        if (dependencies.size() > 1) {
                            StringJoiner sj = new StringJoiner(", ");
                            for (int j = 0; j < dependencies.size(); j++) {
                                if (j == dependency.parameterIndex().getAsInt()) {
                                    sj.add("-> " + dependency.key().toString() + " <-");
                                } else {
                                    sj.add(dependencies.get(j).key().typeLiteral().rawType().getSimpleName());
                                }
                            }
                            sb.append(sj.toString());
                        } else {
                            sb.append(dependency.key().toString());
                            sb.append(" ");
                            sb.append(dependency.variable().get().getName());
                        }
                        sb.append(")");
                    }
                    // b.root.requiredServicesMandatory.add(e.get)
                    // System.err.println(b.root.privateNodeMap.stream().map(e -> e.key()).collect(Collectors.toList()));
                    throw new InjectionException(sb.toString());
                }
            }
        }
    }

    public void recordMissingDependency(BuildServiceNode<?> node, ServiceDependency dependency, boolean fromParent) {

    }

    public void buildBundle(Builder descriptor) {
        for (ServiceNode<?> n : nodes) {
            if (n instanceof BuildServiceNode) {
                descriptor.addServiceDescriptor(((BuildServiceNode<?>) n).toDescriptor());
            }
        }

        for (BuildServiceNode<?> n : exportedNodes) {
            if (n instanceof BuildServiceNodeExported) {
                descriptor.contract().services().addProvides(n.getKey());
            }
        }

        buildContract(descriptor.contract().services());
    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param node
     * @param dependency
     */
    public void recordResolvedDependency(BuildServiceNode<?> node, ServiceDependency dependency, @Nullable ServiceNode<?> resolvedTo, boolean fromParent) {
        requireNonNull(node);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<Entry<BuildServiceNode<?>, ServiceDependency>> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new SimpleImmutableEntry<>(node, dependency));

        if (node.autoRequires) {
            if (dependency.isOptional()) {
                optional.add(dependency.key());
            } else {
                required.add(dependency.key());
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void build(ArtifactBuildContext buildContext) {
        for (BuildServiceNode<?> e : nodes2) {
            if (!nodes.putIfAbsent(e)) {
                System.err.println("OOPS " + e.getKey());
            }
        }
        for (BuildServiceNodeExported<?> e : exportedNodes) {
            ServiceNode<?> sn = nodes.getRecursive(e.getKey());
            if (sn == null) {
                throw new IllegalStateException("Could not find node to export " + e.getKey());
            }
            e.exportOf = (ServiceNode) sn;
            exports.put(e);
        }
        DependencyGraph dg = new DependencyGraph(containerConfiguration, this);

        if (buildContext.isInstantiating()) {
            dg.instantiate();
        } else {
            dg.analyze();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void set(ComponentConfiguration cc, AtProvidesGroup apg) {
        BuildServiceNodeDefault sc;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).getInstance();
            sc = new BuildServiceNodeDefault(this, (InternalConfigSite) cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).getFactory();
            sc = new BuildServiceNodeDefault<>(this, cc, InstantiationMode.SINGLETON, containerConfiguration.lookup.readable(factory.function()),
                    (List) factory.dependencies());
        }

        sc.hasInstanceMembers = apg.hasInstanceMembers;
        // AtProvidesGroup has already validated that the specified type does not have any members that provide services with
        // the same key, so we can just add them now without any verification
        for (AtProvides member : apg.members.values()) {
            nodes2.add(sc.provide(member));// put them directly
        }
        cc.features().set(InjectorBuilder.FK, sc);
    }
}
