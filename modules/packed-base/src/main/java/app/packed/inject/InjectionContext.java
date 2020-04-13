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

import java.util.Set;

import app.packed.base.Key;

/**
 * An injection context object can be injected into any injection site (typically a method or constructor) to query
 * about what dependencies are available for injection at the particular injection site.
 * <p>
 * Example
 * <p>
 * May change from type to type even when registered in the same container.
 * <p>
 * we both need to check the type and the qualifier which must be readable by #visibility
 * 
 * @apiNote In the future, if the Java language permits, {@link InjectionContext} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface InjectionContext {

    /**
     * An immutable set of keys for which dependencies are available for injection at the injection site (typically a method
     * or constructor annotated with {@link Inject}).
     * <p>
     * Note: Even though The set of keys returned by this method does not take into consideration that depending on them
     * will lead to cycles in the dependency graph.
     * 
     * @return an immutable set of keys that are available for injection at the injection site
     */
    Set<Key<?>> keys();

    /**
     * Returns the class that was used to determined which keys are available for injection.
     * 
     * For example, a service that is registered with a package private {@link Key}. Will only be in the set of keys
     * returned by {@link #keys()} if the class returned by this method is in the same package.
     * <p>
     * Normally the declaring class of the constructor or method to who this context is injected into is returned. However,
     * if interface is used from a composite class. The class that depends on the composite will be returned.
     * 
     * @return the class for which we calculate visibility
     */
    // contextTarget target()
    // Eller kan man lave noget hullumhej her ved at lade publish nogle composite klasser, med package private
    // constructors...
    // Og som tager hemmelige "klasser".. Nej man skal ikke kunne instantiere en eller anden composite, som man ikke har
    // adgang til... Men

    // rename to targetClass?
    Class<?> target(); // Det er jo bare .getClass(); Med mindre det er en Composite...
}

// Something about layers....
// If you have 100 services.... And a Method hook adds 3 services...
// There can be really difficult to find...
// So some kind of layering... Maybe it can be feed back to service layer thingy.
// Because it is kind of layer which can only be used from the particular service.

// Set<Class<? extends Annotation>> Factory.primers()
//// Primers can be resolved in relationship to a container
//// Primers can use normal optional/nullable/composite stuff... or they can ignore it... The contract is really
// A textual one, and not a programmatic one....
// Maybe you can opt.. support Optional, Nullable, NotLazy, Provider, Not Composite
// Paa PrimeProvider
// No validation is performed because we can always override the default.... So there is just no way to do it....

// So Factory -> Set<Key<?>> requirements, Set<Key<?>> optionalRequirements, Set<Class<?>> primers()... 

/// ---------------
// Map<String, String> filtered(); A map of all filtered types.... For example, because there are not visible...
// Value is description...

//Er det bare en Sidecar der tager en InjectionRuntime+ComponentContext???
//ComponentContext... Skal ogsaa styres via Access.. @InjectContext(needs ComponentContext)...
//Naah, component burde da vaere fint... Den kan alle faa

//Nej den er speciel... Alle kan supportere den paa en eller anden maade...

//Man laver vel en skabelon naar man gerne vil finde bruge den...

//Vi ved ikke noget om primeannotation

//Inject context kender ikke noget til prime annotations.
//De styrer deres eget loeb, men det anbefales at de overholder f.eks. @Nullable

//En InjectContext vil aldrig dukke op som et requirement
// Can we cheat with Composite here? I think maybe....
// Not first priority though...
//// Det ville ikke fungere super godt hvis vi bruger en sidecar...

// Altsaa der er jo nogen gange hvor ting ikke er tilgaengelig for en constructor. Men kun en metode??

//@Stuff Set<Key<?>> thisIsThing
//Provided via @Stuff + InjectionContext -> ic.keys();
///// -----------
// Vi kunne jo tit godt taenke os at fortaelle en historie...
// Du kan bruge den her service fordi du er annoteret med @Actor...
//// ConfigSite med @Actor som parent???