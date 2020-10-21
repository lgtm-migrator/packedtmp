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

import java.util.Optional;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.container.Extension;
import app.packed.inject.sandbox.Injector;
import app.packed.introspection.FieldDescriptor;
import app.packed.introspection.MemberDescriptor;
import app.packed.introspection.ParameterDescriptor;
import app.packed.introspection.VariableDescriptor;

/**
 * An instance of this interface can be injected into methods that are annotated with {@link Provide}.
 * 
 * 
 * Whenever a A service requestions has two important parts. What exactly are being requested, is it optional is the
 * service being requested.
 * 
 * An injection site extends the dependency interface with runtime information about which injector requested the
 * injection. And if used within a container which component requested the injection.
 * <p>
 * This class is typically used together with the {@link Provide} annotation to provide custom injection depending on
 * attributes of the requestor. <pre> {@code  @Provides
 *  public static Logger provideLogger(PrototypeRequest request) {
 *    if (request.component().isPresent()) {
 *      return Logger.getLogger(request.component().get().getPath().toString());
 *    } else {
 *      return Logger.getAnonymousLogger();
 *    }
 *  }}
 * </pre>
 * <p>
 * An injection site can also be used to create a factory that is functional equivalent:
 * 
 * <pre> {@code
 *  new Factory1<PrototypeRequest, Logger>(
 *    c -> c.component().isPresent() ? Logger.getLogger(request.component().get().getPath().toString()) : Logger.getAnonymousLogger()) {};
 * }
 * </pre>
 * 
 * @apiNote In the future, if the Java language permits, {@link ProvisionContext} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ProvisionContext {

    /**
     * Returns whether or the instance is being injected. For example, into a field, method, constructor.
     *
     * @return whether or the instance is being injected
     */
    boolean isInjection();

    /**
     * Returns whether or not an instance is requested via a {@link ServiceLocator}. Instances that are requested via a
     * service locator always returns empty for all optionals (maybe not extension...)
     *
     * @return whether the instance is being provided via a lookup
     * @see ServiceLocator#use(Class)
     * @see ServiceLocator#use(Key)
     */
    boolean isLookup();

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    // Tillader vi det??? Hvorfor ikke
    // Hvis man vil vaere optional skal man bruge ProvisionContext
    // Og man skal lade sig styre af isOptional
    boolean isOptional();

    /**
     * The class that is being provided a value to. Or {@link Optional#empty()} if a lookup
     * 
     * @return stuff
     */
    default Optional<Class<?>> targetClass() {
        // RequestingClass
        // RequestingMember
        // RequestingVariable

        // Requester, if used for dependency injection....
        // Her er det taenkt som den oprindelig klasse... sans mappers...sans composites
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the key of the service that needs to be provided.
     *
     * @return the key of the service that needs to be provided
     */
    // Maaske den bare skal vaere separat.... Hvis vi gerne vil bruge ProvideContext for prime.
    // Saa er det jo noedvendig ikke noget der hedder key...

    // Og vi ved vel aerlig talt hvilken noegle vi er.....
    // Key<?> key();

    /**
     * Return the component that is requesting a service. Or an empty optional otherwise, for example, when used via
     * {@link Injector#use(Class)}.
     * 
     * @return the component that is requesting the component, or an empty optional if not a component.
     */
    // What to do at configuration time.... We probably don't have a component at that point...

    // ComponentPath???, syntes ikke man skal kunne iterere over dens boern...
    // Det er bare f.eks. til at debugge...
    Optional<Component> targetComponent();

    /**
     * If the requester party is part of an {@link Extension}. Returns the type of extension that is requesting an instance.
     * Otherwise false.
     * <p>
     * 
     * @return who wants this shit
     * 
     * @apiNote This method is only relevant for extension developers.
     */
    Optional<Class<? extends Extension>> targetExtension();

    /**
     * The member (field, method or constructor) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a member.
     * <p>
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #targetVariable()
     */
    // Altsaa taenker man laver en special annotation.
    @Deprecated
    Optional<MemberDescriptor> targetMember();

    /**
     * The variable (field or parameter) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * <p>
     * If this dependency was created from a field the returned optional will contain a {@link FieldDescriptor}. If this
     * dependency was created from a parameter the returned optional will contain a {@link ParameterDescriptor}.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     * @see #targetMember()
     */
    Optional<VariableDescriptor> targetVariable();// Should match Var...-> VarDescriptor-> VariableDescriptor
}
// Vi tager alle annotations med...@SystemProperty(fff) @Foo String xxx
// Includes any qualifier...
// AnnotatedElement memberAnnotations();

// Hvad hvis det ikke er en direkte extension der forsporger???
// Men f.eks. et eller andet inde i en Bundle som er installeret
// som en extension...
// I sidste ende er det jo hvor den ender op der er vigtigt (target class)

//Taenker den 

//Rename to ProvidesHelper, then we can save Context for other stuff. And say helper is always used for additional

//InjectionSite.. Is a bit misleasing because of Injector.get();
//DependencyRequest

//ProvidesContext, ProvisionContext

//Generic Dependency @SystemProperty, ServiceRequest

//qualifier

//What need injection (Dependency)
//who needs the injection (Which service)

//Should use composition

//Jeg syntes ikke ProvidesHelper

//Jeg er tilboejetil til at lave dependency til en final klasse...
//Og saa lave den optional paa Provides
//Dependency -> Only for Injection. use() <- er ikke en dependency men du kan bruge ProvidesHelper
//Hvad med Factory<@Cool String, Foo> Det ville jeg sige var en dependency.
//Saa Dependency har en ElementType of
//ElementType[] types = new ElementType[] { ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD,
//ElementType.PARAMETER };
//Type
//Hahaha, what about @RequiredService.. har maaske ikke noget med dependencies at goere...

//--------------------------------------------------------------------------------------
//Altsaa vi skal supporte Dependency paa alt hvad vi kan laver til et factory udfra...
//---
//Hmmmmmm det aendrer tingene lidt, eller maaske....

//DependencyDescriptor -> klasse
//ProvidesHelper Interface. maaske de samme metoder som dependency...

//Primaere grund er at vi kan bruge Optional<Dependency>
//Som bedre angiver brug af use()

//Hvordan virker InjectorTransformer, eller hvad den nu hedder... Den er ikke en service, men en slags transformer.
//Ville ikke give mening at have den som en dependency... Vi processere altid en ting af gangen....
//Provides, for singleton service burde smide Unsupported operation for dependency. Det her object giver faktisk ikke
//mening
//for singleton services. Alle metoder burde smide UnsupportedOperationException...
//Du burde kunne faa ProvidesHelper injected i din klasse ogsaa....
//@Provides(many = true, as = Logger.class) ... ahh does qualifiers...not so pretty
//public class MyLogger {
/// MyLogger(ProvidesHelper p)
//} Saa kan vi klare AOP ogsaa. Hvilket er rigtig svaert

//ProvidesManyContext... A object that helps
//Does not make sense for singletons... med mindre vi har saaden noget som exportAs()
//ServiceContext <- Permanent [exported(), Set<Key<?>> exportedAs(), String[] usesByChildContainers
//ProvisionContext <- One time

//ProvisionContext (used with Provide), InjectionContext (used with Inject)

//provide(String.class).via(h->"fooo");
//Hmm provide("Goo").via() giver ingen mening..

//provide(MyService).from(MyServiceImpl.class)
//provide(MyServiceImpl).as(MyService.class)

//ServiceRequest... Somebody is requesting a service...

//Kan not be used with singletons...

//ConfigSite???? <- Use unknown for service locator...

//ComponentPath
//ConfigSite

//Den eneste grund til vi ikke sletter denne, er for request traces....
//Detailed Logging....
//(@PrintResolving SomeService)
////Kunne vi tage en enum????
////NO_DEBUG, DEBUG_LOOK_IN_THREADLOCAL

//Interface vs Class..
//Interface because we might want an implementation with meta data internally...
//PackedPrototypeRequest that every entry will read, and log stuff.

//ServiceUsageSite, ProtetypeUsageSite

//ProvideContext <- Used together with Provide.....
//But not if Singleton.....

//Maaske er Component bare en separate injection???
//Men saa skal vi have ComponentContext.component()

//Kan den bruges paa sidecars??? SidecarProvide maaske....
//ProvideLocal? Føler lidt at grunden til vi ikke kan bruge annoteringer
//Som vi vil paa sidecars er pga Provide...
//F.eks. Schedule boer jo fungere praecis som var den paa componenten...

//ProvisionContext (If we have InjectionContext)
//ProvisionPrototypeContext

//Uses attributes????? I Think it may better support it...

//@ProvideNonConstantContext
//PrototypeProvideContext

//Eller ogsaa kan den bruges altid og saa
//er det bare BaseExtension der staar som requestor...

//Altsaa en slags hvorfor bliver jeg instantieret....

//Og kan vi ikke bare smide den paa InjectionContext????
//Ville nu vaere fedt at adskille taenker jeg.
//Den ene er debugging og den her kan vaere useful 
//paa runtime

//Det er ogsaa bare alle informationer vi bliver noedt til at at gemme i InjectionContext....

//ProvideContext(), ProvideServiceContext

//Altsaa taenker vi ogsaa kan bruge den til constanter...
//Saa er der bare tom for alt..
//static ProvideContext of(Dependency dependency) {
//  return new ProvideContextImpl(dependency, null);
//}
//
//static ProvideContext of(Dependency dependency, Component componenent) {
//  return new ProvideContextImpl(dependency, requireNonNull(componenent, "component is null"));
//}

//
///**
//* If this helper class is created as the result of needing dependency injection. This method returns an empty optional
//* if used from methods such as {@link App#use(Class)}.
//* 
//* @return any dependency this class might have
//*/
//default Optional<Dependency> dependency() {
// throw new UnsupportedOperationException();
//}