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
package packed.internal.util;

import packed.internal.thirdparty.util.ValueWeakHashMap;

/**
 *
 */
public abstract class ModuleValue<T> {

    /** The cache of values. */
    final ValueWeakHashMap<Module, T> cache = new ValueWeakHashMap<>();

    /**
     * Computes the given lookup objects's derived value for this {@code LookupValue}.
     * <p>
     * This method will be invoked within the first thread that accesses the value with the {@link #get get} method.
     * <p>
     * If this method throws an exception, the corresponding call to {@code get} will terminate abnormally with that
     * exception, and no lookup value will be recorded.
     *
     * @param module
     *            the module for which a value must be computed
     * @return the newly computed value associated with this {@code ModuleValue}, for the given module object
     */
    protected abstract T computeValue(Module module);

    /**
     * Returns the value for the given lookup object. If no value has yet been computed, it is obtained by an invocation of
     * the {@link #computeValue computeValue} method.
     * 
     * @param module
     *            the module object
     * @return the value for the given moduleobject
     */
    public final T get(Module module) {
        throw new UnsupportedOperationException();
    }
}

abstract class ClassValueModule<T> extends ClassValue<T> {

    /** The cache of values. */
    final ValueWeakHashMap<Module, T> cache = new ValueWeakHashMap<>();

    /** {@inheritDoc} */
    @Override
    protected final T computeValue(Class<?> type) {
        // computeIfAbsent....
        return cache.get(type.getModule());

        // If we need to call computeValueForModule...
        //// We should check for stale items as well....
    }

    protected abstract T computeValueForModule(Module module);
}