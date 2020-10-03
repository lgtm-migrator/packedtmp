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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import app.packed.base.TypeLiteral;
import app.packed.inject.Factory1;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.methodhandle.LookupUtil;

/** A factory handle for {@link Factory1}. */
final class Factory1FactoryHandle<T, R> extends FactoryHandle<R> {

    /** A method handle for {@link Function#apply(Object)}. */
    private static final MethodHandle APPLY = LookupUtil.lookupVirtualPublic(Function.class, "apply", Object.class, Object.class);

    /** A cache of extracted type variables and dependencies from subclasses of this class. */
    private static final ClassValue<Entry<TypeLiteral<?>, List<DependencyDescriptor>>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Entry<TypeLiteral<?>, List<DependencyDescriptor>> computeValue(Class<?> type) {
            return new SimpleImmutableEntry<>(TypeLiteral.fromTypeVariable((Class) type, BaseFactory.class, 0),
                    DependencyDescriptor.fromTypeVariables((Class) type, Factory1.class, 0));
        }
    };
    private final List<DependencyDescriptor> dependencies;

    /** The function that creates the actual objects. */
    private final Function<? super T, ? extends R> function;

    private Factory1FactoryHandle(TypeLiteral<R> type, Function<? super T, ? extends R> function, List<DependencyDescriptor> dependencies) {
        super(type);
        this.function = requireNonNull(function, "function is null");
        this.dependencies = dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public List<DependencyDescriptor> dependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle(Lookup ignore) {
        return APPLY.bindTo(function);
    }

    /**
     * Creates a new factory support instance from an implementation of this class and a function.
     * 
     * @param implementation
     *            the class extending this class
     * @param function
     *            the function used for creating new values
     * @return a new factory support instance
     */
    @SuppressWarnings("unchecked")
    static <T, R> FactoryHandle<R> create(Class<?> implementation, Function<?, ? extends T> function) {
        Entry<TypeLiteral<?>, List<DependencyDescriptor>> fs = CACHE.get(implementation);
        return new Factory1FactoryHandle<>((TypeLiteral<R>) fs.getKey(), (Function<? super T, ? extends R>) function, fs.getValue());
    }
}
