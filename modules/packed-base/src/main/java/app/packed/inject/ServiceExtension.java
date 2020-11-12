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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.ExposeAttribute;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.Qualifier;
import app.packed.bundle.Extension;
import app.packed.bundle.ExtensionConfiguration;
import app.packed.component.BeanConfiguration;
import app.packed.component.ComponentFactoryDriver;
import app.packed.config.ConfigSite;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import app.packed.inject.sandbox.PrototypeConfiguration;
import app.packed.inject.sandbox.ServiceAttributes;
import app.packed.validate.Validator;
import packed.internal.bundle.BundleBuild;
import packed.internal.bundle.ExtensionBuild;
import packed.internal.config.ConfigSiteInjectOperations;
import packed.internal.inject.service.ServiceComposer;
import packed.internal.inject.service.runtime.PackedInjector;

/**
 * The main configuration point for services.
 * <p>
 * This extension provides the following functionality:
 * 
 * is extension provides functionality for exposing and consuming services.
 * 
 * 
 */
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)
// * Manual Injection

// Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
// MHT til Manuel Requirements Management
// (Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseBundle og vaelge det..
// Hmm saa auto instantiere vi jo injector extensionen
//// Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
// altid bruge Manual Requirements
// contracts bliver installeret direkte paa ContainerConfiguration

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Bundle.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @BundleStuff(onActivation = FooActivator.class) -> ForActivator extends BundleController

// Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
// D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
// Ellers selvfoelgelig hvis man bruger provide/@Provides\
public final class ServiceExtension extends Extension {

    /** The containers injection manager which controls all service functionality. */
    // we use this for provideProtoype for now But should go away at some point
    private final BundleBuild bundle;

    /** The service build manager. */
    private final ServiceComposer composer;

    /**
     * Invoked by the runtime to create a new service extension.
     * 
     * @param extension
     *            the configuration of the extension
     */
    /* package-private */ ServiceExtension(ExtensionConfiguration extension) {
        this.bundle = ((ExtensionBuild) extension).bundle();
        this.composer = bundle.newServiceManagerFromServiceExtension();
    }

    /**
     * 
     * This method is typically used if you work with plugin structures. Where you do not now ahead of time what kind of
     * services the plugins export.
     * 
     * {@link ServiceSelection}
     * 
     * @see ServiceWirelets#anchorAll()
     */
    public void anchorAll() {}

    public void anchorIf(Predicate<? super Service> filter) {
        // Only anchrored services???
    }

    // Validates the outward facing contract
    public void checkContract(Validator<? super ServiceContract> validator) {

    }

    public void checkExactContract(ServiceContract sc) {
        checkContract(ServiceContractChecker.exact(sc));
    }

    /**
     * Exports a service of the specified type. See {@link #export(Key)} for details.
     * 
     * @param <T>
     *            the type of service to export
     * @param key
     *            the key of the service to export
     * @return a configuration for the exported service
     * @see #export(Key)
     * @see #exportAll()
     */
    public <T> ExportedServiceConfiguration<T> export(Class<T> key) {
        return export(Key.of(key));
    }

    /**
     * Exports an internal service outside of this bundle.
     * 
     * <pre>
     *  {@code  
     * install(ServiceImpl.class);
     * export(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * 
     * <pre>
     *  {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * <p>
     * Packed does not support any other way of exporting a service provided via a field or method annotated with
     * {@link Provide} except for this method. There are no plan to add an Export annotation that can be used in connection
     * with {@link Provide}.
     * 
     * <p>
     * A service can be exported multiple times
     * 
     * @param <T>
     *            the type of the service to export
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    // Is used to export @Provide method and fields.
    public <T> ExportedServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        return composer.exports().export(key, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    // Altsaa skal vi hellere have noget services().filter().exportall();

    // Alternativ this export all er noget med services()..

    // is exportAll applied immediately or at the end???

    // Maaske er den mest brugbart hvis den bliver applied nu!
    // Fordi saa kan man styre ting...
    // F.eks. definere alt der skal exportes foerst
    // Den er isaer god inde man begynder at linke andre containere

    // One of 3 models...
    // Fails on other exports
    // Ignores other exports
    // interacts with other exports in some way
    /**
     * Exports all internal services.
     * <p>
     * 
     * <ul>
     * <li><b>Service already exported.</b> The service that have already been exported (under any key) are always
     * ignored.</li>
     * <li><b>Key already exported.</b>A service has already been exported under the specified key.
     * <li><b>Are requirements.</b> Services that come from parent containers are always ignored.</li>
     * <li><b>Not part of service contract.</b> If a service contract has set. Only services for whose key is part of the
     * contract is exported.</li>
     * </ul>
     * <p>
     * This method can be invoked more than once. But use cases for this are limited.
     */
    public void exportAll() {

        // Add exportAll(Predicate); //Maybe some exportAll(Consumer<ExportedConfg>)
        // exportAllAs(Function<?, Key>

        // Export all entries except foo which should be export as Boo
        // exportAll(Predicate) <- takes key or service configuration???

        // export all _services_.. Also those that are already exported as something else???
        // I should think not... Det er er en service vel... SelectedAll.keys().export()...
        checkConfigurable();
        composer.exports().exportAll(captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    public void exportAll(Function<? super Service, @Nullable Key<?>> exportFunction) {

    }

    public void exportIf(Predicate<? super Service> filter) {
        // Only anchrored services???
    }

    /**
     * Expose a service contract as an attribute.
     * 
     * @return a service contract for this extension
     */
    @ExposeAttribute(from = ServiceAttributes.class, name = "contract")
    /* package-private */ ServiceContract exposeContract() {
        return composer.newServiceContract();
    }

    /**
     * Exposes an immutable service registry of all exported services. Or null if there are no exports.
     * 
     * @return any exported services. Or null if there are no exports
     */
    @ExposeAttribute(from = ServiceAttributes.class, name = "exported-services")
    @Nullable
    /* package-private */ ServiceRegistry exposeExportedServices() {
        return composer.exports().exportsAsServiceRegistry();
    }

    /**
     * Provides every service from the specified locator.
     * 
     * @param locator
     *            the locator to provide services from
     * @throws IllegalArgumentException
     *             if the specified locator is not implemented by Packed
     */
    public void provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        if (!(locator instanceof PackedInjector)) {
            throw new IllegalArgumentException("Custom implementations of " + ServiceLocator.class.getSimpleName()
                    + " are currently not supported, injector type = " + locator.getClass().getName());
        }
        checkConfigurable();
        composer.provideAll((PackedInjector) locator, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_PROVIDE_ALL));
    }

    /**
     * Binds a new service constant to the specified instance.
     * <p>
     * The default key for the service will be {@code instance.getClass()}. If the type returned by
     * {@code instance.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will have the
     * qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param instance
     *            the instance to bind
     * @return a service configuration for the service
     */
    public <T> BeanConfiguration<T> provideInstance(T instance) {
        return configuration().wireInstance(BeanConfiguration.driver(), instance).provide();
    }

    // Will install a ServiceStatelessConfiguration...
    // Spoergmaalet er om vi ikke bare skal have en driver...
    // og en metode paa BaseBundle...
    public <T> PrototypeConfiguration<T> providePrototype(Factory<T> factory) {
        return bundle.compConf.wire(prototype(), factory);
    }

    // requires bliver automatisk anchoret...
    // anchorAllChildExports-> requireAllChildExports();
    public void require(Class<?>... keys) {
        require(Key.of(keys));
    }

    /**
     * Explicitly adds the specified key to the list of required services. There are typically two situations in where
     * explicitly adding required services can be useful:
     * <p>
     * First, services that are cannot be specified at build time. But is needed later... Is mainly useful when we the
     * services to. For example, importAll() that injector might not a service itself. But other that make use of the
     * injector might.
     * 
     * 
     * <p>
     * Second, for manual service requirement, although it is often preferable to use contracts here
     * <p>
     * In any but the simplest of cases, contracts are useful
     * 
     * @param keys
     *            the key(s) to add
     */
    public void require(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkConfigurable();
        ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE);
        for (Key<?> key : keys) {
            composer.dependencies().require(key, false, cs);
        }
    }

    public void requireOptionally(Class<?>... keys) {
        requireOptionally(Key.of(keys));
    }

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #require(Key...)} or implicitly via, for example, a constructor dependency. The key will be removed from
     * the list of optional services and only be listed as a required key.
     * 
     * @param keys
     *            the key(s) to add
     */
    // How does this work with child services...
    // They will be consumed
    public void requireOptionally(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkConfigurable();
        ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (Key<?> key : keys) {
            composer.dependencies().require(key, true, cs);
        }
    }

    /**
     * Performs a transformation of any exported services. Final adjustments before services are visible a parent container.
     * 
     * @param transformer
     *            transforms the exports
     */
    public void transformExports(Consumer<? super ServiceTransformation> transformer) {
        composer.exports().addExportTransformer(transformer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) // javac
    public static <T> ComponentFactoryDriver<PrototypeConfiguration<T>, T> prototype() {
        return (ComponentFactoryDriver) ComponentFactoryDriver.of(MethodHandles.lookup(), PrototypeConfiguration.class);
    }

    final class SidecarHelper {

    }

    /**
     * A subtension useable from other extensions.
     * <p>
     * There is no support for exporting services. The user is always in full control of what is being exported out from the
     * container via the various export methods on {@link ServiceExtension}.
     * 
     **/
    public final class Sub extends Subtension {

        // Require???
        // This is very limited as we can only require services from other
        // extensions that we depend on...

        // SetContract
        /// This would apply locally to the extension...
        /// limited usefullness

        /** The other extension type. */
        final Class<? extends Extension> extensionType;

        /**
         * sdsd.
         * 
         * @param extensionType
         *            the type this is a sub extension for
         */
        /* package-private */ Sub(Class<? extends Extension> extensionType) {
            this.extensionType = requireNonNull(extensionType, "extensionType is null");
        }
    }
}

class ZExtraFunc {

    protected void addAlias(Class<?> existing, Class<?> newKey) {}

    // Maaske er det her mere injection then service

    // Vi vil gerne tilfoejer
//    protected ExportedServiceConfiguration<ServiceSelection> addSelectin(Function<>) {}

    protected void addAlias(Key<?> existing, Key<?> newKey) {}

    <T> ExportedServiceConfiguration<T> assistedInject(Class<T> interfase) {
        // or abstract class can have state which can be merge in some way?
        // well def not ver 1.

        throw new UnsupportedOperationException();
    }

    <T> ExportedServiceConfiguration<T> addOptional(Class<T> optional) {
        // @Inject is allowed, but other annotations, types und so weiter is not...
        // Den har ihvertfald slet ikke noget providing...
        throw new UnsupportedOperationException();
    }

    <T> ExportedServiceConfiguration<T> alias(Class<T> key) {
        // Hmm maaske vi skal kalde den noget andet...
        // SingletonService kan sikkert sagtens extende den...
        // ProtoypeConfiguration has altid en noegle og ikek optional..
        throw new UnsupportedOperationException();
    }

//    <S, U> void breakCycle(OP2<S, U> op) {
//        // Denne kraever at vi paa en eller anden maade kan bruge OP2...
//        // MethodHandle op.invoker() <--- Saa maaske er det bare ikke hemmeligt mere.
//        // Eller kan bruge det...
//        throw new UnsupportedOperationException();
//    }

    <S, U> void breakCycle(Key<S> key1, Key<U> key2, BiConsumer<S, U> consumer) {
        // cycleBreaker
        throw new UnsupportedOperationException();
    }

    // Det er jo i virkeligheden bare en @RunOnInjection klasse
    // LifecycleExtension-> BreakCycle(X,Y) ->
    <S, U> void cycleBreaker(Class<S> keyProducer, Class<U> key2) {
        // keyProducer will have a Consumer<U> injected in its constructor.
        // In which case it must call it exactly once with a valid instance of U.
        // U will then be field/method inject, initialization und so weither as normally.
        // But to the outside it will not that S depends on U.

        // Den der tager en biconsumer supportere ikke at de kan vaere final fields af hinanden...

        // Det kan ogsaa vaere en klasse CycleBreaker.. som tager ContainerConfiguration
        // Bundle, Service Extension, ExtensionContext ect...

        // DE her virker kun indefor samme container...

        // Syntes i virkeligheden consumer versionen er bedre....
        // Den er meget mere explicit.
        // Den her skal jo pakke initialisering af U ind i en consumer
    }

    <S, U> void cycleBreaker(Class<S> key1, Class<U> key2, BiConsumer<S, U> consumer) {

        // Taenker om vi skal checke at key2 depender on key1...
        // Jeg taenker ja, fordi saa saa kan vi visuelleciere det...
        // Og vi fejler hvis der ikke er en actuel dependency

        // Maaske bare warn istedet for at fejle. Men syntes ikke folk skal have en masse af dem liggende...

//        Break circular references...
//        runOnInitialize(ST2<Xcomp, YComp>(xComp.setY(yComp){});
//        Ideen er at vi har super streng cyclic check...
//        Og saa kan man bruge saadan en her til at bende det...

        // Maaske har vi endda en eksplicit i ServiceManager...
        // breakCycle, breakDependencyCycle
        // cyclicBreak(Op2(X, Y) -> x.setY(y)

        // Alternativt vil vi godt sige at X laver en ny Y component (ikke bare en service)

        // Will probably validate if there is an actual cycle...

//        Of course the idea is that we want to be very explicit about the eyesoar
//        So people can find it...

        // In a perfect world we would also write perfect cycle free code..
        // But to support your petty little program

        // http://blog.jdevelop.eu/?p=382
        // We don't support this. Server must instantiate the Client if it needs it
        // But then client isn't managed!!!

        // Could we also allow the user, to provide another OP???
        // That server could get which it could invoke
        // FN<Client, Server, String> : from server
        // this.client = fn.invoke(this, "fooobar");
        // Hmm, ikke saerlig paen... Men hvis vi vil have final/final
        // Vi tillader kun et object... Hvis man vil have flere.
        // Maa man pakke det ind i en Composite... I saa fald.. Vil det se ud som om
        // At det faktisk er client'en der kalder ting. F.eks. kunne man have en logger.... Som stadig ville se
        // client og ikke server. Selvom man kaldte gemmen servers constructor
        // CycleBreak
        // CConf c= install(Server.class)
        // c.assistInstall(client, new FN2<Client, Server, @Composite SomeRecord(Logger, RandomOtherThing))

        // Break down circles manuel.. https://github.com/google/guice/wiki/CyclicDependencies
        // Using decomposition

        throw new UnsupportedOperationException();
    }

    // Vi venter med den...
    // Altsaa det er jo kun services den kan exportere...
    // Altsaa vi kan jo have nogle
    <T, R> ExportedServiceConfiguration<T> export(Factory1<T, R> factory) {
        // Exports a service by mapping an existing service
        // Eneste problem er nu har vi exported services som ikke er services...
        // Men det er vel ikke anderledes end install(X).provide();
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an unmodifiable view of the services that are currently available within the container.
     * 
     * @return a unmodifiable view of the services that are currently available within the container
     */
    ServiceRegistry services() {
        // Problemet er vel at vi resolver her...
        throw new UnsupportedOperationException();
    }
    // Skal vi ogsaa supportere noget paa tvaers af bundles???
    // Det er vel en slags Wirelet
    // CycleBreaker(SE, ...);
    // CycleBreaker(SE, ...);

    // autoExport

}