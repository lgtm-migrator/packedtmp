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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Supplier;

import packed.inject.factory.InternalFactory0;

/**
 * A {@link Factory} type that uses a {@link Supplier} to create new instances.
 * <p>
 * Is typically used like this:
 *
 * <pre> {@code
 * Factory<Long> f = new Factory0<>(System::currentTimeMillis) {}).setDescription("Startup Time");}</pre>
 * <p>
 * Note that we create a new class inheriting from Factory0 is done in order to information about the type variable.
 *
 * As an alternative to using the constructor of this class, one of the static factory methods can be used:
 *
 * <pre> {@code
 * Factory<Long> f = Factory0.of(System::currentTimeMillis, Long.class)).setDescription("Startup Time")}</pre>
 *
 * The factory created in the last example is functionally equivalent to the factory created in the first example.
 *
 * @param <T>
 *            the type of objects this factory constructs
 * @see Factory1
 * @see Factory2
 */
public abstract class Factory0<T> extends Factory<T> {

    /**
     * Creates a new factory.
     *
     * @param supplier
     *            the supplier to use for creating new instances
     * @throws NullPointerException
     *             if the specified supplier is null
     * @throws IllegalArgumentException
     *             if the type variable T could not be determined. Or if T is an invalid value, for example, {@link Optional}
     */
    protected Factory0(Supplier<? extends T> supplier) {
        super(supplier);
    }

    /**
     * Returns a factory that uses the specified supplier to create new instances.
     *
     * @param <T>
     *            the type of objects the factory will create
     * @param supplier
     *            the supplier to use for creating new instances
     * @param objectType
     *            the type of objects the supplier creates
     * @return a factory that uses the specified supplier to create new instances
     * @throws NullPointerException
     *             if the specified supplier or object type is null
     */
    public static <T> Factory<T> of(Supplier<? extends T> supplier, Class<T> objectType) {
        return of(supplier, TypeLiteral.of(requireNonNull(objectType, "objectType is null")));
    }

    /**
     * Returns a factory that uses the specified supplier to create new instances.
     *
     * @param <T>
     *            the type of objects the factory will create
     * @param supplier
     *            the supplier to use for creating new instances
     * @param objectType
     *            the type of objects the supplier creates
     * @return a factory that uses the specified supplier to create new instances
     * @throws NullPointerException
     *             if the specified supplier or object type is null
     */
    public static <T> Factory<T> of(Supplier<? extends T> supplier, TypeLiteralOrKey<T> typeLiteralOrKey) {
        return new Factory<>(new InternalFactory0<>(supplier, typeLiteralOrKey));
    }
}
