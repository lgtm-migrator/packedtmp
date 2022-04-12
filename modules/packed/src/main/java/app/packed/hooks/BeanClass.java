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
package app.packed.hooks;

import java.lang.invoke.MethodHandles;

/**
 *
 */
public interface BeanClass {

    // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
    // Masske har vi BeanClass.Builder() istedet for???
    static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
        throw new UnsupportedOperationException();
    }
}
