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

import app.packed.inject.Provider;
import app.packed.inject.ProvisionContext;

/**
 *
 */
public class NonConstantLocatorProvider<T> implements Provider<T> {

    final RuntimeService<T> service;

    final ProvisionContext ppc;

    NonConstantLocatorProvider(RuntimeService<T> service, ProvisionContext ppc) {
        this.service = requireNonNull(service);
        this.ppc = requireNonNull(ppc);
    }

    /** {@inheritDoc} */
    @Override
    public T provide() {
        return service.getInstance(ppc);
    }
}
