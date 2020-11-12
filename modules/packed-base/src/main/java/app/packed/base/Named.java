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
package app.packed.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles;

/** A {@link Qualifier} that holds a generic string. */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface Named {

    /** An annotation maker that can create {@link Named} instances. */
    static final AnnotationMaker<Named> MAKER = AnnotationMaker.of(MethodHandles.lookup(), Named.class);

    /**
     * Returns the name.
     * 
     * @return the name
     */
    String value();
}
