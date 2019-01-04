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
package app.packed.bundle;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.container.Container;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Qualifier;
import app.packed.util.TypeLiteral;
import packed.internal.inject.builder.InjectorBuilder;

/**
 * Bundles provide a simply way to package components and service and build modular application. This is useful, for
 * example, for:
 * <ul>
 * <li>Sharing functionality across multiple injectors and/or containers.</li>
 * <li>Hiding implementation details from users.</li>
 * <li>Organizing a complex project into distinct sections, such that each section addresses a separate concern.</li>
 * </ul>
 * <p>
 * There are currently two types of bundles available:
 * <ul>
 * <li><b>{@link InjectorBundle}</b> which bundles information about services, and creates {@link Injector} instances
 * using {@link Injector#of(Class)}.</li>
 * <li><b>{@link ContainerBundle}</b> which bundles information about both services and components, and creates
 * {@link Container} instances using {@link Container#of(Class)}.</li>
 * </ul>
 */

// Descriptor does not freeze, Injector+Container freezes
public abstract class Bundle {

    /** Whether or not {@link #configure()} has been invoked. */
    boolean isFrozen;

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
     * @see InjectorConfiguration#bind(Class)
     */
    protected final <T> ServiceConfiguration<T> bind(Class<T> implementation) {
        return injectorBuilder().bind(implementation);
    }

    protected final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        return injectorBuilder().bind(factory);
    }

    protected final <T> ServiceConfiguration<T> bind(T instance) {
        return injectorBuilder().bind(instance);
    }

    protected final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        return injectorBuilder().bind(implementation);
    }

    protected final void bindInjector(Class<? extends InjectorBundle> bundleType, BundlingStage... filters) {
        injectorBuilder().bindInjector(bundleType, filters);
    }

    /**
     * Imports the services that are available in the specified injector.
     *
     * @param injector
     *            the injector to import services from
     * @param stages
     *            any number of filters that restricts the services that are imported. Or makes them available under
     *            different keys
     * @see InjectorConfiguration#bindInjector(Injector, BundlingStage...)
     * @throws IllegalArgumentException
     *             if the specified stages are not instance all instance of {@link BundlingImportStage} or combinations (via
     *             {@link BundlingStage#andThen(BundlingStage)} thereof
     */
    protected final void bindInjector(Injector injector, BundlingStage... stages) {
        injectorBuilder().bindInjector(injector, stages);
    }

    protected final void bindInjector(InjectorBundle bundle, BundlingStage... stages) {
        injectorBuilder().bindInjector(bundle, stages);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        return injectorBuilder().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        return injectorBuilder().bindLazy(factory);
    }

    protected final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        return injectorBuilder().bindLazy(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        return injectorBuilder().bindPrototype(implementation);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        return injectorBuilder().bindPrototype(factory);
    }

    protected final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        return injectorBuilder().bindPrototype(implementation);
    }

    /**
     * Checks that the {@link #configure()} method has not already been invoked. This is typically used to make sure that
     * users of extensions does try to configure the extension after it has been configured.
     *
     * <pre>{@code
     * public ManagementBundle setJMXEnabled(boolean enabled) {
     *     checkConfigurable(); //will throw IllegalStateException if configure() has already been called
     *     this.jmxEnabled = enabled;
     *     return this;
     * }}
     * </pre>
     * 
     * @throws IllegalStateException
     *             if the {@link #configure()} method has already been invoked once for this extension instance
     */
    protected final void checkConfigurable() {
        if (isFrozen) {
            throw new IllegalStateException("This bundle is no longer configurable");
        }
    }

    /**
     * Returns the configuration object that we delegate to.
     * 
     * @return the configuration object that we delegate to
     */
    abstract InjectorBuilder injectorBuilder();

    /** Configures the bundle using the various methods from the inherited class. */
    protected abstract void configure();

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
     * @see #expose(Key)
     */
    protected final <T> ServiceConfiguration<T> expose(Class<T> key) {
        return injectorBuilder().expose(key);
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
     * @see #expose(Key)
     */
    protected final <T> ServiceConfiguration<T> expose(Key<T> key) {
        return injectorBuilder().expose(key);
    }

    protected final <T> ServiceConfiguration<T> expose(ServiceConfiguration<T> configuration) {
        return injectorBuilder().expose(configuration);
    }

    /**
     * The lookup object passed to this method is never made available through the public api. It is only used internally.
     * Unless your private
     * 
     * @param lookup
     *            the lookup object
     * @see InjectorConfiguration#lookup(Lookup)
     */
    protected final void lookup(Lookup lookup) {
        injectorBuilder().lookup(lookup);
    }

    protected void requireMandatory(Class<?> key) {
        injectorBuilder().requireMandatory(key);
    }

    protected void requireMandatory(Key<?> key) {
        injectorBuilder().requireMandatory(key);
    }

    /**
     * Sets the description of the injector or container.
     * 
     * @param description
     *            the description of the injector or container
     * @see InjectorConfiguration#setDescription(String)
     * @see Injector#getDescription()
     */
    protected final void setDescription(@Nullable String description) {
        injectorBuilder().setDescription(description);
    }

    protected final Set<String> tags() {
        return injectorBuilder().tags();
    }
}
