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
package packed.internal.attribute;

import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.attribute.Attribute;
import app.packed.attribute.AttributeSet;

/**
 *
 */
public final class EmptyAttributeSet implements AttributeSet {

    public static final EmptyAttributeSet EMPTY = new EmptyAttributeSet();

    private EmptyAttributeSet() {}

    /** {@inheritDoc} */
    @Override
    public Set<Attribute<?>> attributes() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Attribute<?>, Object>> entrySet() {
        return Set.of();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T get(Attribute<T> attribute) {
        throw new NoSuchElementException();
    }

    /** {@inheritDoc} */
    @Override
    public <T> void ifPresent(Attribute<T> attribute, Consumer<T> action) {}

    /** {@inheritDoc} */
    @Override
    public <T> void ifPresentOrElse(Attribute<T> attribute, Consumer<? super T> action, Runnable emptyAction) {
        emptyAction.run();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(Attribute<?> attribute) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T orElse(Attribute<T> attribute, T other) {
        return other;
    }
}
