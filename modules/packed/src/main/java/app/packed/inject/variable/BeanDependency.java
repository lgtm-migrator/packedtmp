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
package app.packed.inject.variable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.base.Nullable;
import app.packed.base.Variable;
import app.packed.hooks.BeanClass;
import app.packed.hooks.BeanConstructor;
import app.packed.hooks.BeanField;
import app.packed.hooks.BeanMethod;
import app.packed.inject.Factory;
import app.packed.inject.Provider;

/**
 *
 */
// AutoVariable, AutoVar
// AutoService (Hvis service bliver lidt mere bredt)

// BeanDependency??? Tjahhh den kan jo ogsaa bruges for funktioner...
// Maaske er det saa en BeanFunction....
public abstract class BeanDependency {

    static void $nestWithClass(Class<? extends BeanClass> methodType) {}

    static void $nestWithMethod(Class<? extends BeanMethod> methodType) {}

    static void $nestWithField(Class<? extends BeanField> methodType) {}

    static void $nestWithConstructor(Class<? extends BeanConstructor> methodType) {}

    // VariablsActivatableHook

    

    //// Eller maaske har vi to typer

    /// Kunne vaere interessant mht til Wirelets... boo(FooWirelet w)
    // Men saa inject boo(Stuff<FooWirelet>) istedet for
    // String[] subClassOf() default {};
    // String[] exactType() default {};

    // Does not support lifecycle annotations...
    // only @ScopedProvide

    // VariableBinder, VariableProvider
    //// ContextualizedProvisional

    // 3 typer
    // * Constant | Missing
    
    // * Function
    // * @Contextualizable
    public interface VariableInjector {

        /**
         * 
         */
        // If DefaultValue try and fetch it.
        // Otherwise null if @Nullable or Optional.empty
        // Provider<@Nullable >
        default void missingValue() {
            // Must be a valid optional...
            // Det er tjekket inde
        }
        
        /**
         * Provides the same nullable constant to the variable at runtime.
         * 
         * @param constant
         *            the constant to provide to the variable
         * @throws ClassCastException
         *             if the type of the constant does not match the type of the variable
         */
        default void injectConstant(@Nullable Object constant) {
            injectVia(MethodHandles.constant(variable().getType(), constant));
        }

        /**
         * @param factory
         * 
         * @throws IllegalStateException
         *             if a bind method has already been called on this binder instance (I think it is fine to allow it to be
         *             overriden by itself)
         */
        void injectVia(Factory<?> factory);

        // Det return type of the method handle must match (be assignable to) variable.getType();
        void injectVia(MethodHandle methodHandle);

        void nextStep(BeanDependency instance);

        void nextStepSpawn(Class<? extends BeanDependency> implementation);

        // ------- Must have a single method annotated with @Provide, whose return type must match variable.getType()
        void nextStepSpawn(Factory<? extends BeanDependency> factory); // Den laver et objekt som kan bruge... IDK hvor spaendende det er

        /** {@return the variable that should be bound.} */
        Variable variable(); // IDK know about this
    }

    // Alle hooks kan bruge den
    interface ZookReplacer {

        void nextStep(Object instance);

        void nextStepSpawn(Class<?> implementation);

        // ------- Must have a single method annotated with @Provide, whose return type must match variable.getType()
        void nextStepSpawn(Factory<?> factory); // Den laver et objekt som kan bruge... IDK hvor spaendende det er

    }
}

class Zandbox {

    public interface InjectableVariableDescription {

        /**
         * If the variable is {@link #isOptional() optional} this method returns the optional class.
         * <p>
         * The optional class is currently one of {@link Optional}, {@link OptionalInt}, {@link OptionalDouble} or OptionalLong.
         * 
         * @return
         */
        Optional<Class<?>> getOptionalClass();

        // cannot both be nullable and have a default value
        // cannot both be optional and have a default value
        // if default value it is not required...
        boolean hasDefaultValue(); // maybe we can have a boostrap#setDefaultValue() (extract the string)

        boolean isNullable();

        boolean isOptional();

        /**
         * Wrapped in {@link Provider}.
         * 
         * @return
         */
        boolean isProvider();

        /**
         * @return
         * 
         * @see #isNullable()
         * @see #isOptional()
         * @see #hasDefaultValue()
         */
        boolean isRequired();
    }

    public @interface InjectableVariableHook {

        interface Stuff {

            Object getDefaultValue();

            boolean hasDefaultValue();

            /**
             * @return whether or not there is fallback mechanism for providing a value, for example, a default value
             */
            boolean hasFallback();

            /** {@return whether or not a Nullable annotation is present on the variable} */
            boolean isNullable();

            /**
             * @return whether or the variable is wrapped in an optional type
             * @see Optional
             * @see OptionalDouble
             * @see OptionalLong
             * @see OptionalInt
             */
            boolean isOptional();

            /** {@return whether or not the variable is wrapped in a Provider type} */
            boolean isProvider();

            default boolean isRequired() {
                return !isNullable() && !isOptional() && !hasFallback();
            }
        }

        enum TransformerSupport {
            COMPOSITE, // Any annotation that is called Nullable...
            CONVERSION, DEFAULTS, LAZY, NULLABLE,

            OPTIONAL, PROVIDER, VALIDATATION
        }
    }
}

//class Zarchive {
//
//    // This one is nice... maybe it is $debug Or maybe its like packed.extension.devmode=true
//    // Yeah I think we should put this in devtools
//    protected final void $debug() {
//        // configuration().debug();
//        // ExtensionDev.test();
//    }
//}
//Dynamic variables are dependencies that cannot statically be expressed as a key..
//F.eks. if you want to inject a system property @SystemProperty("doobar") has infinite many possibilities
//DynamicVariable to the rescue

//Can either be used with a sidecar 
//Or without a sidecar in which the context in which it is used must provide it.

//@SomePAnnotation cannot be used in this context.
//Throw new ProvisionException
//Look at the annotation to see which contexts it can be used in.

/// Optional...
//@DynamicVariable(supportOptional = true)

////Old Names
//Dom, DynVar, Prime, @ProvideDynamically...
//Produce instead of provide...
//ProvideSingle
//ProvidePrototype
//ProvideViaPrime() <--- is protoype
//-- Explicitly defined via a sidecar...
//-- Overriden in the some internals
//WildcardVariable <- Nah det har jo ikke noget med Generics at goere....

//>=2 Dynamic variable anntoations fail
//= 1 Dynamic variable ok
//= 0 ordinary service

//@DynVar(positional = 0, -1)

//@DynVar(@HtttpParamGet)