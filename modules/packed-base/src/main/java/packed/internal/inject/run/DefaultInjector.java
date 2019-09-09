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
package packed.internal.inject.run;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.util.ServiceNodeMap;
import packed.internal.util.KeyBuilder;

/** The default implementation of {@link Injector}. */
public final class DefaultInjector extends AbstractInjector {

    /** The configuration site of this injector. */
    private final ConfigSite configSite;

    /** An optional description of the injector. */
    @Nullable
    private final String description;

    /** The parent of this injector, or null if this is a top-level injector. */
    @Nullable
    final AbstractInjector parent;

    /** All services that this injector provides. */
    private final ServiceNodeMap services;

    public DefaultInjector(ConfigSite configSite, @Nullable String description, ServiceNodeMap services) {
        this.parent = null;
        this.configSite = requireNonNull(configSite);
        this.description = description;
        this.services = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return configSite;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    protected void failedGet(Key<?> key) {
        // Oehhh hvad med internal injector, skal vi have en reference til den.
        // Vi kan jo saadan set GC'en den??!?!?!?
        // for (ServiceNode<?> n : services) {
        // System.out.println("Failed Get " + n);
        // if (n instanceof RuntimeNode<T>)
        // }
        super.failedGet(key);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    protected <T> ServiceEntry<T> findNode(Key<T> key) {
        return (ServiceEntry<T>) services.nodes.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public void forEachServiceEntry(Consumer<? super ServiceEntry<?>> action) {
        services.nodes.values().forEach(action);

    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Stream<ServiceDescriptor> services() {
        return (Stream) services.nodes.values().stream().filter(e -> !e.key().equals(KeyBuilder.INJECTOR_KEY)).map(e -> {
            if (e instanceof BuildEntry) {
                return ((BuildEntry) e).toDescriptor();
            }
            return e;
        });
    }

    /** {@inheritDoc} */
    @Override
    public Injector spawn(Wirelet... wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return this;
        }
        // TODO Auto-generated method stub
        return null;
    }
}
