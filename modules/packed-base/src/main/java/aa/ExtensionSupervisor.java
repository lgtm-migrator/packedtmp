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
package aa;

import java.util.List;

import a.ExtensionWiring;
import app.packed.container.extension.Extension;
import app.packed.util.Nullable;

/**
 *
 */

/// Ved ikke omm vi behoever den her
/// Ideen er vi bygger en graf op, som man kan query omkring extensions..

// interessante ting->
// Lowest common ancessors
// Noder med hul i

public abstract class ExtensionSupervisor<E extends Extension> {

    @Nullable
    protected final <T> T findHostedSidecar(Class<T> sidecarType) {
        throw new UnsupportedOperationException();
    }

    // Maaske er det et interface, saa det kun er hvis man bruger wirelets...
    public abstract ExtensionWiring<E> newWiring();

    // start
    // ForEachNode
    // stopch

    interface Node<E extends Extension> {

        E node();

        List<?> wirelets();

        @Nullable
        E parent();

    }
}
