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

import app.packed.application.BuildException;
import app.packed.bean.CustomBeanHook;

/**
 * An assembly that delegates all calls to another assembly.
 * <p>
 * A typical use case for using a delegating assembly is to hide methods on the original assembly. Or to configure the
 * assembly, for example, in test scenarios where you want to specify an assembly class in an annotation.
 * <p>
 * Delegating assemblies cannot use the {@link AssemblyHook} annotation or {@link CustomBeanHook custom bean hooks}.
 * Attempting to use any of these annotations on a delegating assembly will result in a {@link BuildException} being
 * thrown.
 */
public non-sealed abstract class DelegatingAssembly extends Assembly {

    /** {@return the assembly to delegate to.} */
    protected abstract Assembly delegateTo();
}


//// Kan jo saa argumentere for hvorfor man ikke kan specificere wirelets for alle assemblies????
//// Altsaa det ikke mening kun at give tilladelse til at have den her
//// Kunne endda have en instans metode paa Assembly....
//static Assembly wireWith(Assembly assembly, Wirelet... wirelets) {
//    return assembly;
//}