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

import java.util.ArrayList;
import java.util.List;

import app.packed.bundle.Bundle;
import app.packed.bundle.ContainerBuildContext;
import app.packed.bundle.DownstreamWiringOperation;
import app.packed.bundle.WiringOperation;
import app.packed.util.Key;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNode;

/**
 *
 */
final class BindInjectorFromBundle extends AbstractWiring {

    final InjectorBuilder newConfiguration;

    BindInjectorFromBundle(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Bundle bundle, List<WiringOperation> stages) {
        super(injectorConfiguration, configurationSite, bundle, stages);
        this.newConfiguration = new InjectorBuilder(configurationSite, bundle);
    }

    /**
     * 
     */
    void processImport() {
        ContainerBuildContext bs = new ContainerBuildContext() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T with(Class<? super T> type) {
                if (type == InjectorBuilder.class) {
                    return (T) newConfiguration;
                }
                return super.with(type);
            }
        };
        bs.configure(bundle);
        // BundleSupport.invoke().configureInjectorBundle(bundle, newConfiguration, true);
        processImport(newConfiguration.publicNodeList);
    }

    void processExport() {
        for (WiringOperation s : operations) {
            if (s instanceof DownstreamWiringOperation) {
                throw new UnsupportedOperationException();
            }
        }
        List<ServiceBuildNodeExport<?>> exports = new ArrayList<>();
        if (newConfiguration.box.services().required != null) {
            for (Key<?> k : newConfiguration.box.services().required) {
                if (newConfiguration.privateNodeMap.containsKey(k)) {
                    throw new RuntimeException("OOPS already there " + k);
                }
                ServiceNode<?> node = injectorConfiguration.privateNodeMap.getRecursive(k);
                if (node == null) {
                    throw new RuntimeException("OOPS " + k);
                }
                ServiceBuildNodeExport<?> e = new ServiceBuildNodeExport<>(newConfiguration, configurationSite.replaceParent(node.configurationSite()), this,
                        node);
                exports.add(e);
                newConfiguration.privateNodeMap.put(e);
            }
        }
    }
}
