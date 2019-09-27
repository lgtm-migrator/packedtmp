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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.ContainerConfiguration;
import app.packed.contract.Contract;
import app.packed.hook.HookGroupBuilder;
import packed.internal.container.extension.ExtensionComposerContext;
import packed.internal.util.StringFormatter;

/**
 *
 */
public abstract class ExtensionComposer<T extends ComposableExtension<?>> {

    /** The context that all calls are delegated to, must only be accessed via {@link #context}. */
    private ExtensionComposerContext context;

    protected final <E extends Contract> void addContract(Class<E> contractType, BiFunction<T, ExtensionPipelineContext, E> contractFactory) {
        // -> BiFunction(Extension, DescriptorContextWithPipelines)
        requireNonNull(contractType, "contractType is null");
        requireNonNull(contractFactory, "contractFactory is null");
        context().contracts.putIfAbsent(contractType, contractFactory);
    }

    /**
     * Adds the specified extension types to the set of extensions that this extension depends on.
     * 
     * 
     * This is done in order
     * 
     * @param extensionTypes
     *            the types of extension the extension uses.
     */
    @SafeVarargs
    protected final void addDependencies(Class<? extends Extension>... extensionTypes) {

    }

    final void addDependencies(String... extensionTypes) {
        // The names will be resolved when composer is created

    }

    protected final <B extends HookGroupBuilder<G>, G> void addHookGroup(Class<B> builderType, BiConsumer<T, G> groupConsumer) {
        // OnHookGroup
    }

    protected final <E extends ExtensionWireletPipeline<E, ?>> void addPipeline(Class<E> pipelineType, Function<T, E> pipelineFactory) {
        requireNonNull(pipelineType, "pipelineType is null");
        requireNonNull(pipelineFactory, "pipelineFactory is null");
        // Validation??? Pipeline model...
        context().pipelines.putIfAbsent(pipelineType, pipelineFactory);
    }

    @SuppressWarnings("unchecked")
    protected final void buildBundleDescriptor(BiConsumer<? super T, ? super BundleDescriptor.Builder> builder) {
        context().builder = (BiConsumer<? super Extension, ? super Builder>) requireNonNull(builder, "builder is null");
    }

    /** Configures the composer. */
    protected abstract void configure();

    // /**
    // * Returns the extension's extension node. This method will be invoked exactly once by the runtime and must return a
    // * non-null value of the exact same type as the single type parameter to ComposableExtension.
    // *
    // * @return the extension's extension node
    // */
    private ExtensionComposerContext context() {
        ExtensionComposerContext c = context;
        if (c == null) {
            throw new IllegalStateException(
                    "This method can only be called from within the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    /**
     * Invoked by the runtime to start the configuration process.
     * 
     * @param context
     *            the context to wrap
     */
    final void doConfigure(ExtensionComposerContext context) {
        this.context = context;
        // Im not sure we want to null it out...
        // We should have some way to mark it failed????
        // If configure() fails. The ContainerConfiguration still works...
        /// Well we should probably catch the exception from where ever we call his method
        try {
            configure();
        } finally {
            this.context = null;
        }
    }

    /**
     * Registers a (callback) action that is invoked (by the runtime) immediately after an extension has been instantiated
     * and added to the configuration of the container, but before it is returned to the user. This method is typically
     * invoked as the result of a user calling {@link ContainerConfiguration#use(Class)}.
     * <p>
     * This method is useful in situations where ...
     * <p>
     * The newly instantiated extension is returned to the user immediately after the specified action completes.
     * <p>
     * Subsequent invocations of this method will schedule each action in order of invocation.
     * 
     * @param action
     *            The action to be performed after the extension has been added
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void onAdd(Consumer<? super T> action) {
        requireNonNull(action, "action is null");
        Consumer<? super T> a = context().onAddAction;
        context().onAddAction = a == null ? (Consumer) action : a.andThen((Consumer) action);
    }

    /**
     * A callback method that is invoked immediately after a container has been successfully configured. This is typically
     * after {@link Bundle#configure()} has returned.
     * <p>
     * <p>
     * The default implementation of this method does nothing.
     */
    // If the container contains multiple extensions. They are invoked in reverse order. If E2 has a dependency on E1.
    // E2.onConfigured() will be invoked before E1.onConfigure(). This is done in order to allow extensions to perform
    // additional configuration on other extension after user code has been executed
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void onConfigured(Consumer<? super T> action) {
        requireNonNull(action, "action is null");
        Consumer<? super T> a = context().onConfiguredAction;
        context().onConfiguredAction = a == null ? (Consumer) action : a.andThen((Consumer) action);
    }

    // addNode??
    protected final <E extends ExtensionNode<T>> void useNode(Class<E> nodeType, Function<T, E> nodeFactory) {
        requireNonNull(nodeType, "nodeType is null");
        requireNonNull(nodeFactory, "nodeFactory is null");
        if (!Modifier.isFinal(nodeType.getModifiers())) {
            throw new ExtensionDeclarationException("The extension node type must be declared final, node type = " + StringFormatter.format(nodeType));
        }
        context().nodeType = nodeType;
        context().nodeFactory = nodeFactory;
    }
}
