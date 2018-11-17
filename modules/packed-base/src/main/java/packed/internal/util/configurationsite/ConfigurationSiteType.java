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
package packed.internal.util.configurationsite;

import static java.util.Objects.requireNonNull;

/**
 *
 */
public enum ConfigurationSiteType {

    /** */
    INJECTOR_BIND("Injector.bind"),

    /** */
    INJECTOR_IMPORT_FROM("Injector.importFrom"),

    /** */
    INJECTOR_IMPORT_SERVICE("Injector.importService"),

    /** */
    INJECTOR_OF("Injector.of"),

    /** */
    DESCRIPTOR_OF("Descriptor.of");

    final String f;

    ConfigurationSiteType(String f) {
        this.f = requireNonNull(f);
    }

    public String operation() {
        return f;
    }

    @Override
    public String toString() {
        return f;
    }
}
