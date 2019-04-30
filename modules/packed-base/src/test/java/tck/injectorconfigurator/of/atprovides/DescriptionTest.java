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
package tck.injectorconfigurator.of.atprovides;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import app.packed.inject.SimpleInjectorConfigurator;
import app.packed.inject.Provides;

/** Tests {@link Provides#description()}. */
public class DescriptionTest {

    /** Tests service with description on {@link Provides}. */
    @Test
    public void injectorWithDescription() {
        Injector i = of(c -> c.provide(new WithDescription()));
        assertThat(i.getDescriptor(Long.class).get().description()).hasValue("niceField");
        assertThat(i.getDescriptor(Integer.class).get().description()).hasValue("niceMethod");
    }

    /** Tests service without description on {@link Provides}. */
    @Test
    public void injectorWithoutDescription() {
        Injector i = of(c -> c.provide(new WithoutDescription()));
        assertThat(i.getDescriptor(Long.class).get().description()).isEmpty();
        assertThat(i.getDescriptor(Integer.class).get().description()).isEmpty();
    }

    private static Injector of(Consumer<? super SimpleInjectorConfigurator> consumer) {
        return Injector.of(c -> {
            c.lookup(MethodHandles.lookup());
            consumer.accept(c);
        });
    }

    static class WithDescription {

        @Provides(description = "niceField")
        public static final Long F = 0L;

        @Provides(description = "niceMethod")
        public static int m() {
            return 0;
        }
    }

    static class WithoutDescription {

        @Provides
        public static final Long F = 0L;

        @Provides
        public static int m() {
            return 0;
        }
    }
}
