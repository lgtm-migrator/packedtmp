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
package packed.internal.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import packed.internal.service.buildtime.ServiceAssembly;
import packed.internal.service.buildtime.ExportedServiceAssembly;

/**
 * An error manager is lazily created when an the configuration of an injection manager fails
 */
public class InjectionErrorManager {

    /** A map of multiple exports of the same key. */
    final LinkedHashMap<Key<?>, LinkedHashSet<ExportedServiceAssembly<?>>> failingDuplicateExports = new LinkedHashMap<>();

    /** A map of all keyed exports where an entry matching the key could not be found. */
    final LinkedHashMap<Key<?>, LinkedHashSet<ExportedServiceAssembly<?>>> failingUnresolvedKeyedExports = new LinkedHashMap<>();

    /** A map of build entries that provide services with the same key. */
    final LinkedHashMap<Key<?>, LinkedHashSet<ServiceAssembly<?>>> failingDuplicateProviders = new LinkedHashMap<>();
}
