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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import app.packed.inject.Provide;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.RSN;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.inject.util.InternalServiceDescriptor;
import packed.internal.util.KeyBuilder;

/**
 * A build node is used at configuration time, to make sure that multiple services with the same key are not registered.
 * And for helping in initialization dependency graphs. Build nodes has extra fields that are not needed at runtime.
 */
public abstract class BSN<T> implements ServiceNode<T> {

    /** An empty array of nodes */
    private static final ServiceNode<?>[] EMPTY_ARRAY = new ServiceNode<?>[0];

    /** The configuration site of this object. */
    private final InternalConfigSite configSite;

    /** The dependencies of this node. */
    public final List<InternalDependencyDescriptor> dependencies;

    public String description;

    /** A flag used to detect cycles in the dependency graph. */
    boolean detectCycleVisited;

    /** Whether or this node contains a dependency on {@link ProvideHelper}. */
    final boolean hasDependencyOnInjectionSite;

    /** The injector configuration this node is registered with. */
    @Nullable // Is nullable for stages for now
    protected final InjectorBuilder injectorBuilder;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has a
     * methods annotated with {@link Provide}. In which the case the declaring class might need to be constructor injected
     * before the method can be executed.
     */
    Key<T> key;

    /** The resolved dependencies of this node. */
    public final ServiceNode<?>[] resolvedDependencies;

    /** We cache the runtime node, to make sure it is only created once. */
    @Nullable
    private RSN<T> runtimeNode;

    BSN(InjectorBuilder injectorBuilder, InternalConfigSite configSite, List<InternalDependencyDescriptor> dependencies) {
        this.configSite = requireNonNull(configSite);
        this.injectorBuilder = injectorBuilder;
        this.dependencies = requireNonNull(dependencies);
        this.resolvedDependencies = dependencies.isEmpty() ? EMPTY_ARRAY : new ServiceNode<?>[dependencies.size()];
        boolean hasDependencyOnInjectionSite = false;
        if (!dependencies.isEmpty()) {
            for (InternalDependencyDescriptor e : dependencies) {
                if (e.key().equals(KeyBuilder.INJECTION_SITE_KEY)) {
                    hasDependencyOnInjectionSite = true;
                    break;
                }
            }
        }
        this.hasDependencyOnInjectionSite = hasDependencyOnInjectionSite;
    }

    @SuppressWarnings("unchecked")
    public void as(Key<? super T> key) {
        requireNonNull(key, "key is null");
        // checkConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....
        this.key = (Key<T>) key;
    }

    public final void checkResolved() {
        for (int i = 0; i < resolvedDependencies.length; i++) {
            ServiceNode<?> n = resolvedDependencies[i];
            if (n == null && !dependencies.get(i).isOptional()) {
                throw new AssertionError("Dependency " + dependencies.get(i) + " was not resolved");
            }
        }
    }

    /**
     * Returns the configuration site of this configuration.
     * 
     * @return the configuration site of this configuration
     */
    @Override
    public final InternalConfigSite configSite() {
        return configSite;
    }

    /**
     * If this node is located on another build node return the node, otherwise null. For example a method annotated with
     * {@link Provide} on a class that is itself registered as a component.
     * 
     * @return stuff
     */
    @Nullable
    BSN<?> declaringNode() {
        return null;
    }

    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public String getDescription() {
        return description;
    }

    public final Key<T> getKey() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return key;
    }

    /**
     * Returns whether or not this node has any dependencies that needs to be resolved.
     *
     * @return whether or not this node has any dependencies that needs to be resolved
     */
    @Override
    public abstract boolean needsResolving();

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    abstract RSN<T> newRuntimeNode();
    //
    // protected void onFreeze() {
    // if (key != null) {
    // if (this instanceof BuildtimeServiceNodeExported) {
    // injectorBuilder.box.services().exports.put(this);
    // } else {
    // if (!injectorBuilder.box.services().nodes.putIfAbsent(this)) {
    // System.err.println("OOPS " + key);
    // }
    // }
    // }
    // }

    public final ServiceDescriptor toDescriptor() {
        return new InternalServiceDescriptor(key, configSite, description /* immutableCopyOfTags() */);
    }

    /** {@inheritDoc} */
    @Override
    public final RSN<T> toRuntimeNode() {
        RSN<T> runtime = this.runtimeNode;
        return runtime == null ? this.runtimeNode = newRuntimeNode() : runtime;
    }
}
