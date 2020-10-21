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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import app.packed.base.AttributedElementStream;
import app.packed.base.Key;
import app.packed.inject.Service;
import app.packed.inject.ServiceRegistry;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.util.PackedAttributeHolderStream;

/** An abstract implementation of ServiceRegistry. */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    /**
     * Subclasses must extend this method and provide an immutable service map.
     * 
     * @return an immutable map containing all services
     */
    protected abstract Map<Key<?>, Service> services();

    /** {@inheritDoc} */
    @Override
    public final Optional<Service> findService(Class<?> key) {
        return findService(Key.of(key));
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<Service> findService(Key<?> key) {
        requireNonNull(key, "key is null");
        Service s = services().get(key);
        return Optional.ofNullable(s);
    }

    /** {@inheritDoc} */
    @Override
    public final void forEach(Consumer<? super Service> action) {
        services().values().forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEmpty() {
        return services().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean contains(Key<?> key) {
        requireNonNull(key, "key is null");
        return services().containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<Service> iterator() {
        return services().values().iterator();
    }

    /** {@inheritDoc} */
    @Override
    public final Set<Key<?>> keys() {
        return services().keySet();
    }

    /** {@inheritDoc} */
    @Override
    public final int size() {
        return services().size();
    }

    /** {@inheritDoc} */
    @Override
    public final Spliterator<Service> spliterator() {
        return services().values().spliterator();
    }

    /** {@inheritDoc} */
    @Override
    public final AttributedElementStream<Service> stream() {
        return new PackedAttributeHolderStream<>(services().values().stream());
    }

    /** {@inheritDoc} */
    @Override
    public final List<Service> toList() {
        return List.copyOf(services().values());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return services().toString();
    }

    /**
     * Creates a new service registry by making an immutable copy of the specified service map.
     * 
     * @param map
     *            the map to make an immutable copy
     * @return a new service registry
     */
    public static ServiceRegistry copyOf(Map<Key<?>, ? extends ServiceBuild> map) {
        LinkedHashMap<Key<?>, Service> l = new LinkedHashMap<Key<?>, Service>();
        for (ServiceBuild e : map.values()) {
            l.put(e.key(), e.toService());
        }
        return new CopyOfRegistry(l);
    }

    /** The registry implementation returned by {@link #copyOf(Map)}. */
    private static final class CopyOfRegistry extends AbstractServiceRegistry {

        /** The services that are wrapped */
        private final Map<Key<?>, Service> services;

        private CopyOfRegistry(Map<Key<?>, Service> services) {
            // TODO does not maintain order?
            this.services = Map.copyOf(services); // We want the map to immutable
        }

        /** {@inheritDoc} */
        @Override
        protected Map<Key<?>, Service> services() {
            return services;
        }
    }
}
