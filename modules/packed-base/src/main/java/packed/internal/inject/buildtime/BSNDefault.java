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

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.inject.InjectionException;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provide;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.inject.runtime.AbstractRuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeLazyServiceNode;
import packed.internal.inject.runtime.RuntimePrototypeServiceNode;
import packed.internal.inject.runtime.RuntimeSingletonServiceNode;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.invoke.FunctionHandle;
import packed.internal.invoke.InvokableMember;
import packed.internal.util.ThrowableUtil;

/**
 * A abstract node that builds thing from a factory. This node is used for all three binding modes mainly because it
 * makes extending it with {@link ProvidedComponentConfiguration} much easier.
 */
class BSNDefault<T> extends BSN<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** An internal factory, null for nodes created from an instance. */
    @Nullable
    private FunctionHandle<T> function;

    public boolean hasInstanceMembers;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    /** The instantiation mode of this node. */
    private InstantiationMode instantionMode;

    /** The parent, if this node is the result of a member annotated with {@link Provide}. */
    private final BSNDefault<?> parent;

    public BSNDefault(InjectorBuilder injectorBuilder, ComponentConfiguration cc, InstantiationMode instantionMode, FunctionHandle<T> function,
            List<InternalDependencyDescriptor> dependencies) {
        super(injectorBuilder, (InternalConfigSite) cc.configSite(), dependencies);
        this.function = requireNonNull(function, "factory is null");
        this.parent = null;
        this.instantionMode = requireNonNull(instantionMode);

        // Maaske skal vi bare smide UnsupportedOperationException istedet for???
        // Vi faar jo problemet ved f.eks. CACHE_PER_APP.....
        // her giver det ikke meningen at faa componenten...

        // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
        // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        // }
    }

    /**
     * Creates a new node from an instance.
     * 
     * @param injectorConfiguration
     *            the injector configuration
     * @param configSite
     *            the configuration site
     * @param instance
     *            the instance
     */
    public BSNDefault(InjectorBuilder injectorConfiguration, InternalConfigSite configSite, T instance) {
        super(injectorConfiguration, configSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.parent = null;
        this.instantionMode = InstantiationMode.SINGLETON;
        this.function = null;
    }

    BSNDefault(InternalConfigSite configSite, AtProvides atProvides, FunctionHandle<T> factory, BSNDefault<?> parent) {
        super(parent.injectorBuilder, configSite, atProvides.dependencies);
        this.parent = parent;
        this.function = requireNonNull(factory, "factory is null");
        this.instantionMode = atProvides.instantionMode;
        description = atProvides.description;
    }

    @Override
    BSN<?> declaringNode() {
        return parent;
    }

    private FunctionHandle<T> fac() {
        if (parent != null) {
            InvokableMember<T> ff = (InvokableMember<T>) function;
            if (ff.isMissingInstance()) {
                function = ff.withInstance(parent.getInstance(null));
            }
        }
        return function;
    }

    /** {@inheritDoc} */
    @Override
    public final T getInstance(ProvideHelper ignore) {
        if (instantionMode == InstantiationMode.PROTOTYPE) {
            return newInstance();
        }

        T i = instance;
        if (i == null) {
            instance = i = newInstance();
        }
        return i;
    }

    public BSNDefault<T> instantiateAs(InstantiationMode mode) {
        requireNonNull(mode, "mode is null");
        this.instantionMode = mode;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final InstantiationMode instantiationMode() {
        return instantionMode;
    }

    public void lazy() {
        instantiateAs(InstantiationMode.LAZY);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean needsInjectionSite() {
        return hasDependencyOnInjectionSite;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean needsResolving() {
        return !dependencies.isEmpty();
    }

    private T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        int size = dependencies.size();
        if (size > 0) {
            params = new Object[size];
            for (int i = 0; i < resolvedDependencies.length; i++) {
                requireNonNull(resolvedDependencies[i]);
                params[i] = resolvedDependencies[i].getInstance(injectorBuilder == null ? null : injectorBuilder.resolver.publicInjector, dependencies.get(i),
                        null);
            }
        }
        Object o;
        MethodHandle mh = fac().toMethodHandle();

        try {
            o = mh.invokeWithArguments(params);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new InjectionException("foo", e);
        }
        @SuppressWarnings("unchecked")
        T t = (T) o;
        requireNonNull(t);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    final AbstractRuntimeServiceNode<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new RuntimeSingletonServiceNode<>(this, i);
        }

        if (parent == null || parent.instantiationMode() == InstantiationMode.SINGLETON || parent.instance != null
                || (function instanceof InvokableMember && !((InvokableMember<?>) function).isMissingInstance())) {
            if (instantionMode == InstantiationMode.PROTOTYPE) {
                return new RuntimePrototypeServiceNode<>(this, fac());
            } else {
                return new RuntimeLazyServiceNode<>(this, fac(), null);
            }
        }
        // parent==LAZY and not initialized, this.instantionMode=Lazy or Prototype

        return new RuntimeLazyServiceNode<>(this, fac(), null);

    }

    public void prototype() {
        if (hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
        if (hasInstanceMembers) {
            throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
        }
        instantiateAs(InstantiationMode.PROTOTYPE);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BSN<?> provide(AtProvides atProvides) {
        InternalConfigSite icss = configSite().thenAnnotatedMember(ConfigSiteType.INJECTOR_PROVIDE, atProvides.provides, atProvides.member);

        InvokableMember<?> fi = atProvides.invokable;
        if (!atProvides.isStaticMember) {
            // getInstance(null);
            // fi = fi.withInstance(this.instance);
        }

        BSNDefault<?> node = new BSNDefault<>(icss, atProvides, fi, this);
        node.as((Key) atProvides.key);
        return node;
    }

    @Override
    public final String toString() {
        return function == null ? String.valueOf(instance) : function.toString();
    }
}
