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
package packed.internal.service.runtime;

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.ProvidePrototypeContext;
import packed.internal.service.buildtime.ServiceMode;

/** An entry holding a constant. */
public final class ConstantInjectorEntry<T> extends RuntimeEntry<T> {

    /** The singleton instance. */
    @Nullable
    private final T constant;

    /**
     * @param configSite
     * @param key
     */
    public ConstantInjectorEntry(ConfigSite configSite, Key<T> key, @Nullable T instance) {
        super(configSite, key);
        this.constant = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvidePrototypeContext ignore) {
        return constant;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.CONSTANT;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
