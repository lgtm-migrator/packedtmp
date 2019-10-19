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
package packed.internal.module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import app.packed.lang.Key;
import app.packed.lang.TypeLiteral;

/** A support class for calling package private methods in the app.packed.util package. */
public interface AppPackedUtilAccess extends SecretAccess {

    boolean isCanonicalized(TypeLiteral<?> typeLiteral);

    Key<?> toKeyNullableQualifier(Type type, Annotation qualifier);

    /**
     * Converts the type to a type literal.
     * 
     * @param type
     *            the type to convert
     * @return the type literal
     */
    TypeLiteral<?> toTypeLiteral(Type type);
}
