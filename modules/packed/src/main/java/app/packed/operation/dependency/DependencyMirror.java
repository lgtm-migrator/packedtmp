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
package app.packed.operation.dependency;

import java.util.Optional;

import app.packed.application.Realm;
import app.packed.operation.OperationMirror;

/**
 * A mirror for a dependency.
 */
// Hmm den burde vel passe til BeanInjector
public interface DependencyMirror {

    DependencyGraphMirror graph();
    
    /** {@return the operation the dependency belongs to.} */
    // Hvad med unresolved... Tror vi skal fejle
    OperationMirror operation();
    
    Optional<Realm> providedBy();
    
    Optional<OperationMirror> providingOperation();
    
    boolean isResolved();
    
    boolean isSatisfiable();
}
