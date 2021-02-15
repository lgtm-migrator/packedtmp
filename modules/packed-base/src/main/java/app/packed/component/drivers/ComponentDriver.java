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
package app.packed.component.drivers;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import app.packed.base.TypeToken;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;
import packed.internal.component.PackedComponentDriver;

/**
 * Component drivers are responsible for configuring and creating new components. They are rarely created by end-users.
 * And it is possible to use Packed without every being directly exposed to component drivers.
 * Instead users would normally used some of the predifined used drivers such as ....
 * <p>
 * 
 * Every time you, for example, call {@link BaseAssembly#install(Class)} it actually de
 * 
 * install a
 * 
 * @param <C>
 *            the type of configuration this driver exposes
 * 
 * @apiNote In the future, if the Java language permits, {@link ComponentDriver} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ComponentDriver<C extends ComponentConfiguration> {

    /**
     * Returns the set of modifiers that will be applied to the component.
     * <p>
     * The runtime may add additional modifiers once the component is wired.
     * 
     * @return the set of modifiers that will be applied to the component
     */
    ComponentModifierSet modifiers();

    default ComponentDriverDescriptor descriptor() {
        // Grunden til vi splitter det op er egenlig at vi tillader private
        // component drivere. Saa hvis vi gerne vil have 
        // ComponentDriverDescriptor Component.driver();
        // og ikke Component.driver();
        throw new UnsupportedOperationException();
    }
    
    // Er det prefixes?? eller specifike ting...
    // SCD.with(Wirelet.Name("sdfsdff?")) // kunne ogsaa vaere configuration...    
    default ComponentDriver<C> with(Wirelet wirelet) {
        throw new UnsupportedOperationException();
    }
    
    default ComponentDriver<C> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @param <C>
     *            the type of configuration that is returned to the user when the driver is wired
     * @param lookup
     *            a lookup object that must have access to invoke the constructor of the specified configuration type
     * @param configurationType
     *            the type of configuration that should be returned to the user when the component is wired
     * @param options
     *            optional options
     * @return a component driver
     */
    static <C extends ComponentConfiguration> ComponentDriver<C> of(MethodHandles.Lookup lookup, Class<? extends C> configurationType, Option... options) {
        return PackedComponentDriver.of(lookup, configurationType, options);
    }

    static <C extends ComponentConfiguration> ComponentDriver<C> of(MethodHandles.Lookup lookup, TypeToken<? extends C> configurationType, Option... options) {
        throw new UnsupportedOperationException();
    }

    // Tror jeg er mere til et builder approach her..
    // Hvis det begynder at blive komplekts er det altsaa lidt lettere at overskue...
    // Saa Builder er driver type...
    // Og saa maa vi leve med lidt redundency
    // Eller ogsaa maa vi bide the bullet og faa en ComponentSourceDriver
    // Det gode ved options, er man kun har en metode.
    // Det daarlige er at det alt andet lige er lidt mere forvirrende

    /**
     * @apiNote In the future, if the Java language permits, {@link ArtifactDriver} may become a {@code sealed} interface,
     *          which would prohibit subclassing except by explicitly permitted types.
     */
    public interface Option {

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         * @see ComponentModifier#BUNDLE_ROOT
         */
        static Option bundle() {
            return PackedComponentDriver.OptionImpl.BUNDLE;
        }

        /**
         * The component the driver will be a container.
         * <p>
         * A container that is a component cannot be sourced??? Yes It can... It can be the actor system
         * 
         * @return stuff
         * @see ComponentModifier#CONSTANT
         */
        // InstanceComponentDriver automatically sets the source...
        static Option constantSource() {
            return PackedComponentDriver.OptionImpl.CONSTANT;
        }

        static Option sourceAssignableTo(Class<?> rawType) {
            throw new UnsupportedOperationException();
        }

        static Option statelessSource() {
            return PackedComponentDriver.OptionImpl.STATELESS;
        }

        static Option validateParent(Predicate<? super Component> validator, String msg) {
            return validateWiring((c, d) -> {
                if (validator.test(c)) {
                    throw new IllegalArgumentException(msg);
                }
            });
        }

        static Option validateParentIsContainer() {
            return validateParent(c -> c.hasModifier(ComponentModifier.BUNDLE_ROOT), "This component can only be wired to a container");
        }

        // The parent + the driver
        //

        /**
         * Returns an option that
         * 
         * @param validator
         * @return the option
         */
        // Hmm integration with vaildation
        static Option validateWiring(BiConsumer<Component, ComponentDriver<?>> validator) {
            throw new UnsupportedOperationException();
        }

        // Option serviceable()
        // Hmm Maaske er alle serviceable.. Og man maa bare lade vaere
        // at expose funktionaliteten.
    }
}
//TODO maybe remove all methods... And have attributes???
//And just retain implementations for internal usage...

//TODO vi kunne expose noget om hvad den laver den her driver...
//Saa kan man lave checks via ContainerConfigurationContext
//sourceModel.doo().hasAnnotation(Actor.class);
//Error handling top->down and then as a static bundle method as last resort.
//The bundle XX.... defines a non-static error handler method. But it was never installed

//S spawn();
//CompletableFuture<S> spawnAsync();

//Stateless, Statefull, DistributedObject, Entity <-

//Requestlets, Scopelets, ...

//Charactariska = How Many Instances, Managaged/ Unmanaged, Dynamic-wire (host)

//Wirelets for components??????? Nej ikke udo
//install(Doo.class, name("fsdsfd"), description("weweqw));

//install(Role, implementation, wirelets);

//Bundle.setDefaultRole <- On Runtime.
//F.eks. Actor .withRole(Actor)

//Role -> Pool [5-25 instance, timeout 1 minute]

//I role skulle man kun installere en slags controller...

//Install

//setMaxInstances();

//Role-> PrototypeOptionas. Its a prototype of

//I think there are extra settings on prototype...
//Such as caching...
//Because they are unthogonal, lazy has nothing todo with actors.

//But about runtime hotswap, for example, for actors...
//We kind of swap the type...

//We have a special component implementation for that...

//COMPONENT_DRIVEr definere ingen drivere selv..
//Skal den vaere here eller paa ComponentDriver????
//Syntes egentlig ikke den er tilknyttet ComponentDriver...
//Men hvis folk selv definere for custom defineret vil det maaske give mening.
//At smide dem paa configurationen... Der er jo ingen

//En anden meget positiv ting er at vi vi har 2 component drivere
//sourced and unsourced. People shouldn't really need to look at
//both of the classes two find what they need..

//interface InstanDriver<T, C>

/**
 *
 *
 * @param <C>
 *            the type of configuration that will be returned to the user
 */
//Noget med scanning....

//Det er jo fucking genialt...
//Det betyder folk kan lave deres egne "component" systemer...

//Vi supporter kun en surragate taenker jeg
//Vi supportere ogsaa kun klasse scanning paa registrering tidspunkt.
//Ikke frit dynamisk taenker jeg. 

//Altsaa er det her maaden at lave prototype service paa???

//Vi vil forresten gerne have en SingletonContext der ogsaa fungere for Funktioner med registrere...
//Nej de har jo ikke en type....
