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
package packed.internal.service.buildtime.dependencies;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.service.buildtime.service.ExportedBuildEntry;

/**
 * An error manager is lazily created when an the configuration of an injection manager fails
 */
public class InjectionErrorManager {

    /** A map of multiple exports of the same key. */
    @Nullable
    final LinkedHashMap<Key<?>, LinkedHashSet<ExportedBuildEntry<?>>> failingDuplicateExports = new LinkedHashMap<>();

    /** A map of all keyed exports where an entry matching the key could not be found. */
    @Nullable
    final LinkedHashMap<Key<?>, LinkedHashSet<ExportedBuildEntry<?>>> failingUnresolvedKeyedExports = new LinkedHashMap<>();

}
