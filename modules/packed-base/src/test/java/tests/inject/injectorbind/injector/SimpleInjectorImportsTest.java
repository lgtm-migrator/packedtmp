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
package tests.inject.injectorbind.injector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static support.assertj.Assertions.npe;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import app.packed.inject2.ServiceWiringOperations;
import app.packed.util.Key;
import packed.internal.inject.ServiceWiringImportOperation;
import support.stubs.annotation.Left;
import support.stubs.annotation.Right;

/** Tests the {@link InjectorConfigurator#wireInjector(Injector, ServiceWiringImportOperation...)} method. */
public class SimpleInjectorImportsTest {

    /** Tests various null arguments. */
    @Test
    public void nullArguments() {
        Injector i = Injector.of(c -> c.provide("X"));
        npe(() -> Injector.of(c -> c.wireInjector((Injector) null)), "injector");
        npe(() -> Injector.of(c -> c.wireInjector(i, (ServiceWiringImportOperation[]) null)), "operations");

        // TODO test error message
        assertThatNullPointerException().isThrownBy(() -> Injector.of(c -> c.wireInjector(i, ServiceWiringOperations.NO_IMPORTS, null)));
    }

    /** Tests that we can import no services. */
    @Test
    public void import0() {
        Injector i1 = Injector.of(c -> {
            c.provide("X");
            c.provide(123);
        });

        Injector i = Injector.of(c -> {
            c.wireInjector(i1, ServiceWiringOperations.NO_IMPORTS);
        });
        assertThat(i.services().count()).isEqualTo(0L);
    }

    /** Tests that we can import a single service. */
    @Test
    public void import1() {
        Injector i1 = Injector.of(c -> c.provide("X"));

        Injector i = Injector.of(c -> c.wireInjector(i1));
        assertThat(i.with(String.class)).isEqualTo("X");

    }

    /** Tests that we can chain stages. */
    @Test
    public void rebindChaining() {
        Injector i1 = Injector.of(c -> c.provide("X"));

        Injector i = Injector.of(c -> {
            c.wireInjector(i1, ServiceWiringOperations.rebindImport(new Key<String>() {}, new Key<@Left String>() {}),
                    ServiceWiringOperations.rebindImport(new Key<@Left String>() {}, new Key<@Right String>() {}));
        });
        assertThat(i.hasService(String.class)).isFalse();
        assertThat(i.hasService(new Key<@Left String>() {})).isFalse();
        assertThat(i.with(new Key<@Right String>() {})).isEqualTo("X");
    }

    /** Tests that we can rebind imported services. */
    @Test
    public void rebindImports() {
        Injector i1 = Injector.of(c -> c.provide("X"));
        Injector i2 = Injector.of(c -> c.provide("Y"));

        Injector i = Injector.of(c -> {
            c.wireInjector(i1, ServiceWiringOperations.rebindImport(new Key<String>() {}, new Key<@Left String>() {}));
            c.wireInjector(i2, ServiceWiringOperations.rebindImport(new Key<String>() {}, new Key<@Right String>() {}));
        });

        assertThat(i.with(new Key<@Left String>() {})).isEqualTo("X");
        assertThat(i.with(new Key<@Right String>() {})).isEqualTo("Y");
    }

    /** Tests that we can switch keys of two imported services. */
    @Test
    public void rebindImports2() {
        Injector i1 = Injector.of(c -> c.provide("X").as(new Key<@Left String>() {}));
        Injector i2 = Injector.of(c -> c.provide("Y").as(new Key<@Right String>() {}));

        Injector i = Injector.of(c -> {
            c.wireInjector(i1);
            c.wireInjector(i2);
        });
        assertThat(i.with(new Key<@Left String>() {})).isEqualTo("X");
        assertThat(i.with(new Key<@Right String>() {})).isEqualTo("Y");

        // Now let us switch them around
        i = Injector.of(c -> {
            c.wireInjector(i1, ServiceWiringOperations.rebindImport(new Key<@Left String>() {}, new Key<@Right String>() {}));
            c.wireInjector(i2, ServiceWiringOperations.rebindImport(new Key<@Right String>() {}, new Key<@Left String>() {}));
        });

        assertThat(i.with(new Key<@Left String>() {})).isEqualTo("Y");
        assertThat(i.with(new Key<@Right String>() {})).isEqualTo("X");
    }
}
