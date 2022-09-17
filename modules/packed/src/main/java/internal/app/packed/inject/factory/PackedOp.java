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
package internal.app.packed.inject.factory;

import app.packed.operation.op.CapturingOp;
import app.packed.operation.op.Op;

/**
 *
 */
@SuppressWarnings("rawtypes")
public abstract sealed class PackedOp<R> extends Op<R> permits InternalFactory, CapturingOp {

    
    // Knows the OpType

    //// Subtype
    // DelegatingOp
    // KnowMHOp
    // DontknowMH
    
}
