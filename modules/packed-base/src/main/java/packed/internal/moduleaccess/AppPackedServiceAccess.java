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
package packed.internal.moduleaccess;

import app.packed.service.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.inject.factoryhandle.FactorySupport;
import packed.internal.service.build.ServiceExtensionNode;

/** A support class for calling package private methods in the app.packed.service package. */
public interface AppPackedServiceAccess extends SecretAccess {

    /**
     * Extracts a handle from the specified factory
     * 
     * @param <T>
     *            the type of elements the factory produces
     * @param factory
     *            the factory to extract from
     * @return the handle
     */
    <T> FactorySupport<T> toSupport(Factory<T> factory);

    ServiceExtensionNode toNode(ServiceExtension e);
}
