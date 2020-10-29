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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.Provider;
import app.packed.inject.ProvisionContext;
import app.packed.inject.ServiceLocator;
import packed.internal.inject.PackedProvisionContext;
import packed.internal.inject.service.build.ServiceBuild;

/** An entry that represents a service at runtime. */
public abstract class RuntimeService extends AbstractService {

    /** The point where this entry was registered. */
    private final ConfigSite configSite;

    /** The key under which the service is available. */
    private final Key<?> key;

    RuntimeService(ConfigSite configSite, Key<?> key) {
        this.configSite = requireNonNull(configSite);
        this.key = requireNonNull(key);
    }

    /**
     * Creates a new runtime node from a build entry.
     *
     * @param buildEntry
     *            the build node to create the runtime node from
     */
    RuntimeService(ServiceBuild buildEntry) {
        this(buildEntry.configSite(), buildEntry.key());
    }

    public final ConfigSite configSite() {
        return configSite;
    }

    @Override
    public <T> AbstractService decorate(Function<? super T, ? extends T> decoratingFunction) {
        throw new UnsupportedOperationException();
    }

    // We need this to adapt to build time transformations
    public abstract MethodHandle dependencyAccessor();

    /**
     * Returns an instance.
     * 
     * @param request
     *            a request if needed by {@link #requiresPrototypeRequest()}
     * @return the instance
     */
    public abstract Object getInstance(@Nullable ProvisionContext request);

    Object getInstanceForLocator(ServiceLocator locator) {
        ProvisionContext pc = PackedProvisionContext.of(key);
        Object t = getInstance(pc);
        return t;
    }

    Provider<?> getProviderForLocator(ServiceLocator locator) {
        if (isConstant()) {
            Object t = getInstanceForLocator(locator);
            return Provider.ofConstant(t);
        } else {
            ProvisionContext pc = PackedProvisionContext.of(key);
            return new ServiceWrapperProvider<Object>(this, pc);
        }
    }

    @Override
    public abstract boolean isConstant();

    /** {@inheritDoc} */
    @Override
    public final Key<?> key() {
        return key;
    }

    @Override
    public AbstractService rekeyAs(Key<?> key) {
        return new DelegatingRuntimeService(this, key);
    }

    public abstract boolean requiresPrototypeRequest();

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key());
        sb.append("[isConstant=").append(isConstant()).append(']');
        return sb.toString();
    }
}
