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
package internal.app.packed.operation.binding;

import java.util.function.Supplier;

import app.packed.container.User;
import app.packed.framework.Nullable;
import app.packed.operation.BindingMirror;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;

/**
 * A binding to a constant.
 */
// Tror maaske vi skal have en MH adaptor som kan wrappe den i Optional osv.
public final class OldConstantBindingSetup extends BindingSetup {

    /** Non-null if bound by an application. */
    @Nullable
    private final ExtensionSetup boundBy;

    /** The constant that was bound. */
    @Nullable
    private final Object constant;


    public OldConstantBindingSetup(OperationSetup operation, @Nullable ExtensionSetup boundBy, int index, Object constant,
            Supplier<? extends BindingMirror> specializedMirror) {
        super(operation, index, boundBy == null ? User.application() : User.extension(boundBy.extensionType));
        this.boundBy = boundBy;
        this.constant = constant;
        this.mirrorSupplier = specializedMirror;
    }

}
//
///** {@inheritDoc} */
//@Override
//public MethodHandle read() {
//  MethodHandle mh = MethodHandles.constant(constant == null ? Object.class : constant.getClass(), constant);
//  return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
//}