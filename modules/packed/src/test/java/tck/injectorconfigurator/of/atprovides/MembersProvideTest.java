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

import app.packed.service.Key;
import app.packed.service.ProvideService;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceLocator.Composer;
import testutil.stubs.annotation.StringQualifier;

/**
 * Tests {@link ProvideService} on fields and methods.
 */
public class MembersProvideTest {

    @Test
    public void fieldsAndMethods() {
        validate(of(c -> c.provideInstance(new VisibilityStatic())));
        validate(of(c -> c.provide(VisibilityStatic.class)));
        // validate(of(c -> c.provide(VisibilityStatic.class).lazy()));
        validate(of(c -> c.providePrototype(VisibilityStatic.class)));
    }

    private static ServiceLocator of(Consumer<? super Composer> consumer) {
        return ServiceLocator.of(c -> {
            c.lookup(MethodHandles.lookup());
            consumer.accept(c);
        });
    }

    private static void validate(ServiceLocator i) {
        assertThat(i.use(new Key<@StringQualifier("f_package") String>() {})).isEqualTo("package_f");
        assertThat(i.use(new Key<@StringQualifier("f_private") String>() {})).isEqualTo("private_f");
        assertThat(i.use(new Key<@StringQualifier("f_protected") String>() {})).isEqualTo("protected_f");
        assertThat(i.use(new Key<@StringQualifier("f_public") String>() {})).isEqualTo("public_f");

        assertThat(i.use(new Key<@StringQualifier("m_package") String>() {})).isEqualTo("package_m");
        assertThat(i.use(new Key<@StringQualifier("m_public") String>() {})).isEqualTo("public_m");
        assertThat(i.use(new Key<@StringQualifier("m_protected") String>() {})).isEqualTo("protected_m");
        assertThat(i.use(new Key<@StringQualifier("m_private") String>() {})).isEqualTo("private_m");
    }

public  static class VisibilityNonStatic {

        @ProvideService
        @StringQualifier("f_package")
        final String F_PACKAGE = "package_f";

        @ProvideService
        @StringQualifier("f_private")
        private final String F_PRIVATE = "private_f";

        @ProvideService
        @StringQualifier("f_protected")
        protected final String F_PROTECTED = "protected_f";

        @ProvideService
        @StringQualifier("f_public")
        public final String F_PUBLIC = "public_f";

        @ProvideService
        @StringQualifier("m_package")
        String m_package() {
            return "package_m";
        }

        @ProvideService
        @StringQualifier("m_private")
        private String m_private() {
            return "private_m";
        }

        @ProvideService
        @StringQualifier("m_protected")
        protected String m_protected() {
            return "protected_m";
        }

        @ProvideService
        @StringQualifier("m_public")
        public String m_public() {
            return "public_m";
        }
    }

public  static class VisibilityStatic {

        @ProvideService
        @StringQualifier("f_package")
        static final String F_PACKAGE = "package_f";

        @ProvideService
        @StringQualifier("f_private")
        private static final String F_PRIVATE = "private_f";

        @ProvideService
        @StringQualifier("f_protected")
        protected static final String F_PROTECTED = "protected_f";

        @ProvideService
        @StringQualifier("f_public")
        public static final String F_PUBLIC = "public_f";

        @ProvideService
        @StringQualifier("m_package")
        static String m_package() {
            return "package_m";
        }

        @ProvideService
        @StringQualifier("m_private")
        private static String m_private() {
            return "private_m";
        }

        @ProvideService
        @StringQualifier("m_protected")
        protected static String m_protected() {
            return "protected_m";
        }

        @ProvideService
        @StringQualifier("m_public")
        public static String m_public() {
            return "public_m";
        }
    }
}
