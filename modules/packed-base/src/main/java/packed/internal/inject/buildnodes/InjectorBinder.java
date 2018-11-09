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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import packed.internal.inject.InternalInjector;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.NodeMap;
import packed.internal.inject.buildnodes.DependecyCycleDetector.DependencyCycle;

public final class InjectorBinder {

    /** All nodes that have been added to this builder, even those that are not exposed. */
    final ArrayList<BuildNode<?>> buildNodes = new ArrayList<>();

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildNode<?>> detectCyclesFor;

    /** All nodes that have been exposed under a particular key */
    final NodeMap exposed = new NodeMap();

    final ArrayList<ImportFromInjector> injectorImport = new ArrayList<>();

    final InternalInjectorConfiguration c;

    public InjectorBinder(InternalInjectorConfiguration c) {
        this.c = requireNonNull(c);
    }

    public void addImportInjector(ImportFromInjector i) {
        requireNonNull(i);
        injectorImport.add(i);
    }

    public <T> BuildNode<T> addAndScan(BuildNode<T> node) {
        buildNodes.add(requireNonNull(node));

        // If we need to separate the scanning, just take a boolean in the method
        if (node instanceof BuildNode) {
            // BuildNodeInstanceOrFactory<?> scanNode = (BuildNodeInstanceOrFactory<?>) node;
            // if (scanNode.getMirror().methods().annotatedMethods() != null) {
            // for (AnnotationProvidesReflectionData pmm : scanNode.getMirror().methods().annotatedMethods().providesMethods()) {
            // throw new UnsupportedOperationException();
            // // Get original is gone
            // // BuildNodeProvidesMethod<Object> pm = new BuildNodeProvidesMethod<>(scanNode, pmm, scanNode.getOriginal());
            // // buildNodes.add(pm);
            // // pm.as(pm.getReturnType());
            // }
            // }
        }
        return node;
    }

    public Injector build() {
        InternalInjector injector = new InternalInjector(c);
        // listener().injectorBuilder_freeze(builder);
        // freezeBuilder();

        for (ImportFromInjector i : injectorImport) {
            for (BuildNode<?> n : i.m.values()) {
                buildNodes.add(n);
            }
        }

        InjectorBuilderResolver.resolveAllDependencies(this);

        dependencyCyclesDetect();
        instantiateAll(injector);

        return injector;
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
        ArrayDeque<BuildNode<?>> stack = new ArrayDeque<>();
        ArrayDeque<BuildNode<?>> dependencies = new ArrayDeque<>();
        for (BuildNode<?> node : detectCyclesFor) {
            if (!node.detectCycleVisited) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependecyCycleDetector.detectCycle(node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    /**
     * Instantiates all nodes.
     *
     * @throws RuntimeException
     *             if a node could not be instantiated
     */
    public void instantiateAll(InternalInjector injector) {
        for (BuildNode<?> node : buildNodes) {
            if (node instanceof BuildNodeFactorySingleton) {
                BuildNodeFactorySingleton<?> s = (BuildNodeFactorySingleton<?>) node;
                if (s.getBindingMode() == BindingMode.EAGER_SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }
        for (BuildNode<?> n : buildNodes) {
            if (n.getKey() != null) {
                injector.nodes.put(n.toRuntimeNode());
            }
        }
    }

}
