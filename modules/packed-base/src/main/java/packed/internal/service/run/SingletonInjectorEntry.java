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
package packed.internal.service.run;

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.service.ProvideContext;
import packed.internal.service.build.BuildEntry;

/** An injector entry holding a {@link InstantiationMode#SINGLETON} instance. */
// Can't implement both ServiceDescriptor and Provider...
public final class SingletonInjectorEntry<T> extends InjectorEntry<T> {

    /** The singleton instance. */
    @Nullable
    private final T instance;

    /**
     * Creates a new node.
     *
     * @param buildNode
     *            the node to create this node from
     * @param instance
     *            the singleton instance
     */
    public SingletonInjectorEntry(BuildEntry<T> buildNode, @Nullable T instance) {
        super(buildNode);
        this.instance = requireNonNull(instance);
    }

    /**
     * @param configSite
     * @param key
     * @param description
     */
    public SingletonInjectorEntry(ConfigSite configSite, Key<T> key, @Nullable String description, @Nullable T instance) {
        super(configSite, key, description);
        this.instance = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideContext ignore) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
