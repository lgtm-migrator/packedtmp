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
package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.hook.OnHookAggregateBuilder;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.model.ComponentLookup;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
public final class ExtensionHookPerComponentGroup {

    /** A list of callbacks for the particular extension. */
    private final List<Callback> callbacks;

    /** The type of extension that will be activated. */
    private final Class<? extends Extension> extensionType;

    public final List<MethodConsumer<?>> methodConsumers;

    private ExtensionHookPerComponentGroup(Builder b) {
        this.extensionType = requireNonNull(b.extensionType);
        this.methodConsumers = List.copyOf(b.consumers);
        this.callbacks = b.callbacks;
    }

    public int getNumberOfCallbacks() {
        return callbacks.size();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void add(PackedContainerConfiguration container, ComponentConfiguration cc) {
        Extension extension = container.use((Class) extensionType);

        try {
            for (Callback c : callbacks) {
                c.mh.invoke(extension, cc, c.o);
            }
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class Builder {

        /** The component type */
        final Class<?> componentType;

        private ArrayList<MethodConsumer<?>> consumers = new ArrayList<>();

        final ArrayList<Callback> callbacks = new ArrayList<>();

        final IdentityHashMap<Class<?>, OnHookAggregateBuilder<?>> mmm = new IdentityHashMap<>();
        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        private final ComponentLookup lookup;

        final ExtensionOnHookDescriptor con;

        public Builder(Class<?> componentType, Class<? extends Extension> extensionType, ComponentLookup lookup) {
            this.componentType = requireNonNull(componentType);
            this.con = ExtensionOnHookDescriptor.get(extensionType);
            this.extensionType = requireNonNull(extensionType);
            this.lookup = requireNonNull(lookup);
        }

        public ExtensionHookPerComponentGroup build() {
            // Add all aggregates
            for (Entry<Class<?>, OnHookAggregateBuilder<?>> m : mmm.entrySet()) {
                MethodHandle mh = con.aggregators.get(m.getKey());
                callbacks.add(new Callback(mh, m.getValue().build()));
            }
            return new ExtensionHookPerComponentGroup(this);
        }

        public void onAnnotatedField(Field field, Annotation annotation) {
            PackedAnnotatedFieldHook hook = new PackedAnnotatedFieldHook(lookup, field, annotation);
            process(con.findMethodHandleForAnnotatedField(hook), hook);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) {
            PackedAnnotatedMethodHook hook = new PackedAnnotatedMethodHook(lookup, method, annotation, consumers);
            process(con.findMethodHandleForAnnotatedMethod(hook), hook);
        }

        private void process(MethodHandle mh, Object hook) {
            Class<?> owner = mh.type().parameterType(0);
            if (owner == extensionType) {
                callbacks.add(new Callback(mh, hook));
            } else {
                // The method handle refers to an aggregator object.
                OnHookAggregatorDescriptor a = OnHookAggregatorDescriptor.get((Class<? extends OnHookAggregateBuilder<?>>) owner);
                OnHookAggregateBuilder<?> sup = mmm.computeIfAbsent(owner, k -> a.newAggregatorInstance());
                requireNonNull(sup);
                requireNonNull(hook);
                try {
                    mh.invoke(sup, hook);
                } catch (Throwable e) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class MethodConsumer<S> {
        final BiConsumer<S, Runnable> consumer;
        final Class<S> key;
        final MethodHandle mh;

        /**
         * @param key
         * @param consumer
         */
        public MethodConsumer(Class<S> key, BiConsumer<S, Runnable> consumer, MethodHandle mh) {
            this.key = requireNonNull(key);
            this.consumer = requireNonNull(consumer, "consumer is null");
            this.mh = requireNonNull(mh);

        }

        public void prepare(ContainerConfiguration cc, ArtifactInstantiationContext ic) {
            S s = ic.use(cc, key);
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    try {
                        mh.invoke();
                    } catch (Throwable e) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e);
                        throw new RuntimeException(e);
                    }
                }
            };
            consumer.accept(s, r);
        }
    }

    /** A dummy type indicating that no aggregator should be used. */
    public static abstract class NoAggregator implements Supplier<Void>, OnHookAggregateBuilder<Void> {}
}
