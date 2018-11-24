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

import java.util.List;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import packed.internal.inject.Node;
import packed.internal.inject.runtimenodes.RuntimeServiceNode;
import packed.internal.inject.runtimenodes.RuntimeServiceNodeAlias;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A build node that is created when a service is exposed.
 */
public final class BuildNodeExposed<T> extends BuildNode<T> {

    // Exposed vs imported....
    // Maaske skal vi have to forskellige, det taenker jeg

    Node<T> exposureOf;

    final Key<T> privateKey;

    /**
     * @param configuration
     *            the injector configuration this node is being added to
     * @param configurationSite
     *            the configuration site of the exposure
     */
    public BuildNodeExposed(InternalInjectorConfiguration configuration, InternalConfigurationSite configurationSite, Key<T> privateKey) {
        super(configuration, configurationSite, List.of());
        this.privateKey = requireNonNull(privateKey, "privateKey is null");
    }

    @Override
    @Nullable
    BuildNode<?> declaringNode() {
        return (exposureOf instanceof BuildNode) ? ((BuildNode<?>) exposureOf).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return exposureOf.getBindingMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return null;
    }

    public Key<T> getPrivateKey() {
        return privateKey;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    RuntimeServiceNode<T> newRuntimeNode() {
        return new RuntimeServiceNodeAlias<>(this, exposureOf);
    }
}
