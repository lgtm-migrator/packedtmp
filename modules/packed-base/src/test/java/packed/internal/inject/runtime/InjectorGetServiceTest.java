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
package packed.internal.inject.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import support.stubs.Letters.A;

/**
 * Tests various things that do not have their own test class.
 */
public class InjectorGetServiceTest {

    @Test
    public void isRuntimeServices() {
        Injector i = Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(A.class);
        });

        assertThat(i.getDescriptor(A.class).get()).isInstanceOf(RuntimeServiceNode.class);
    }
}
