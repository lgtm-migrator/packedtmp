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
package packed.internal.sidecar;

import app.packed.sidecar.MethodSidecar;
import packed.internal.lifecycle.LifecycleDefinition;

/**
 *
 */
// Naar man har en Function sidecar... Laver vi i virkeligheden en MethodSidecarModel 
// Maaske har vi kun en faelles ClassDataSidecar...

// Uniqueness -> [SidecarType_X_MethodType_X_Name] 
//MethodType kan maaske paa et tidspunkt bliver genericfiet..
// Jamen saa skal vi jo bruge generiske typer... Optional<String> er forskellige for Optional<Long>
// Selvom method typerne ender ens...
/** A model of {@link MethodSidecar}. */
public final class MethodSidecarModel extends SidecarModel {

    /** Meta data about the extension sidecar. */
    private static final SidecarTypeMeta STM = new SidecarTypeMeta(MethodSidecar.class, LifecycleDefinition.of(MethodSidecar.INSTANTIATION));

    /** The instance id into {@link ClassInstance#data}. */
    final int instanceId;

    // descriptorId.. enten det eller ogsaa binder vi den til methodhandle...

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder used to construct the model
     */
    protected MethodSidecarModel(Builder builder) {
        super(builder);
        this.instanceId = builder.instanceId;
        // MethodHandles.insertArguments(target, pos, values)
    }

    public static class Builder extends SidecarModel.Builder {

        int instanceId;

        /**
         * @param sidecarType
         */
        public Builder(Class<?> sidecarType) {
            super(sidecarType, STM);
        }
    }
}
