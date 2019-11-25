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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.api.Contract;
import app.packed.lang.Nullable;
import packed.internal.container.MutableWireletList;
import packed.internal.container.extension.AbstractExtensionModelBuilder;

/**
 * An extension composer is used for specifying how an extension works.
 */
// Ville vaere rart at kunne gruppe metoderne efter et system og et prefix
// I think move to Extension when done... any rename to Composer
public abstract class ExtensionComposer<E extends Extension> {

    /** The context that all calls are delegated to, must only be accessed via {@link #context}. */
    private AbstractExtensionModelBuilder context;

    protected final <P extends ExtensionWirelet.Pipeline<E, P, W>, W extends ExtensionWirelet<P>> void addPipeline(Class<P> pipelineType,
            BiFunction<E, MutableWireletList<W>, P> pipelineFactory) {
        requireNonNull(pipelineType, "pipelineType is null");
        requireNonNull(pipelineFactory, "pipelineFactory is null");
        // Validation??? Pipeline model...
        context().pipelines.putIfAbsent(pipelineType, pipelineFactory);
    }

    /**
     * Will process each extension top down..
     * 
     * @param action
     *            the action to perform
     */
    protected final void completeEach(Consumer<? super E> action) {

    }

    /** Configures the composer. This method is invoked exactly once for a given implementation. */
    protected abstract void configure();

    /**
     * Returns the context object that this composer wraps.
     * 
     * @return the context object that this composer wraps.
     * @throws IllegalStateException
     *             if called outside {@link #configure()}
     */
    private AbstractExtensionModelBuilder context() {
        AbstractExtensionModelBuilder c = context;
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
    final void doConfigure(AbstractExtensionModelBuilder context) {
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
     * Exposes a contract of the specified type.
     * <p>
     * if no pipeline of the specified type is available then null is passed as the second argument to {@link BiFunction}.
     * 
     * <p>
     * If the specified contract factory does not return a non-null object of the specified contract type when invoked, the
     * runtime will will throw a {@link InternalExtensionException}.
     * 
     * @param <C>
     *            the type of contract to expose
     * @param contractType
     *            the type of contract the factory creates
     * @param contractFactory
     *            a factory for creating the contract
     * @throws InternalExtensionException
     *             if trying to register a contract type that has already been registered with another extension
     */
    protected final <C extends Contract, T extends ExtensionWirelet.Pipeline<?, ?, ?>> void exposeContract(Class<C> contractType, Class<T> pipelineType,
            BiFunction<? super E, @Nullable T, C> contractFactory) {
        // check pipelinetype is registered
        requireNonNull(contractType, "contractType is null");
        requireNonNull(contractFactory, "contractFactory is null");
        context().contracts.putIfAbsent(contractType, contractFactory);
    }

    protected final <C extends Contract> void exposeContract(Class<C> contractType, Function<? super E, C> contractFactory) {
        requireNonNull(contractType, "contractType is null");
        requireNonNull(contractFactory, "contractFactory is null");
        context().contracts.putIfAbsent(contractType, contractFactory);
    }

    @SuppressWarnings("unchecked")
    // Den er ikke saerlig god den her....
    //// Kan bedre lide exposeContract
    protected final void exposeDescriptor(BiConsumer<? super E, ? super BundleDescriptor.Builder> builder) {
        context().builder = (BiConsumer<? super Extension, ? super BundleDescriptor.Builder>) requireNonNull(builder, "builder is null");
    }

    protected final void exposeFeature() {}

    protected final void onAddPostProcessor(Consumer<? extends ExtensionTree<E>> consumer) {

    }

    /**
     * A callback method that is invoked immediately after a container has been successfully configured. This is typically
     * after {@link Bundle#configure()} has returned.
     * <p>
     * The default implementation of this method does nothing.
     * 
     * @param action
     *            the action to perform
     */
    // If the container contains multiple extensions. They are invoked in reverse order. If E2 has a dependency on E1.
    // E2.onConfigured() will be invoked before E1.onConfigure(). This is done in order to allow extensions to perform
    // additional configuration on other extension after user code has been executed
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void onConfigured(Consumer<? super E> action) {
        requireNonNull(action, "action is null");
        Consumer<? super E> a = context().onConfiguredAction;
        context().onConfiguredAction = a == null ? (Consumer) action : a.andThen((Consumer) action);
    }

    /**
     * Registers a (callback) action that is invoked, by the runtime, immediately after an extension has been instantiated,
     * but before the extension is returned to the user. This is typically as the result of a user calling
     * {@link ContainerConfiguration#use(Class)}.
     * <p>
     * If this method is invoked more than once, each action will be performed in the order (FIFO) they where registered.
     * 
     * @param action
     *            The action to be performed after the extension has been instantiated
     */
    @SuppressWarnings("unchecked")
    protected final void onExtensionInstantiated(Consumer<? super E> action) {
        context().onExtensionInstantiated((Consumer<? super Extension>) action);
    }

    // /**
    // * Invoked whenever the container is being instantiated. In case of a container image this means that method might be
    // * invoked multiple times. Even by ___multiple threads___
    // *
    // * @param action
    // * an instantiation context object
    // */
    // // Maa koeres efter trae ting??? Eller ogsaa skal det foregaa paa trae tingen...
    // // Nope det skal ikke foregaa paa trae tingen. Fordi den skal kun bruges hvis man ikke
    // // har extension communication
    // @SuppressWarnings({ "unchecked", "rawtypes" })
    // protected final void onInstantiation(BiConsumer<? super E, ? super ExtensionInstantiationContext> action) {
    // requireNonNull(action, "action is null");
    // BiConsumer<? super E, ? super ExtensionInstantiationContext> a = context().onInstantiation;
    // context().onInstantiation = a == null ? (BiConsumer) action : a.andThen((BiConsumer) action);
    // }

    /**
     * Registers a (callback) action that is invoked, by the runtime, whenever this extension type has been registered in
     * both a parent and child container.
     * <p>
     * {@link #onExtensionInstantiated(Consumer)} is always invoked for the extension before this method.
     * <p>
     * If this method is invoked more than once, each action will be performed in the order (FIFO) they where registered.
     * 
     * @param action
     *            the action to perform
     */
    @SuppressWarnings("unchecked")
    protected final void onLinkage(BiConsumer<? super E, ? super E> action) {
        context().onLinkage((BiConsumer<? super Extension, ? super Extension>) action);
    }
}
