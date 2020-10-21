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
package app.packed.inject;

import java.util.function.Consumer;

import app.packed.component.Wirelet;

/**
 * This class provide various wirelets that can be used to transform and filter services being pull and pushed into
 * containers.
 */
public final class ServiceWirelets {

    /** No instantiation. */
    private ServiceWirelets() {}

    public static Wirelet to(Consumer<? super ServiceTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet from(Consumer<? super ServiceTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    // restrict contract
    // provide <-- easy access
}
