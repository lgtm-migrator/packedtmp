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
package app.packed.hook2;

/**
 *
 */
// Ideen er lidt at alle der har behov for at dekorere metoder, bliver registereret.
// En bruger kan saa vaelge at registerer en AopResolver eller Consumer<AopResolver>
// Hvor man kan saette den exact raekkefoelger.
// Maybe just AOPInvocationManager, AOPInvoker
// Smid den .aop

/// AOP er 100% treebased....
/// Forstaaet paa den maade at en parent definere strategien...
/// Og saa kan et child evt. refinere den...

// Maaske kan en extension checke om en given AOP annotering er tilstede.
// HVis vi altsaa beslutter at den explicit skal vaere tilstede paa en bundle...

// Men det behover den vel ikke....

/// Taenker at alle der vil bruge AOP, maa bruge AopExtension...
/// Her kan man ogsaa praecist styre raekkefoelgen..
/// Tilfoeje debugging o.s.v.

class AopResolver {

}
