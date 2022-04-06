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
package app.packed.component;

import java.util.List;

import app.packed.bean.operation.OperationMirror;

/**
 *
 */
// Do we ever return empty trees? Det tror jeg ikke.

// Fx en Lifetime er jo altid et component tree...

// TreeView

//// TreeView<ComponentMirror>
//// TreeView<ContainerMirror>
//// TreeView<T extends Extension<?>>
public interface ComponentMirrorTree extends ComponentMirrorSet {
    ComponentMirror root(); // Optional<CM> if we have empty trees.

    default List<OperationMirror> operations() {
        throw new UnsupportedOperationException();
    }
}
// interface ComponentMirrorSet
