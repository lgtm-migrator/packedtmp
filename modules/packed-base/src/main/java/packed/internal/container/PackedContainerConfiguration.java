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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.Factory;
import packed.internal.component.ComponentNodeConfiguration;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** The context to delegate all calls to. */
    private final PackedContainerRole context;

    private final ComponentNodeConfiguration node;

    public PackedContainerConfiguration(PackedContainerRole context) {
        super(context.node);
        this.context = context;
        this.node = context.node;
    }

    /** {@inheritDoc} */
    @Override
    public <W extends Wirelet> Optional<W> assemblyWirelet(Class<W> type) {
        return node.assemblyWirelet(type);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        return context.extensions();
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Class<T> implementation) {
        return install(Factory.find(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return node.install(factory);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        return node.installInstance(instance);
    }

    /** {@inheritDoc} */
    @Override
    public StatelessConfiguration installStateless(Class<?> implementation) {
        return node.installStateless(implementation);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isArtifactRoot() {
        return node.depth() == 0;// not sure this is correct
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        node.realm().lookup(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setDescription(String description) {
        node.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setName(String name) {
        node.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return context.use(extensionType);
    }
}
