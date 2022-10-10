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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.Option;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.operation.Op;
import internal.app.packed.bean.BeanProps.InstallerOption.CustomIntrospector;
import internal.app.packed.bean.BeanProps.InstallerOption.CustomPrefix;
import internal.app.packed.bean.BeanProps.InstallerOption.NonUnique;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.operation.op.PackedOp;

/** Implementation of BeanHandle.Builder. */
public record BeanProps(
        
        /** The kind of bean. */
        BeanKind kind,

        /** The bean class, is typical void.class for functional beans. */
        Class<?> beanClass,

        /** The type of source the installer is created from. */
        BeanSourceKind sourceKind,

        /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
        @Nullable Object source,

        /** A model of hooks on the bean class. Or null if no member scanning was performed. */
        @Nullable BeanClassModel beanModel,

        /** The operator of the bean. */
        ExtensionSetup operator,

        RealmSetup realm,

        @Nullable ExtensionSetup extensionOwner,

        /** A custom bean introspector that may be set via {@link #introspectWith(BeanIntrospector)}. */
        @Nullable BeanIntrospector customIntrospector,

        @Nullable String namePrefix,

        boolean nonUnique) {

    private static <T> BeanHandle<T> install(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Class<?> beanClass,
            BeanKind kind, BeanSourceKind sourceKind, @Nullable Object source, BeanHandle.Option... options) {
        boolean nonUnique = false;
        BeanIntrospector customIntrospector = null;
        String namePrefix = null;
        BeanClassModel beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanClassModel(beanClass);
        requireNonNull(options, "options is null");
        for (Option o : options) {
            requireNonNull(o, "option was null");
            InstallerOption io = (InstallerOption) o;
            io.validate(kind);
            if (io instanceof InstallerOption.CustomIntrospector ci) {
                customIntrospector = ci.introspector;
            } else if (io instanceof InstallerOption.CustomPrefix cp) {
                namePrefix = cp.prefix;
            } else {
                nonUnique = true;
            }

        }
        BeanProps bp = new BeanProps(kind, beanClass, sourceKind, source, beanModel, operator, realm, extensionOwner, customIntrospector, namePrefix,
                nonUnique);
        
        realm.wireCurrentComponent();

        BeanSetup bean = new BeanSetup(realm, bp);

        // bean.initName

        // Scan the bean class for annotations unless the bean class is void or scanning is disabled
        if (sourceKind != BeanSourceKind.NONE) {
            new Introspector(bean, bp.customIntrospector).introspect();
        }

        return new PackedBeanHandle<>(bean);
    }

    public static <T> BeanHandle<T> installClass(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind kind,
            Class<T> clazz, BeanHandle.Option... options) {
        requireNonNull(clazz, "clazz is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return install(operator, realm, extensionOwner, clazz, kind, BeanSourceKind.CLASS, clazz, options);
    }

    public static BeanHandle<?> installFunctional(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner,
            BeanHandle.Option... options) {
        return install(operator, realm, extensionOwner, void.class, BeanKind.FUNCTIONAL, BeanSourceKind.NONE, null, options);
    }

    public static <T> BeanHandle<T> installInstance(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, T instance,
            BeanHandle.Option... options) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Op.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }

        // Optional is also not valid
        // or Provider, Lazy, ect
        // Ved heller ikke DependencyProvided beans

        // TODO check kind
        // cannot be operation, managed or unmanaged, Functional
        return install(operator, realm, extensionOwner, instance.getClass(), BeanKind.CONTAINER, BeanSourceKind.INSTANCE, instance, options);
    }

    public static <T> BeanHandle<T> installOp(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, BeanKind kind, Op<T> op,
            BeanHandle.Option... options) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        PackedOp<T> pop = PackedOp.crack(op);
        return install(operator, realm, extensionOwner, pop.type().returnType(), kind, BeanSourceKind.OP, pop, options);
    }

    // Eclipse requires permits here.. Compiler bug
    public sealed interface InstallerOption extends BeanHandle.Option permits NonUnique, CustomIntrospector, CustomPrefix {

        static final InstallerOption NON_UNIQUE = new NonUnique();

        default void validate(BeanKind kind) {}

        public record NonUnique() implements InstallerOption {

            /** {@inheritDoc} */
            @Override
            public void validate(BeanKind kind) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional beans");
                }
            }
        }

        public record CustomIntrospector(BeanIntrospector introspector) implements InstallerOption {

            public CustomIntrospector

            {
                requireNonNull(introspector, "introspector is null");
            }

            /** {@inheritDoc} */
            @Override
            public void validate(BeanKind kind) {
                if (!kind.hasInstances()) {
                    throw new IllegalArgumentException("NonUnique cannot be used with functional beans");
                }
            }
        }

        public record CustomPrefix(String prefix) implements InstallerOption {

        }
    }
}
//
///**
// * An installer used to create {@link BeanHandle}. Is created using the various {@code beanInstaller} methods on
// * {@link BeanExtensionPoint}.
// * <p>
// * The main purpose of this interface is to allow various configuration that is needed before the bean is introspected.
// * If the configuration is not needed before introspection the functionality such be present on {@code BeanHandle}
// * instead.
// * 
// * @see BeanExtensionPoint#newFunctionalBean()
// * @see BeanExtensionPoint#beanInstallerFromClass(Class)
// * @see BeanExtensionPoint#newHandleFromOp(Op)
// * @see BeanExtensionPoint#beanBuilderFromInstance(Object)
// */
//// Could have, introspectionDisable()/noIntrospection
//
//@Deprecated
//sealed interface Installer<T> permits PackedBeanHandleInstaller {
//
////    /**
////     * Marks the bean as owned by the extension representing by specified extension point context
////     * 
////     * @param context
////     *            an extension point context representing the extension that owns the bean
////     * @return this builder
////     * @throws IllegalStateException
////     *             if build has previously been called on the builder
////     */
////    Installer<T> forExtension(UseSite context);
//
//    /**
//     * Adds a new bean to the container and returns a handle for it.
//     * 
//     * @return the new handle
//     * @throws IllegalStateException
//     *             if install has previously been called
//     */
//    BeanHandle<T> install();
//
////    /**
////     * There will never be any bean instances.
////     * <p>
////     * This method can only be used together with {@link BeanExtensionPoint#beanInstallerFromClass(Class)}.
////     * 
////     * @return this installer
////     * @throws IllegalStateException
////     *             if used without source kind {@code class}
////     */
////    // I think we have an boolean instantiate on beanInstallerFromClass
////    Installer<T> instanceless();
////
////    /**
////     * Registers a bean introspector that will be used instead of the framework calling
////     * {@link Extension#newBeanIntrospector}.
////     * 
////     * @param introspector
////     * @return this builder
////     * 
////     * @throws UnsupportedOperationException
////     *             if the bean has a void bean class
////     * 
////     * @see Extension#newBeanIntrospector
////     */
////    Installer<T> introspectWith(BeanIntrospector introspector);
//
//    // Option.Singleton, Option.lifetimeLazy;
//
//    // Instance -> Altid eager
//
//    // Eager - Singleton
//    // Eager - NonSingleton
//    // Lazy - Singleton
//    // Lazy - NonSingleton
//    // Many
////    Installer<T> kindSingleton();
////
////    Installer<T> kindUnmanaged();
//
//}