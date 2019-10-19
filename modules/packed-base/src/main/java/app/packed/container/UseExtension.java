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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-annotation that can be placed on annotations...
 * 
 * That has a number of uses depending on what types it is placed on
 * 
 * Bundle.
 * 
 * Extension
 * 
 * Annotation, Instances
 * 
 * ComponentType??? Ja det er jo saadan hvad InstanceOf
 * 
 * Inherited???? Hmmm, Actor
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseExtension {

    String[] optional() default {};

    /**
     * Returns the extensions that are used.
     * 
     * @return the extensions that are used
     */
    Class<? extends Extension>[] value() default {};
}

// Vi havde en module some target, men det er bare accident waiting to happen
// En der faar annoteret et modul og glemmer alt om det....

// FeatureAnnotation

// Unfprtunantely, you cannot register random annotations for use. As this would break encapsulation.
// ActivateExtension

// Vi har vel i virkeligheden 3 interesante ting...
// Extension
// Hook
// Bundle (ContainerSource??)
// Components???? HMMMMM. HMMMM

// Can be used on
// Hook Annotations
// Other Extensions... Or just use Extension#use
// Hook Class/Interface, for example, @ActivateExtension(LoggingExtension.class) Logger

// RequireExtension, UseExtension, ActivateExtension
// final void uses(String... extensionTypes) {
// // The names will be resolved when composer is created
//
// // Det ideeele ville vaere hvis man kunne specificere en eller callback/klasse der skulle koeres.
// // Hvis den givne extension var der.
// // Maaske noget a.la. dependOn(String, String instantiateThisClassAndInvokXX)
// }
