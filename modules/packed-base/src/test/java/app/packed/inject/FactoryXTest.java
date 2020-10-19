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
package app.packed.inject;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static testutil.assertj.Assertions.checkThat;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import app.packed.base.TypeLiteral;

/** Tests {@link Factory}. */
public class FactoryXTest {

    /** Tests that we can capture information about a simple factory producing {@link Integer} instances. */
    @Test
    public void integerFactory0() {
        Factory<Integer> f = new Factory<>(() -> 1) {};
        checkThat(f).is(Integer.class);
        checkThat(f).hasNoDependencies();

        f = new Intermediate<String, Integer, Long>(() -> 123) {};
        checkThat(f).is(Integer.class);
        checkThat(f).hasNoDependencies();
    }

    /** Tests that we can capture information about a simple factory producing lists of integers instances. */
    @Test
    public void listIntegerFactory0() {
        Factory<List<Integer>> f = new Factory<>(() -> List.of(1)) {};
        checkThat(f).is(new TypeLiteral<List<Integer>>() {});
        checkThat(f).hasNoDependencies();

        f = new Intermediate<>(() -> List.of(1)) {};
        checkThat(f).is(new TypeLiteral<List<Integer>>() {});
        checkThat(f).hasNoDependencies();
    }

    /**
     * Tests that we fail if we cannot read the type variable. We test it both when directing inheriting from Factory0, and
     * via an intermediate class.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void typeParameterIndeterminable() {
        // TODO change to Factory instead of BaseFactory
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Factory(() -> 1) {}).withNoCause()
                .withMessageStartingWith("Cannot determine type variable <T> for Factory<T> on class app.packed.inject.");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Intermediate(() -> 1) {}).withNoCause()
                .withMessageStartingWith("Cannot determine type variable <T> for Factory<T> on class app.packed.inject.Factory");
    }

    @Test
    public void fff() throws Throwable {
        @SuppressWarnings("rawtypes")
        Supplier s = () -> 23;
        @SuppressWarnings("unchecked")
        Factory<?> f = new Factory<String>(s) {};
        try {
            f.toMethodHandle(null).invoke();
        } catch (FactoryException ok) {}
    }

    /** Check that we can have an intermediate abstract class. */
    static abstract class Intermediate<S, T, R> extends Factory<T> {
        protected Intermediate(Supplier<T> supplier) {
            super(supplier);
        }
    }
}
