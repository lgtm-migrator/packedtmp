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
package app.packed.container;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.Mirror;

/** A mirror of an {@link Assembly}. */
// What about delegating assemblies
public sealed interface AssemblyMirror extends Mirror permits AssemblySetup.BuildtimeAssemblyMirror {

    /** {@return the application this assembly contributes to.} */
    ApplicationMirror application();

    /** {@return the assembly class.} */
    Class<? extends Assembly> assemblyClass();

    /**
     * {@return a stream of any child assemblies defined by this assembly.}
     * 
     * @see ContainerConfiguration#link(Assembly, Wirelet...)
     */
    Stream<AssemblyMirror> children(); // should be aligned with everyone else

    /** {@return the container that is defined by this assembly.} */
    ContainerMirror container();

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    // present on ContainerMirror as well? Maybe a ContainerHookMirror, I really think it should be
    List<Class<? extends AssemblyHook>> hooks();

    /** @return whether or not this assembly defines the root container in the application.} */
    boolean isRoot();

    /**
     * {@return the parent of this assembly, or empty if the assembly defines the root container of the application.}
     */
    Optional<AssemblyMirror> parent();
}
