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
package packed.internal.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import app.packed.artifact.ArtifactContext;
import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.lifecycleold.StopOption;
import app.packed.service.Injector;
import packed.internal.component.ComponentNode;

/** Used to expose a container as an ArtifactContext. */
public final class PackedArtifactContext implements ArtifactContext {

    /** The component node we are wrapping. */
    private final ComponentNode component;

    public PackedArtifactContext(ComponentNode container) {
        this.component = requireNonNull(container);
    }

    public Collection<Component> children() {
        return component.children();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return component.configSite();
    }

    public int depth() {
        return component.depth();
    }

    /** {@inheritDoc} */
    @Override
    public Injector injector() {
        return (Injector) component.data[0];
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return component.name();
    }

    public Optional<Component> parent() {
        return component.parent();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return component.path();
    }

    /** {@inheritDoc} */
    @Override
    public void stop(StopOption... options) {

    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(Option... options) {
        return component.stream(options);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Key<T> key) {
        return injector().use(key);
    }

    /** {@inheritDoc} */
    @Override
    public Component useComponent(CharSequence path) {
        return component.useComponent(path);
    }

    public ComponentRelation relationTo(Component other) {
        return component.relationTo(other);
    }
}
