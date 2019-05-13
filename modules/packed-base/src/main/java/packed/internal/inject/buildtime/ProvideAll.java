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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import app.packed.bundle.WiringOption;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.util.Key;
import packed.internal.annotations.AtProvides;
import packed.internal.bundle.AppPackedBundleSupport;
import packed.internal.classscan.ImportExportDescriptor;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.inject.InternalDependencyDescriptor;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceWiringImportOperation;
import packed.internal.inject.runtime.AbstractInjector;

/** Provides services from an existing Injector. */
public final class ProvideAll extends AbstractFreezableNode {

    /** The injector we are providing services from. */
    private final Injector injector;

    /** The configuration of the injector that binding another bundle or injector. */
    final ContainerBuilder injectorConfiguration;

    /** The wiring options used when creating this configuration. */
    final List<WiringOption> options;

    public ProvideAll(ContainerBuilder configuration, Injector injector, WiringOption... operations) {
        super(configuration.configurationSite().spawnStack(ConfigurationSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND));
        this.injectorConfiguration = requireNonNull(configuration);
        this.injector = requireNonNull(injector, "injector is null");
        this.options = List.of(requireNonNull(operations, "operations is null"));
    }

    public void process() {
        List<ServiceNode<?>> nodes;

        if (injector instanceof AbstractInjector) {
            nodes = ((AbstractInjector) injector).copyNodes();
        } else {
            throw new IllegalArgumentException("Currently only Injectors created by Packed are supported");
        }

        processImport(nodes);
    }

    /**
     * @param externalNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    private void processImport(List<? extends ServiceNode<?>> externalNodes) {

        // First create service build nodes for every existing node
        HashMap<Key<?>, BuildtimeServiceNode<?>> nodes = new HashMap<>();
        for (ServiceNode<?> node : externalNodes) {
            if (!node.isPrivate()) {
                BuildtimeServiceNodeProvideAll<?> n = new BuildtimeServiceNodeProvideAll<>(injectorConfiguration,
                        configurationSite.replaceParent(node.configurationSite()), this, node);
                nodes.put(node.key(), n);
            }
        }

        // Process each wiring operation
        for (WiringOption operation : options) {
            if (operation instanceof WiringOption) {
                AppPackedBundleSupport.invoke().startWireOperation(operation);
                nodes = processImportStage(operation, nodes);
                AppPackedBundleSupport.invoke().finishWireOperation(operation);
                throw new Error();
            }
        }

        // Add all to the private node map
        for (BuildtimeServiceNode<?> node : nodes.values()) {
            if (!injectorConfiguration.box.services().nodes.putIfAbsent(node)) {
                throw new InjectionException("oops for " + node.key()); // Tried to import a service with a key that was already present
            }
        }
    }

    private HashMap<Key<?>, BuildtimeServiceNode<?>> processImportStage(WiringOption stage, HashMap<Key<?>, BuildtimeServiceNode<?>> nodes) {
        ImportExportDescriptor ied = ImportExportDescriptor.from(AppPackedBundleSupport.invoke().lookupFromWireOperation(stage), stage.getClass());

        for (AtProvides m : ied.provides.members.values()) {
            for (InternalDependencyDescriptor s : m.dependencies) {
                if (!nodes.containsKey(s.key())) {
                    throw new InjectionException("not good man, " + s.key() + " is not in the set of incoming services");
                }
            }
        }

        // Make runtime nodes....

        HashMap<Key<?>, BuildtimeServiceNode<?>> newNodes = new HashMap<>();

        for (Iterator<BuildtimeServiceNode<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
            BuildtimeServiceNode<?> node = iterator.next();
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
