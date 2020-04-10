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
package packed.internal.component;

import app.packed.component.ComponentType;
import app.packed.component.Stateless;
import packed.internal.artifact.PackedInstantiationContext;

/**
 *
 */
final class PackedStatelessComponent extends AbstractComponent implements Stateless {

    private final Class<?> type;

    PackedStatelessComponent(AbstractComponent parent, PackedStatelessComponentConfiguration configuration, PackedInstantiationContext ic) {
        super(parent, configuration, ic);
        this.type = configuration.definition();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> definition() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentType type() {
        return ComponentType.STATELESS;
    }
}
