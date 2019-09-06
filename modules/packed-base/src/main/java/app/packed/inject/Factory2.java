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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import app.packed.util.InvalidDeclarationException;
import app.packed.util.TypeLiteral;
import packed.internal.inject.factoryhandle.Factory2FactoryHandle;

/**
 * A {@link Factory} type that takes two dependencies and uses a {@link BiFunction} to create new instances. The input
 * to the bi-function being the two dependencies.
 * 
 * @see Factory0
 * @see Factory1
 */
public abstract class Factory2<T, U, R> extends Factory<R> {

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<Entry<TypeLiteral<?>, List<ServiceDependency>>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Entry<TypeLiteral<?>, List<ServiceDependency>> computeValue(Class<?> type) {
            return new SimpleImmutableEntry<>(TypeLiteral.fromTypeVariable((Class) type, Factory.class, 0),
                    ServiceDependency.fromTypeVariables((Class) type, Factory2.class, 0, 1));
        }
    };

    /**
     * Creates a new factory.
     *
     * @param function
     *            the function to use for creating new instances
     * @throws InvalidDeclarationException
     *             if the type variable T, U or R could not be determined. Or if T or U does not represent a proper
     *             dependency, or R does not represent a proper key
     */
    protected Factory2(BiFunction<? super T, ? super U, ? extends R> function) {
        super(function);
    }

    /**
     * Creates a new factory support instance from an implementation of this class and a (bi) function.
     * 
     * @param implementation
     *            the class extending this class
     * @param function
     *            the (bi) function used for creating new values
     * @return a new factory support instance
     */
    @SuppressWarnings("unchecked")
    static <T, U, R> FactorySupport<R> create(Class<?> implementation, BiFunction<?, ?, ? extends T> function) {
        Entry<TypeLiteral<?>, List<ServiceDependency>> fs = CACHE.get(implementation);
        return new FactorySupport<>(new Factory2FactoryHandle<>((TypeLiteral<R>) fs.getKey(), (BiFunction<? super T, ? super U, ? extends R>) function),
                fs.getValue());
    }
}
