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
package packed.internal.inject.build.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.WireletList;
import app.packed.feature.FeatureKey;
import app.packed.inject.Factory;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.Key;
import packed.internal.container.CoreComponentConfiguration;
import packed.internal.container.FactoryComponentConfiguration;
import packed.internal.container.InstantiatedComponentConfiguration;
import packed.internal.inject.InjectorConfigSiteOperations;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.ErrorMessages;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.run.AbstractInjector;

/**
 *
 */
public final class ServiceProvidingManager {

    /** A that is used to store parent nodes */
    private static FeatureKey<ComponentBuildEntry<?>> FK = new FeatureKey<>() {};

    /** All provided nodes. */
    public final ArrayList<BuildEntry<?>> entries = new ArrayList<>();

    public final ArrayList<ProvideAllFromInjector> provideAll = new ArrayList<>(0);

    final InjectorBuilder builder;

    /**
     * @param injectorBuilder
     */
    public ServiceProvidingManager(InjectorBuilder injectorBuilder) {
        this.builder = requireNonNull(injectorBuilder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void onProvidesGroup(ComponentConfiguration cc, AtProvidesGroup apg) {
        ComponentBuildEntry parentNode;
        if (cc instanceof InstantiatedComponentConfiguration) {
            Object instance = ((InstantiatedComponentConfiguration) cc).instance;
            parentNode = new ComponentBuildEntry(builder, cc.configSite(), instance);
        } else {
            Factory<?> factory = ((FactoryComponentConfiguration) cc).factory;
            MethodHandle mh = builder.pcc.lookup.toMethodHandle(factory.handle());
            parentNode = new ComponentBuildEntry<>(builder, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }

        // If any of the @Provide methods are instance members the parent node needs special treatment.
        // As it needs to be constructed, before the field or method can provide services.
        parentNode.hasInstanceMembers = apg.hasInstanceMembers;

        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : apg.members) {
            ConfigSite configSite = parentNode.configSite().thenAnnotatedMember(InjectorConfigSiteOperations.INJECTOR_PROVIDE, atProvides.provides,
                    atProvides.member);
            ComponentBuildEntry<?> node = new ComponentBuildEntry<>(configSite, atProvides, atProvides.methodHandle, parentNode);
            node.as((Key) atProvides.key);
            entries.add(node);
        }

        // Set the parent node, so it can be found from provideFactory or provideInstance
        cc.features().set(FK, parentNode);
    }

    public void provideAll(AbstractInjector injector, ConfigSite confitSite, WireletList wirelets) {
        provideAll.add(new ProvideAllFromInjector(builder, confitSite, injector, wirelets));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideFactory(ComponentConfiguration cc, Factory<T> factory, FactoryHandle<T> function) {
        ComponentBuildEntry<?> c = cc.features().get(FK);
        if (c == null) {
            // config site???
            MethodHandle mh = builder.pcc.lookup.toMethodHandle(function);
            c = new ComponentBuildEntry<>(builder, cc, InstantiationMode.SINGLETON, mh, (List) factory.dependencies());
        }
        c.as((Key) factory.key());
        entries.add(c);
        return new PackedProvidedComponentConfiguration<>(cc, (ComponentBuildEntry) c);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provideInstance(ComponentConfiguration cc, T instance) {
        // First see if we have already installed the node. This happens in #set if the component container any members
        // annotated with @Provides
        ComponentBuildEntry<?> node = cc.features().get(FK);

        if (node == null) {
            // No node found, components has no @Provides method, create a new node
            node = new ComponentBuildEntry<T>(builder, cc.configSite(), instance);
        }

        node.as((Key) Key.of(instance.getClass()));
        entries.add(node);
        return new PackedProvidedComponentConfiguration<>((CoreComponentConfiguration) cc, (ComponentBuildEntry) node);
    }

    public boolean processNodesAndCheckForDublicates(ArtifactBuildContext buildContext) {
        HashMap<Key<?>, BuildEntry<?>> uniqueNodes = new HashMap<>();
        LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> duplicateNodes = new LinkedHashMap<>(); // preserve order for error message

        processNodesAndCheckForDublicates0(uniqueNodes, duplicateNodes, entries);
        for (ProvideAllFromInjector ii : provideAll) {
            processNodesAndCheckForDublicates0(uniqueNodes, duplicateNodes, ii.entries.values());
        }

        // Add error messages if any nodes with the same key have been added multiple times
        if (!duplicateNodes.isEmpty()) {
            ErrorMessages.addDuplicateNodes(buildContext, duplicateNodes);
        }
        builder.resolvedEntries.addAll(uniqueNodes.values());
        return !duplicateNodes.isEmpty();
    }

    private void processNodesAndCheckForDublicates0(HashMap<Key<?>, BuildEntry<?>> uniqueNodes,
            LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> duplicateNodes, Iterable<? extends BuildEntry<?>> nodes) {
        for (BuildEntry<?> node : nodes) {
            Key<?> key = node.key();
            if (key != null) {
                BuildEntry<?> existing = uniqueNodes.putIfAbsent(key, node);
                if (existing != null) {
                    HashSet<BuildEntry<?>> hs = duplicateNodes.computeIfAbsent(key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                    hs.add(node);
                }
            }
        }
    }
}
