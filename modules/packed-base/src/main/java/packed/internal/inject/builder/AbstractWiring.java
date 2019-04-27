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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import app.packed.bundle.Bundle;
import app.packed.bundle.UpstreamWiringOperation;
import app.packed.bundle.WiringOperation;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.annotations.AtProvides;
import packed.internal.bundle.BundleSupport;
import packed.internal.classscan.ImportExportDescriptor;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.InternalDependencyDescriptor;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceWiringImportOperation;

/**
 * An abstract class for the injector bind methods {@link InjectorConfigurator#wireInjector(Class, WiringOperation...)},
 * {@link InjectorConfigurator#wireInjector(Bundle, WiringOperation...)}, and
 * {@link InjectorConfigurator#wireInjector(Injector, UpstreamWiringOperation...)}.
 */
abstract class AbstractWiring {

    @Nullable
    final Bundle bundle;

    /** The configuration site of binding. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector that binding another bundle or injector. */
    final InjectorBuilder injectorConfiguration;

    /** The wiring operations. */
    final List<WiringOperation> operations;

    AbstractWiring(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Bundle bundle, List<WiringOperation> stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.operations = requireNonNull(stages);
        this.bundle = requireNonNull(bundle, "bundle is null");
    }

    AbstractWiring(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, List<WiringOperation> stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.operations = requireNonNull(stages);
        this.bundle = null;
    }

    /**
     * @param importableNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    void processImport(List<? extends ServiceNode<?>> importableNodes) {
        HashMap<Key<?>, ServiceBuildNodeImport<?>> nodes = new HashMap<>();
        for (ServiceNode<?> node : importableNodes) {
            if (!node.isPrivate()) {
                nodes.put(node.key(),
                        new ServiceBuildNodeImport<>(injectorConfiguration, configurationSite.replaceParent(node.configurationSite()), this, node));
            }
        }
        // Process each stage
        for (WiringOperation operation : operations) {
            if (operation instanceof UpstreamWiringOperation) {
                BundleSupport.invoke().startWireOperation(operation);
                nodes = processImportStage((UpstreamWiringOperation) operation, nodes);
                BundleSupport.invoke().finishWireOperation(operation);
            }
        }

        // Add all to the private node map
        for (ServiceBuildNodeImport<?> node : nodes.values()) {
            if (!injectorConfiguration.box.services().nodes.putIfAbsent(node)) {
                throw new InjectionException("oops for " + node.key()); // Tried to import a service with a key that was already present
            }
        }
    }

    private HashMap<Key<?>, ServiceBuildNodeImport<?>> processImportStage(UpstreamWiringOperation stage, HashMap<Key<?>, ServiceBuildNodeImport<?>> nodes) {
        // Find @Provides, lookup class

        ImportExportDescriptor ied = ImportExportDescriptor.from(BundleSupport.invoke().lookupFromWireOperation(stage), stage.getClass());

        for (AtProvides m : ied.provides.members.values()) {
            for (InternalDependencyDescriptor s : m.dependencies) {
                if (!nodes.containsKey(s.key())) {
                    throw new InjectionException("not good man, " + s.key() + " is not in the set of incoming services");
                }
            }
        }

        // Make runtime nodes....

        // System.out.println("Any provides " + !ied.provides.isEmpty());

        HashMap<Key<?>, ServiceBuildNodeImport<?>> newNodes = new HashMap<>();

        for (Iterator<ServiceBuildNodeImport<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
            ServiceBuildNodeImport<?> node = iterator.next();
            Key<?> existing = node.key();

            // invoke the import function on the stage
            if (stage instanceof ServiceWiringImportOperation) {
                ((ServiceWiringImportOperation) stage).onEachService(node);
            }

            if (node.key() == null) {
                iterator.remove();
            } else if (!node.key().equals(existing)) {
                iterator.remove();
                // TODO check if a node is already present
                newNodes.put(node.key(), node); // Should make new, with new configuration site
            }
        }
        // Put all remaining nodes in newNodes;
        newNodes.putAll(nodes);
        return newNodes;
    }

}
