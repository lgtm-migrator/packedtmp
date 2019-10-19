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
package packed.internal.service.build.dependencies;

import static java.util.Objects.requireNonNull;

import app.packed.config.ConfigSite;
import app.packed.lang.Nullable;
import app.packed.service.Dependency;
import packed.internal.inject.hooks.AtInject;
import packed.internal.service.build.BuildEntry;

/**
 *
 */
class DependencyRequirement {

    final ConfigSite configSite;

    final Dependency dependency;

    @Nullable
    final BuildEntry<?> entry;

    AtInject atInject;
    // Contract <- If requirement added via a contract

    DependencyRequirement(Dependency dependency, ConfigSite configSite) {
        this.dependency = requireNonNull(dependency, "dependency is null");
        this.configSite = requireNonNull(configSite);
        this.entry = null;
    }

    DependencyRequirement(Dependency dependency, BuildEntry<?> entry) {
        this.dependency = requireNonNull(dependency, "dependency is null");
        this.configSite = entry.configSite();
        this.entry = entry;
    }

    DependencyRequirement(Dependency dependency, BuildEntry<?> entry, AtInject atInject) {
        this.dependency = requireNonNull(dependency, "dependency is null");
        this.configSite = entry.configSite();
        this.entry = entry;
        this.atInject = requireNonNull(atInject);
    }
}
