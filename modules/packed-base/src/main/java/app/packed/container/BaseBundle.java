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

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.inject.Factory;
import app.packed.inject.InjectionExtension;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject.Provide;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.lifecycle.LifecycleExtension;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Qualifier;

/**
 * Base bundles contains easy access to common functionality difined by the various extension in this module.
 * 
 * 
 * With common functionality provide by app.packed.base
 * 
 * <p>
 * 
 * Bundles provide a simply way to package components and build modular application. This is useful, for example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link BaseBundle}</b> which bundles information about services, and creates {@link Injector} instances using
 * .</li>
 * <li><b>{@link BaseBundle}</b> which bundles information about both services and components, and creates container
 * instances using .</li>
 * </ul>
 */

// Descriptor does not freeze, Injector+Container freezes

// explicitServiceRequirements(); <- You can put it in an environment to force it. No it would break encapsulation
// AnyBundle...
// Bundle + BaseBundle, or
// AnyBundle + Bundle <- I think I like this better....

// We never return, for example, Bundle or AnyBundle to allow for method chaining.
// As this would make it deficult to extend

// protected final Restrictions restrictions = null;

public abstract class BaseBundle extends Bundle {

    /**
     * Exposes an internal service outside of this bundle, equivalent to calling {@code expose(Key.of(key))}. A typical use
     * case if having a single
     * 
     * When you expose an internal service, the descriptions and tags it may have are copied to the exposed services.
     * Overridden them will not effect the internal service from which the exposed service was created.
     * 
     * <p>
     * Once an internal service has been exposed, the internal service is made immutable. For example,
     * {@code setDescription()} will fail in the following example with a runtime exception: <pre>{@code 
     * ServiceConfiguration<?> sc = bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);
     * sc.setDescription("foo");}
     * </pre>
     * <p>
     * A single internal service can be exposed under multiple keys: <pre>{@code 
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service1.class).setDescription("Service 1");
     * expose(ServiceImpl.class).as(Service2.class).setDescription("Service 2");}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * 
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    protected final <T> ServiceConfiguration<T> export(Class<T> key) {
        return injector().export(key);
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * 
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * <pre> {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * @param <T>
     *            the type of the exposed service
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    protected final <T> ServiceConfiguration<T> export(Key<T> key) {
        return injector().export(key);
    }

    protected final <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration) {
        return injector().export(configuration);
    }

    protected final void exportHooks(Class<?>... hookTypes) {
        // interface = Instance Of Hooks
        throw new UnsupportedOperationException();
    }

    // protected final void exportHooks(Contract contract) {
    // throw new UnsupportedOperationException();
    // }

    /**
     * Returns an instance of the injector extension, installing it if it has not already been installed.
     * 
     * @return an instance of the injector extension
     */
    protected final InjectionExtension injector() {
        return use(InjectionExtension.class);
    }

    protected final LifecycleExtension lifecycle() {
        return use(LifecycleExtension.class);
    }

    protected final ComponentExtension component() {
        return use(ComponentExtension.class);
    }

    protected final ComponentConfiguration install(Object instance) {
        return component().install(instance);
    }

    protected final ComponentConfiguration installHelper(Class<?> implementation) {
        return component().installStatic(implementation);
    }

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#findInjectable(Class)} to
     * find a valid constructor or method to instantiate the service instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the {@code Class} is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
     * @see InjectorConfigurator#provide(Class)
     */
    // Rename to Provide@
    protected final <T> ProvidedComponentConfiguration<T> provide(Class<T> implementation) {
        return injector().provide(implementation);
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provide}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ProvidedComponentConfiguration<T> provide(Factory<T> factory) {
        return injector().provide(factory);
    }

    protected final <T> ProvidedComponentConfiguration<T> provide(T instance) {
        return injector().provide(instance);
    }

    protected final void requireService(Class<?> key) {
        injector().require(Key.of(key));
    }

    protected final void requireService(Key<?> key) {
        injector().require(key);
    }

    protected final void serviceManualRequirements() {
        injector().manualRequirementsManagement();
    }

    /**
     * Prints the contract of the specified bundle.
     * 
     * @param bundle
     *            the bundle to print the contract for
     */
    protected static void printContract(BaseBundle bundle) {
        BundleContract.of(bundle).print();
    }

    protected static void printDescriptor(BaseBundle bundle) {
        BundleDescriptor.of(bundle).print();
    }

}

// /**
// * Install the specified component instance.
// * <p>
// * If this install operation is the first install operation of the container. The component will be installed as the
// * root component of the container. All subsequent install operations on this bundle will have have component as its
// * parent. If you wish to have a specific component as a parent, the various install methods on
// * {@link ComponentConfiguration} can be used to specify a specific parent.
// *
// * @param <T>
// * the type of component to install
// * @param instance
// * the component instance to install
// * @return this configuration
// */
// protected final <T> ComponentConfiguration<T> install(T instance) {
// return containerBuilderX().install(instance);
// }

// protected final void wireContainer(Bundle bundle, WiringOperation... stages) {
// containerBuilderX().wireContainer(bundle, stages);
// }

// protected final void wireInjector(Bundle bundle, WiringOperation... operations) {
// injectorBuilder().wireInjector(bundle, operations);
// }
//
// /**
// * Imports the services that are available in the specified injector.
// *
// * @param injector
// * the injector to import services from
// * @param stages
// * any number of filters that restricts the services that are imported. Or makes them available under
// * different keys
// * @see InjectorConfiguration#wireInjector(Injector, WiringOperation...)
// * @throws IllegalArgumentException
// * if the specified stages are not instance all instance of {@link UpstreamWiringOperation} or combinations
// * (via {@link WiringOperation#andThen(WiringOperation)} thereof
// */
// protected final void wireInjector(Injector injector, WiringOperation... operations) {
// injectorBuilder().wireInjector(injector, operations);
// }

// protected ComponentServiceConfiguration<?> installBundle() {
// return containerBuilderX().installService(this);
// }
//
// protected final void main(String methodName, Class<?>... arguments) {
// throw new UnsupportedOperationException();
// }

// public interface Restrictions {
// void service(Class<?> clazz);
// }

/// **
// * @param builder
// * the injector configuration to delagate to
// * @param freeze
// * @apiNote we take an AbstractBundleConfigurator instead of a BundleConfigurator to make sure we never parse an
// * external configurator by accident. And we some let the bundle implementation invoke
// * {@link #lookup(java.lang.invoke.MethodHandles.Lookup)} on a random interface. Thereby letting the Lookup
// * object escape.
// */
// final void configure(InjectorBuilder builder, boolean freeze) {
//
// // Maybe we can do some access checkes on the Configurator. To allow for testing....
// //
// // if (this.injectorBuilder != null) {
// // throw new IllegalStateException();
// // } else if (isFrozen && freeze) {
// // // vi skal have love til f.eks. at koere en gang descriptor af, saa det er kun hvis vi skal freeze den ogsaa doer.
// // throw new IllegalStateException("Cannot configure this bundle, after it has been been frozen");
// // }
// // this.injectorBuilder = requireNonNull(builder);
// // try {
// // configure();
// // } finally {
// // this.injectorBuilder = null;
// // if (freeze) {
// // isFrozen = true;
// // }
// // }
// throw new UnsupportedOperationException();
// }
//// /**
//// * Returns the bundle support object which
//// *
//// * @return the bundle support object
//// */
//// protected final ContainerBuildContext context() {
//// // Vi laver en bundle nyt per configuration.....
//// ContainerBuildContext s = context;
//// if (s == null) {
//// throw new IllegalStateException("This method can only be called from within Bundle.configure(). Maybe you tried to
//// call Bundle.configure directly");
//// }
//// return s;
//// }
/**
 * A injector bundle provides a simple way to package services into a resuable container nice little thingy.
 * 
 * Bundles provide a simply way to package components and service. For example, so they can be used easily across
 * multiple containers. Or simply for organizing a complex project into distinct sections, such that each section
 * addresses a separate concern.
 * <p>
 * Bundle are useually
 *
 * <pre>
 * class WebServerBundle extends Bundle {
 *
 *     private port = 8080; //default port
 *
 *     protected void configure() {
 *        install(new WebServer(port));
 *     }
 *
 *     public WebServerBundle setPort(int port) {
 *         checkNotFrozen();
 *         this.port = port;
 *         return this;
 *     }
 * }
 * </pre>
 *
 * The bundle is used like this:
 *
 * <pre>
 * ContainerBuilder b = new ContainerBuilder();
 * b.use(WebServiceBundle.class).setPort(8080);
 *
 * Container c = cc.newContainer();
 * </pre>
 * <p>
 * Bundles must have a single public no argument constructor.
 * <p>
 * Bundles are strictly a configuration and initialization time concept. Bundles are not available
 */
//
/// **
// * Opens the bundle for modification later on
// */
// protected final void open() {
// // Nope....
// }
// ID256 BundleHash????? API wise. SpecHash..

// protected void lookup(Lookup lookup, LookupAccessController accessController) {}
// protected final void checkNotNativeRuntime() {
// if (GraalSupport.inImageRuntimeCode()) {
// StackFrame f = StackWalker.getInstance().walk(e -> e.skip(1).findFirst().get());
// throw new IllegalStateException("Cannot call " + f.getMethodName() + "() when running as a native-image");
// }
// }