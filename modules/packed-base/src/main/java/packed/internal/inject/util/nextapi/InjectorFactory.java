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
package packed.internal.inject.util.nextapi;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Set;

import app.packed.inject.ComponentServiceConfiguration;
import app.packed.inject.Injector;
import app.packed.inject.Provide;
import app.packed.util.Key;

/**
 *
 */
class InjectorFactory {
    // Can we create a AppFactory as well???
    // Maaske skal man lave et bundle, som har required services...

    // Man burde ogsaa kunne lave nogle assisted inject factories....
    // Hvor man kan lave en instance af X

    // Istedet for settet skal vi have saadan en modifiere som vi bruger andre steder
    protected void addNewInstanceLookup(MethodHandles.Lookup lookup) {

    }

    // ConfigSource is disabled per default
    protected <T> ComponentServiceConfiguration<T> registerInjectorFactory(Class<T> injectorFactory, Set<Key<?>> keysToInheritFromParent) {
        // key = class, description = Interface factory for xxxxx
        throw new UnsupportedOperationException();
    }

    protected void registerInjectorFactory(Class<?> injectorFactory) {
        registerInjectorFactory(IFactory.class); // <--- IFactory can be injected like a normal service...
    }

    // the factory is spawned from internal services
    // Problemer er maaske at ikke kan definere endnu et injector factory level uden at angive en bundle....

    // Problemet er vel at vi kun kan klare en extra level
    interface IFactory {
        Injector a(Request request, Response response);

        Injector b(Request request, Response response);

        // Parameters can override existing services
        Injector b(Request request, Response response, String overridesExistingStringService);

        Somebody c(Request request, Response response);

        // Fails if Request is not available for each method
        @Provide
        default LocalDate now(Request request) {
            throw new UnsupportedOperationException();
        }
    }

    // Alternativ have et interface eller lignende for f.eks. AbstractHost, InjectorFactory
    // Men okay, saa kan vi ikke angive parametere...

    interface Request {}

    interface Response {}

    interface Somebody {}

    // Maaske kan vi registerere det her med en host ogsaa...
    // F.eks. i henhold til Sessions. Hvor vi registererer dem som apps.
}

interface InjectorFactory2 {
    // Tager disse to objekter, laver en injector fra bundlen.
    // Og saa outputter String, hvor str1 og str2 er dependencies....
    String spawn(long str1, int str2);

    Injector spawn(String httpRequest, String httpResponse);
}

interface UserDefinedSpawner {
    // App spawn(Host h, String httpRequest, String httpResponse);
}

// We do not put service contract on a running object, because it does not work very good.
/// **
// * Returns a service contract
// *
// * @return
// */
//// Hmm, vi extender jo maaske den her klasse, og vil returnere en MultiContract
// default ServiceContract contract() {
// // Injector.serviceContractOf(Injector i);
// Injector.contractOf(Injector i); -> contractOf(i).services();
// // O
// throw new UnsupportedOperationException();
// }

// default void print() {
// services().forEach(s -> System.out.println(s));
// }
/// **
// * Creates a new injector builder with this injector with all the services available in this injector also be
/// available
// * in the new injector. To override existing service in this injector use {@link #spawn(Consumer, Predicate)} and
/// remove
// * the service you do not want via a filter Put on Injectors???
// *
// * @return a new injector builder
// */
// default Injector spawnInjector(Consumer<? super InjectorConfiguration> configurator) {
// // Nej vil sgu have det så man let kan overskrive dem.
// // Bliver noedt til at have noget special support.
// // Maa smide dem alle ind i
//

// spawnFiltered(Predicate<? extends ServiceDescriptor);

// // Would also be nice with a filtered injector, but spawn(c->{}, filter) will do it for now
// requireNonNull(configurator, "configurator is null");
// return Injector.of(c -> {
// c.importInjector(this);
// configurator.accept(c);
// });
// }
