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
package packed.internal.invokers;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;

import app.packed.inject.Inject;

/**
 * A service class descriptor contains information about injectable fields and methods.
 */
// Qualifier, default key...
public class ServiceClassDescriptor<T> {

    /** The class this descriptor is created from. */
    private final Class<T> clazz;

    /** All fields annotated with {@link Inject}. */
    private final Collection<FieldInvokerAtInject> injectableFields;

    /** All methods annotated with {@link Inject}. */
    private final Collection<MethodInvokerAtInject> injectableMethods;

    /** The simple name of the class as returned by {@link Class#getSimpleName()}. (Quite a slow operation) */
    private final String simpleName;

    /**
     * Creates a new descriptor.
     * 
     * @param clazz
     *            the class to create a descriptor for
     * @param lookup
     *            the lookup object used to access fields and methods
     */
    ServiceClassDescriptor(Class<T> clazz, MethodHandles.Lookup lookup) {
        this(clazz, lookup, MemberScanner.forService(clazz, lookup));
    }

    ServiceClassDescriptor(Class<T> clazz, MethodHandles.Lookup lookup, MemberScanner scanner) {
        // Do we need to store lookup??? I think yes. And then collect all annotated Fields in a list
        // We then run through each of them
        // Or maybe just throw it in an invoker?? The classes you register, are normally there for a reason.
        // Meaning the annotations are probablye

        this.clazz = clazz;
        this.simpleName = clazz.getSimpleName();
        this.injectableFields = scanner.fieldsAtInject == null ? List.of() : List.copyOf(scanner.fieldsAtInject);
        this.injectableMethods = scanner.methodsAtInject == null ? List.of() : List.copyOf(scanner.methodsAtInject);
    }

    /**
     * Returns the simple name of the class.
     *
     * @return the simpleName of the class
     * @see Class#getSimpleName()
     */
    public final String getSimpleName() {
        return simpleName;
    }

    /**
     * Returns the type that is mirrored
     *
     * @return the type that is mirrored
     */
    public final Class<T> getType() {
        return clazz;
    }

    /**
     * Returns whether or not the service type has any injectable fields
     * 
     * @return whether or not the service type has any injectable fields
     */
    public final boolean hasInjectableFields() {
        return !injectableFields.isEmpty();
    }

    /**
     * Returns whether or not the service type has any injectable methods
     * 
     * @return whether or not the service type has any injectable methods
     */
    public final boolean hasInjectableMethods() {
        return !injectableMethods.isEmpty();
    }

    /**
     * Returns all injectable fields on this type.
     * 
     * @return all injectable fields on this type
     */
    public final Collection<FieldInvokerAtInject> injectableFields() {
        return injectableFields;
    }

    /**
     * Returns all injectable methods on this type.
     * 
     * @return all injectable methods on this type
     */
    public final Collection<MethodInvokerAtInject> injectableMethods() {
        return injectableMethods;
    }

    /**
     * Returns a service class descriptor for the specified lookup and type
     * 
     * @param <T>
     *            the type of element the service class descriptor holds
     * @param lookup
     *            the lookup
     * @param type
     *            the type
     * @return a service class descriptor for the specified lookup and type
     */
    public static <T> ServiceClassDescriptor<T> from(MethodHandles.Lookup lookup, Class<T> type) {
        return LookupDescriptorAccessor.get(lookup).getServiceDescriptor(type);
    }
}
//
// public Key<T> getDefaultKey() {
// // Hmmmm, this does not play well with Factory.of(SomeQualifiedServiceClass.class) <- Which ignores the qualifier....
// // skal virke baade paa bind(Class) + bind(instance)
// // Static @Inject method
//
// /** The default key that this service will be made available, unless explicitly bound to another key. */
// // Men er det ikke her vi har Factoriet?????
// volatile Key<T> defaultKey;
//
// return defaultKey;
// }
