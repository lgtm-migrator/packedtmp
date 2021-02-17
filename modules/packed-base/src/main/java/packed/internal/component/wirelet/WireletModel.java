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
package packed.internal.component.wirelet;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;

public final class WireletModel {

    /** A cache of models for {@link Wirelet} subclasses. */
    private static final ClassValue<WireletModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked" })
        @Override
        protected WireletModel computeValue(Class<?> type) {
            return new WireletModel((Class<? extends Wirelet>) type);
        }
    };

    @Nullable
    final Class<? extends Wirelet> stackBy;

    final boolean buildtimeOnly;

    /**
     * Create a new wirelet model.
     * 
     * @param type
     *            the wirelet type
     */
    private WireletModel(Class<? extends Wirelet> type) {
        WireletPreModel m = WireletPreModel.consume(type);
        this.stackBy = m.stackBy;
        this.buildtimeOnly = m.buildtimeOnly;
    }

    /**
     * Returns a model for the specified wirelet type.
     * 
     * @param wireletType
     *            the wirelet type to return a model for.
     * @return the model
     */
    public static WireletModel of(Class<? extends Wirelet> wireletType) {
        return MODELS.get(wireletType);
    }
}
