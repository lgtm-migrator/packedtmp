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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.InjectionSite;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.util.AbstractConfiguration;
import packed.internal.util.KeyBuilder;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A build node is used at configuration time, to make sure that multiple services with the same key are not registered.
 * And for helping in initialization dependency graphs. Build nodes has extra fields that are not needed at runtime.
 */
public abstract class ServiceBuildNode<T> extends AbstractConfiguration implements ServiceNode<T>, ServiceConfiguration<T> {

    /** An empty array of nodes */
    private static final ServiceNode<?>[] EMPTY_ARRAY = new ServiceNode<?>[0];

    /** The dependencies of this node. */
    public final List<InternalDependency> dependencies;

    /** A flag used to detect cycles in the dependency graph. */
    boolean detectCycleVisited;

    /** Whether or this node contains a dependency on {@link InjectionSite}. */
    final boolean hasDependencyOnInjectionSite;

    /** The injector configuration this node is registered with. */
    protected final InjectorBuilder injectorBuilder;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has a
     * methods annotated with {@link Provides}. In which the case the declaring class might need to be constructor injected
     * before the method can be executed.
     */
    @Nullable
    private Key<T> key;

    /** The resolved dependencies of this node. */
    public final ServiceNode<?>[] resolvedDependencies;

    /** We cache the runtime node, to make sure it is only created once. */
    @Nullable
    private RuntimeServiceNode<T> runtimeNode;

    ServiceBuildNode(InjectorBuilder injectorBuilder, InternalConfigurationSite configurationSite, List<InternalDependency> dependencies) {
        super(configurationSite);
        this.injectorBuilder = requireNonNull(injectorBuilder);
        this.dependencies = requireNonNull(dependencies);
        this.resolvedDependencies = dependencies.isEmpty() ? EMPTY_ARRAY : new ServiceNode<?>[dependencies.size()];

        boolean hasDependencyOnInjectionSite = false;
        if (!dependencies.isEmpty()) {
            for (InternalDependency e : dependencies) {
                if (e.getKey().equals(KeyBuilder.INJECTION_SITE_KEY)) {
                    hasDependencyOnInjectionSite = true;
                    break;
                }
            }
        }
        this.hasDependencyOnInjectionSite = hasDependencyOnInjectionSite;
    }

    @Override
    public ServiceBuildNode<T> as(Class<? super T> key) {
        requireNonNull(key, "key is null");
        return as(Key.of(key));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public ServiceBuildNode<T> as(Key<? super T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....
        this.key = (Key<T>) key;
        return this;
    }

    @Override
    public ServiceConfiguration<?> asNone() {
        checkConfigurable();
        key = null;
        return this;
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
     * If this node is located on another build node return the node, otherwise null. For example a method annotated with
     * {@link Provides} on a class that is itself registered as a component.
     * 
     * @return
     */
    @Nullable
    ServiceBuildNode<?> declaringNode() {
        return null;
    }

    @Override
    public final void freeze() {
        super.freeze();
    }

    /** {@inheritDoc} */
    @Override
    public final Key<T> getKey() {
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
    abstract RuntimeServiceNode<T> newRuntimeNode();

    /** {@inheritDoc} */
    @Override
    public ServiceBuildNode<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final RuntimeServiceNode<T> toRuntimeNode() {
        RuntimeServiceNode<T> runtime = this.runtimeNode;
        return runtime == null ? this.runtimeNode = newRuntimeNode() : runtime;
    }
}
