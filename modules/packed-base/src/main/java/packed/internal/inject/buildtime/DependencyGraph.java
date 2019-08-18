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

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceDependency;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.annotations.ServiceClassDescriptor;
import packed.internal.inject.buildtime.DependencyGraphCycleDetector.DependencyCycle;
import packed.internal.inject.runtime.DefaultInjector;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.util.KeyBuilder;

public final class DependencyGraph {

    static final ServiceClassDescriptor INJ = ServiceClassDescriptor.from(MethodHandles.lookup(), DefaultInjector.class);

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildServiceNode<?>> detectCyclesFor;

    /** The root injector builder. */
    final PackedContainerConfiguration root;

    final InjectorBuilder ib;

    /**
     * Creates a new dependency graph.
     * 
     * @param root
     *            the root injector builder
     */
    public DependencyGraph(PackedContainerConfiguration root, InjectorBuilder ib) {
        this.root = requireNonNull(root);
        this.ib = requireNonNull(ib);
    }

    /** Also used for descriptors. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void analyze() {
        ib.privateInjector = new DefaultInjector(root, ib.nodes);
        BuildServiceNodeDefault d = new BuildServiceNodeDefault<>(ib, root.configSite(), ib.privateInjector);
        d.as(KeyBuilder.INJECTOR_KEY);
        ib.nodes.put(d);
        // TODO replace with something a.la.
        // dcc.source.isInjectorConfigurator
        if (root.buildContext().artifactType() == Injector.class) {
            ib.publicInjector = ib.privateInjector;
        } else {
            ib.publicInjector = new DefaultInjector(root, ib.exports);

            // Add public injector
            // bn = new BuildNodeInstance<>(c, configSite.UNKNOWN, c.publicInjector);
            // bn.as(Injector.class);
            // c.public BuildNodeList.add(bn);

        }

        // if (ib.injectorBundleBindings != null) {
        // for (BindInjectorFromBundle bi : ib.injectorBundleBindings) {
        // bi.processExport();
        // // new DependencyGraph(bi.newConfiguration).instantiate();
        // }
        // }

        // If we do not export services into a bundle. We should be able to resolver much quicker..
        resolveAllDependencies(this);
        dependencyCyclesDetect();
    }

    /**
     * Tries to find a dependency cycle.
     *
     * @throws InjectionException
     *             if a dependency cycle was detected
     */
    public void dependencyCyclesDetect() {
        DependencyCycle c = dependencyCyclesFind();
        if (c != null) {
            throw new InjectionException("Dependency cycle detected: " + c);
        }
    }

    DependencyCycle dependencyCyclesFind() {
        if (detectCyclesFor == null) {
            throw new IllegalStateException("Must resolve nodes before detecting cycles");
        }
        ArrayDeque<BuildServiceNode<?>> stack = new ArrayDeque<>();
        ArrayDeque<BuildServiceNode<?>> dependencies = new ArrayDeque<>();
        for (BuildServiceNode<?> node : detectCyclesFor) {
            if (!node.detectCycleVisited) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependencyGraphCycleDetector.detectCycle(node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    public void instantiate() {
        analyze();

        // Instantiate all singletons
        // System.out.println(root.box.services().exports);

        for (ServiceNode<?> node : ib.nodes) {
            if (node instanceof BuildServiceNodeDefault) {
                BuildServiceNodeDefault<?> s = (BuildServiceNodeDefault<?>) node;
                if (s.instantiationMode() == InstantiationMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }

        // Okay we are finished, convert all nodes to runtime nodes.
        ib.nodes.toRuntimeNodes();
        if (ib.nodes != ib.exports) {
            ib.exports.toRuntimeNodes();
        }
    }

    // Requirements -> cannot require any exposed services, or internally registered services...

    static void resolveAllDependencies(DependencyGraph graph) {
        graph.detectCyclesFor = new ArrayList<>();

        InjectorBuilder services = graph.ib;

        for (ServiceNode<?> nn : services.nodes) {
            BuildServiceNode<?> node = (BuildServiceNode<?>) nn;

            if (node.needsResolving()) {
                graph.detectCyclesFor.add(node);
                List<InternalDependencyDescriptor> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    ServiceDependency dependency = dependencies.get(i);
                    ServiceNode<?> resolveTo = services.nodes.getNode(dependency);
                    services.recordResolvedDependency(node, dependency, resolveTo, false);
                    node.resolvedDependencies[i] = resolveTo;
                }
            }
        }
        services.checkForMissingDependencies();
        // b.root.privateNodeMap.forEach(n -> ((ServiceBuildNode<?>) n).checkResolved());
    }
}

// All exposures
// if (builder.publicNodeList != null) {
// for (BuildNode<?> bn : builder.publicNodeList) {
// if (bn instanceof BuildNodeExposed) {
// BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
// Node<?> node = builder.privateNodeMap.getRecursive(bne. .getPrivateKey());
// bne.exposureOf = requireNonNull((Node) node, "Could not find private key " + bne.getPrivateKey());
// }
// }
// }
