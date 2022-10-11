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
package app.packed.bean;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.ExtensionDescriptor;
import app.packed.container.InternalExtensionException;
import app.packed.operation.BindingMirror;
import app.packed.operation.InvocationType;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.IntrospectorOnField;
import internal.app.packed.bean.IntrospectorOnMethod;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.binding.PackedOnBindingHook;

/**
 * @see Extension#newBeanIntrospector
 */
public abstract class BeanIntrospector {
    
    /**
     * The configuration of this processor. Is initially null but populated via
     * {@link #initialize(ExtensionDescriptor, BeanSetup)}.
     */
    @Nullable
    private Setup setup;

    /** {@return an annotation reader for for the bean.} */
    public final AnnotationReader beanAnnotations() {
        // AnnotationReader.of(beanClass());
        throw new UnsupportedOperationException();
    }

    public final Class<?> beanClass() {
        return setup().bean.beanClass();
    }

    /**
     * @param postFix
     *            the message to include in the final message
     * 
     * @throws BeanDefinitionException
     *             always thrown
     */
    public final void failWith(String postFix) {
        throw new BeanDefinitionException("OOPS " + postFix);
    }

    /**
     * Invoked by a MethodHandle from ExtensionSetup.
     * 
     * @param extension
     *            the extension that owns the scanner
     * @param bean
     *            the bean we are scanning
     * @throws IllegalStateException
     *             if called more than once
     */
    final void initialize(ExtensionSetup operator, BeanSetup bean) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = new Setup(operator.model, bean);
    }

    public void onBindingHook(OnBindingHook dependency) {}

    public void onClassHook(OnClassHook clazz) {}

    /**
     * A callback method that is called for fields that are annotated with a field hook annotation defined by the extension:
     * 
     * is annotated with an annotation that itself is annotated with {@link FieldHook} and where
     * {@link FieldHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a given field and extension. Even if there are multiple matching hook
     * annotations on the same field.
     * 
     * @param field
     *            the bean field
     * @see BeanExtensionPoint.FieldHook
     */
    // onFieldHook(Set<Class<? extends Annotation<>> hooks, BeanField));
    public void onFieldHook(OnFieldHook field) {
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle field annotation(s) " + field.hooks());
    }

    public void onMethodHook(OnMethod method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle method annotation(s) " + method.hooks());
    }

    /**
     * A callback method that is called after any other methods on this class.
     * <p>
     * This method can be used to provide final validation or registration of the bean.
     * <p>
     * If an exception is thrown at any time doing processing of the bean this method will not be called.
     * 
     * @see #onPreIntrospect()
     */
    public void onPostIntrospect() {}

    /**
     * A callback method that is invoked before any calls to {@link #onClassHook(OnClassHook)},
     * {@link #onFieldHook(OnFieldHook)}, {@link #onMethodHook(OnMethod)} or {@link #onBindingHook(OnBindingHook)}.
     * <p>
     * This method can be used to setup data structures or perform validation.
     * 
     * @see #onPostIntrospect()
     */
    public void onPreIntrospect() {}

    public final Class<? extends Extension<?>> registrant() {
        // Ideen er at vi kan checke at vi selv er registranten...
        throw new UnsupportedOperationException();
    }

    /**
     * {@return the internal configuration class.}
     * 
     * @throws IllegalStateException
     *             if called from the constructor of the class
     */
    private Setup setup() {
        Setup s = setup;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }

    /**
     * An annotation reader can be used to process annotations on bean elements.
     * 
     * @see AnnotatedElement
     */
    // If we can, we should move this to BeanProcessor.AnnotationReader
    // Maybe BeanAnnotationReader? Don't think we will use it elsewhere?
    // AnnotatedBeanElement?
    public interface AnnotationReader {

        /** {@return whether or not there are any annotations to read.} */
        boolean hasAnnotations();

        boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

        // Det er taenk
        Annotation[] readAnyOf(Class<?>... annotationTypes);

        /**
         * Returns a annotation of the specified type or throws {@link BeanDefinitionException} if the annotation is not present
         * 
         * @param <T>
         *            the type of the annotation to query for and return if present
         * @param annotationClass
         *            the Class object corresponding to the annotation type
         * @return the annotation for the specified annotation type if present
         * 
         * @throws BeanDefinitionException
         *             if the specified annotation is not present or the annotation is a repeatable annotation and there are not
         *             exactly 1 occurrences of it
         * 
         * @see AnnotatedElement#getAnnotation(Class)
         */
        //// foo bean was expected method to dddoooo to be annotated with
        <T extends Annotation> T readRequired(Class<T> annotationClass);

        // Q) Skal vi bruge den udefra beans???
        // A) Nej vil ikke mene vi beskaeftiger os med andre ting hvor vi laeser det.
        // Altsaa hvad med @Composite??? Det er jo ikke en bean, det bliver noedt til at vaere fake metoder...
        // Paa hver bean som bruger den...
        // Vi exponere den jo ikke, saa kan jo ogsaa bare bruge den...

        // I think the only we reason we call it BeanAnnotationReader is because
        // if we called AnnotationReader is should really be located in a utility package
    }

    /**
     *
     */
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RUNTIME)
    @Documented
    // Move to BeanExtensionPoint
    public @interface BindingHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowAllAccess() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface FieldHook {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet() default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    /**
     *
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface MethodHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanIntrospector.OnMethod#operationBuilder(ExtensionBeanConfiguration)} and... will fail with
         * {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanIntrospector.OnMethod#operationBuilder(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    /**
    *
    */
    // Eller ogsaa peeler vi inde vi kalder provide

    // Med alle de andre bean ting. Saa har vi en BeanField->Operation
    // Skal vi have noget lige saadan her BeanDependency->Provisioning
    // eller BeanVariable -> Dependency???
    // Saa kan vi strippe af paa BeanVariable
    // Saa bliver BeanVariable

    // Can be on the bean. Or on a composite.
    public sealed interface OnBindingHook permits PackedOnBindingHook {

        // Hmm idk about the unwrapping and stuff here
        AnnotationReader annotations();

        /**
         * Binds the specified value to the parameter.
         * <p>
         * Vi tager Nullable med saa vi bruge raw.
         * <p>
         * Tror vi smider et eller andet hvis vi er normal og man angiver null. Kan kun bruges for raw
         * 
         * @param value
         *            the value to bind
         * @throws ClassCastException
         *             if the type of the value does not match the underlying parameter
         * @throws IllegalStateException
         *             if a binding has already been created for the underlying parameter.
         */
        void bind(@Nullable Object value);

        /**
         * <p>
         * For raw er det automatisk en fejl
         * 
         * @throws Giver ikke mening for rawModel
         */
        void bindEmpty();

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws BeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new BeanDefinitionException("OOPS " + postFix);
        }

        /**
         * @return
         * 
         * @throws UnsupportedOperationException
         *             if called from {@link OperationHandle#bindings()}
         */
        Class<?> hookClass(); // Skal vel ogsaa tilfoejes til BF, BM osv

        // Kan only do this if is invoking extension!!
        default OnBindingHook connectInvocationArgument(int index) {
            // Kan jo faktisk ogsaa bruges med context?
            return connectInvocationArgument(index, index);
        }
        
        default OnBindingHook connectInvocationArgument(int index, int operationIndex) {
            // Used from ops.
            return this;
        }
        /** {@return the extension that is responsible for invoking the underlying operation.} */
        Class<? extends Extension<?>> invokingExtension();

        void provide(MethodHandle methodHandle);

        void provide(Op<?> fac);

        /**
         * @param index
         *            the index of the argument
         * 
         * @throws IndexOutOfBoundsException
         * @throws IllegalArgumentException
         *             if the invocation argument is not of kind {@link InvocationType.ArgumentKind#ARGUMENT}
         * @throws UnsupportedOperationException
         *             if the {@link #invokingExtension()} is not identical to the binding extension
         * @throws ClassCastException
         * 
         * @see InvocationType
         */
        void provideFromInvocationArgument(int index);

        /**
         * @return
         * 
         * @throws BeanDefinitionException
         *             if the variable was a proper key
         */
        default Key<?> readKey() {
            throw new UnsupportedOperationException();
        }

        /**
         * Variable is resolvable at runtime.
         * <p>
         * Cannot provide instance. Must provide an optional class or Null will represent a missing value. Maybe just optional
         * class for now
         * 
         * @return
         */
        OnBindingHook runtimeBindable();

        OnBindingHook specializeMirror(Supplier<? extends BindingMirror> supplier);

        default TypeInfo type() {
            throw new UnsupportedOperationException();
        }

        Variable variable();

        interface TypeInfo {

            void checkAssignable(Class<?> clazz, Class<?>... additionalClazzes);

            boolean isAssignable(Class<?> clazz, Class<?>... additionalClazzes);

            Class<?> rawType();
        }
    }

    // CheckRealmIsApplication
    // CheckRealmIsExtension
    /**
     *
     * <p>
     * Members from the {@code java.lang.Object} class are never returned.
     */

    // Kig maaske i Maurizio Mirror thingy...
    public interface OnClassHook {

        void forEachConstructor(Consumer<? super OnConstructorHook> m);

        void forEachMethod(Consumer<? super OnMethod> m);

        boolean hasFullAccess();

        // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
        // Masske har vi BeanClass.Builder() istedet for???

        // Cute men vi gider ikke supportere det
//       static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
//           throw new UnsupportedOperationException();
//       }

        // Fields first, include subclasses, ... blabla
        // Maybe on top of full access have boolean custom processing on ClassHook
        void setProcessingStrategy(Object strategy);
    }

    /**
     * This class represents a {@link Constructor} on a bean.
     * <p>
     * Unlike field and methods hooks. There are no way to define hooks on constructors. Instead they must be defined on a
     * bean driver or a bean class. Which determines how constructors are processed.
     */
    // Do we need a BeanExecutable??? Not sure we have a use case
    // Or maybe we just have BeanMethod (Problem with constructor() though)
    public interface OnConstructorHook {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();

        /**
         * Returns the modifiers of the constructor.
         * 
         * @return the modifiers of the constructor
         * @see Constructor#getModifiers()
         * @apiNote the method is named getModifiers instead of modifiers to be consistent with
         *          {@link Constructor#getModifiers()}
         */
        int getModifiers();

        OperationHandle newOperation();

        /** {@return a factory type for this method.} */
        OperationType operationType();
    }

    /**
     * This class represents a {@link Field} on a bean.
     * 
     * @see BeanExtensionPoint.FieldHook
     * @see BeanIntrospector#onField(BeanProcessor$BeanField)
     * 
     * @apiNote There are currently no support for obtaining a {@link VarHandle} for a field.
     */
    public sealed interface OnFieldHook permits IntrospectorOnField {

        /** {@return an annotation reader for the field.} */
        AnnotationReader annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws BeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new BeanDefinitionException("OOPS " + postFix);
        }

        /** {@return the underlying field.} */
        Field field();

        /**
         * Attempts to convert field to a {@link Key} or fails by throwing {@link BeanDefinitionException} if the field does not
         * represent a proper key.
         * <p>
         * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
         * key. As a binding hook is typically used in cases where this would be needed.
         * 
         * @return a key representing the field
         * 
         * @throws BeanDefinitionException
         *             if the field does not represent a proper key
         */
        default Key<?> fieldToKey() {
            return Key.convertField(field());
        }

        /**
         * @return
         */
        default Set<Class<?>> hooks() {
            return Set.of();
        }

        /**
         * {@return the modifiers of the field.}
         * 
         * @see Field#getModifiers()
         * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Field#getModifiers()}
         */
        int modifiers();

        /**
         * Creates a new operation that reads the field as specified by {@link Lookup#unreflectGetter(Field)}.
         * 
         * @param operator
         *            the bean that will invoke the operation. The operator must be defined in the same container (or in a
         *            parent container) as the bean that declares the field
         * @return an operation handle
         * @throws IllegalArgumentException
         *             if the specified operator is not a direct ancestor of the bean that declares the field
         */
        OperationHandle newGetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType);

        /**
         * @param operator
         * @param accessMode
         *            the access mode determines the operation type as exposed via the mirror api. However, extensions are free
         *            to use any access mode they want
         * @return
         * 
         * @see VarHandle#toMethodHandle(java.lang.invoke.VarHandle.AccessMode)
         * 
         * @apiNote there are currently no way to create more than 1 MethodHandle or VarHandle per operation. If this is needed
         *          at some point. We could take a varargs of access modes and then allow repeat calls to methodHandleNow. No
         *          matter what we must declare the invocation types when we create the operation, so we can check access before
         *          creating the actual operation
         */
        OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, VarHandle.AccessMode accessMode, InvocationType invocationType);

        /**
         * Creates a new operation that writes a field as specified by {@link Lookup#unreflectSetter(Field)}.
         * 
         * @return an operation configuration object
         */
        OperationHandle newSetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType);

        /**
         * {@return the underlying field represented as a {@code Variable}.}
         * 
         * @see Variable#ofField(Field)
         */
        Variable variable(); // mayby toVariable (kun hvis den fejler taenker jeg)
    }

    /**
     * This class represents a {@link Method} on a bean.
     * 
     */
    public sealed interface OnMethod permits IntrospectorOnMethod {

        /** {@return an annotation reader for the method.} */
        AnnotationReader annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws BeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new BeanDefinitionException("OOPS " + postFix);
        }

        /**
         * {@return the modifiers of the underlying method.}
         *
         * @see Method#getModifiers()
         * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
         */
        int getModifiers();

        /**
         * @return
         */
        boolean hasInvokeAccess();

        /**
         * @return
         */
        default Set<Class<?>> hooks() {
            return Set.of();
        }

        /** {@return the underlying method.} */
        Method method();

        /**
         * Attempts to convert field to a {@link Key} or fails by throwing {@link BeanDefinitionException} if the field does not
         * represent a proper key.
         * <p>
         * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
         * key. As a binding hook is typically used in cases where this would be needed.
         * 
         * @return a key representing the field
         * 
         * @throws BeanDefinitionException
         *             if the field does not represent a proper key
         */
        default Key<?> methodToKey() {
            return Key.convertMethodReturnType(method());
        }

        /**
         * Creates a new operation that can invoke the underlying method.
         * <p>
         * If an {@link OperationMirror} is created for this operation. It will report
         * {@link OperationTargetMirror.OfMethodInvoke} as its {@link OperationMirror#target()}.
         * 
         * @param operator
         *            the extension bean that will invoke the operation. The extension bean must be located in the same (or in a
         *            direct ancestor) container as the bean that declares the method.
         * @param invocationType
         * @return an operation handle
         * 
         * @see Lookup#unreflect(Method)
         * @see BeanMethodHook#allowInvoke()
         * @see BeanClassHook#allowAllAccess()
         * 
         * @throws IllegalArgumentException
         *             if the specified operator is not in the same container as (or a direct ancestor of) the method's bean.
         */
        OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType);

        /** {@return a operation type for this method.} */
        OperationType operationType();

        //
        //// Support for raw methods handles???
        /// **
        // * Returns a direct method handle to the {@link #method()} (without any intervening argument bindings or
        //// transformations
        // * that may have been configured elsewhere).
        // *
        // * @return a direct method handle to the underlying method
        // * @see Lookup#unreflect(Method)
        // * @see BeanMethodHook#allowInvoke()
        // * @see BeanClassHook#allowAllAccess()
        // *
        // * @throws UnsupportedOperationException
        // * if invocation access has not been granted via {@link BeanMethodHook#allowInvoke()} or
        // * BeanClassHook#allowAllAccess()
        // */
    }

    /** A small utility record to hold the both the extension model and the bean in one field. */
    // Replace with Introspector???
    private record Setup(ExtensionDescriptor extension, BeanSetup bean) {}
}
