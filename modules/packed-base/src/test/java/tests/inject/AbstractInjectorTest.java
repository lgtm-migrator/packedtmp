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
package tests.inject;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;

/**
 *
 */
public abstract class AbstractInjectorTest {

    public static Injector injector(MethodHandles.Lookup lookup, Consumer<? super InjectorConfiguration> consumer) {
        return Injector.of(c -> {
            c.lookup(lookup);
            consumer.accept(c);
        });
    }

}
