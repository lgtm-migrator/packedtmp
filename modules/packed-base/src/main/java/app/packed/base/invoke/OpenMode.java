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
package app.packed.base.invoke;

/**
 *
 */
// AccessOperation.. Instance er vel mere en type end en operation
public enum OpenMode {

    CUSTOM, // Can specify a class...
    // Eller ogsaa er det i addition til hvad man har specificeret her/
    // dvs @OpensFor(value = INVOKE, gateKeeper = SomeOpenGatekeeper.class)
    // boolean checker or void check throws Exception?

    /** Access rights for invocation of a method or constructor. */
    INVOKE,

    /** Access rights for reading a field. */
    GET_FIELD,

    /** Access rights for writing a field. */
    // Was Field_SET but sounded like Set<Field>
    SET_FIELD,

    // Needs a Type annotation.... (also on subclasses...) Must check Inherited status of the annotation...

    // All_MEMBERS_FULL

    // Subclasses must be annotated @OpenForSupertypes() (all or only annotations???)

    // Kan jo ikke bare give adgang til at members i super klasser....

    FULL_CLASS, FULL_CLASSAND_SUBCLASSES,

    /** Access to instances of the target. Needed, for example, for VarHandle.compareAndSet(xxxxx) */
    // Ideen er lidt at man kan faa alle instanser af X
    /// Jajaja, saa betyder det at ikke alle kan faa adgang....
    INSTANCE,

    /** Ja ja ja ja, det kunne måske virke. */
    COMPONENT_CONTEXT;
    // INVOKE_PARAMETERS <= Full access to all parameters... To set them. I think thats okay...
    // Trouble I think is you want to change the parameters
}

// To get a VarHandle you need both Field_get + Field Set

//// En maade at sige jeg gerne vil bruge XYZ metode...
// @ServiceAccess("ddd.module)

// Access to services
// Access rights to all parameters....
