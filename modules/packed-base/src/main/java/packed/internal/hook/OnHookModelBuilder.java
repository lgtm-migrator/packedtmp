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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.reflect.ClassFinder;
import packed.internal.reflect.ClassProcessor;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.util.Tiny;
import packed.internal.util.TinyPair;
import packed.internal.util.UncheckedThrowableFactory;
import packed.internal.util.types.TypeUtil;

/** A builder for classes that may contain methods annotated with {@link OnHook}. */
final class OnHookModelBuilder {

    private final static UncheckedThrowableFactory<? extends RuntimeException> tf = UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY;

    final MutableOnHookMap<TinyPair<Node, MethodHandle>> allEntries = new MutableOnHookMap<>();

    /** All non-root nodes, index by their the type of the hook. */
    private final IdentityHashMap<Class<? extends Hook>, Node> nodes = new IdentityHashMap<>();

    /** The root node. */
    private final Node root;

    /** A stack that is used to process each node. */
    final ArrayDeque<Node> stack = new ArrayDeque<>();

    OnHookModelBuilder(ClassProcessor cp, Class<?>... additionalParameters) {
        if (Hook.class.isAssignableFrom(cp.clazz())) {
            this.root = new Node(cp, cp.clazz());
        } else {
            this.root = new Node(cp);
        }
    }

    @Nullable
    OnHookModel build() {
        // Find all methods annotated with @OnHook and process them.
        root.cp.findMethods(m -> onMethod(root, m));
        for (Node b = stack.pollFirst(); b != null; b = stack.pollFirst()) {
            Node bb = b;
            bb.cp.findMethods(m -> onMethod(bb, m));
        }

        // Roots are only required to have OnHook if they are an Hook themself.
        // For example, Extension and Bundle should not fail here.
        if (allEntries.isEmpty() && Hook.Builder.class.isAssignableFrom(root.cp.clazz())) {
            throw new AssertionError("There must be at least one method annotated with @OnHook on " + root.cp.clazz());
        }

        // Uses a simple iterative algorithm, to make sure there are no interdependencies between the custom hooks
        // It is potentially O(n^2) but this should not be a problem in practice
        // We add each no with no dependencies to the end of the stack.
        int index = nodes.size();
        boolean doContinue = true;
        while (doContinue && !nodes.isEmpty()) {
            doContinue = false;
            for (Iterator<Node> iterator = nodes.values().iterator(); iterator.hasNext();) {
                Node b = iterator.next();
                if (!Tiny.anyMatch(b.dependencies, e -> e.index == 0)) {
                    b.index = index--;
                    stack.addFirst(b);
                    iterator.remove();
                    doContinue = true;
                }
            }
        }
        stack.addFirst(root);

        if (!nodes.isEmpty()) {
            // Okay, we got some circles.
            throw new UnsupportedOperationException("Not supported currently");
        }
        if (allEntries.isEmpty()) {
            return null;
        }
        return new OnHookModel(this);
    }

    private void onMethod(Node node, Method method) {
        if (!method.isAnnotationPresent(OnHook.class)) {
            return;
        }
        if (method.getParameterCount() == 0) {
            throw tf.newThrowableForMethod(
                    "Methods annotated with @" + OnHook.class.getSimpleName() + " must take at least 1 parameter of type " + Hook.class.getCanonicalName(),
                    method);
        }
        Parameter[] parameters = method.getParameters();
        Parameter hook = parameters[0];
        if (!Hook.class.isAssignableFrom(hook.getType())) {
            throw tf.newThrowableForMethod("The first parameter of a method annotated with @" + OnHook.class.getSimpleName() + " must be of type "
                    + Hook.class.getCanonicalName() + " was " + parameters[0].getType(), method);
        }
        @SuppressWarnings("unchecked")
        Class<? extends Hook> hookType = (Class<? extends Hook>) hook.getType();

        for (int i = 1; i < parameters.length; i++) {
            if (node != root) {
                throw tf.newThrowableForMethod(
                        "Implementations of Hook.Builder can only take a single parameter for methods annotated with @" + OnHook.class.getSimpleName(), method);
            }
            if (Hook.class.isAssignableFrom(parameters[i].getType())) {
                throw tf.newThrowableForMethod("Cannot have more than 1 parameter that are instances of " + Hook.class.getCanonicalName(), method);
            }
            //
            // // If we have additional parameters on our initial builder, check that they are okay.
            // if (b == root && parameters.length > 1) {
            // // Check that the remaining are okay
            // // Probably want these additional parameters in a list to Entry
            // }
        }

        MethodHandle mh = node.cp.unreflect(method, tf);

        // Let first see if it is a base book.
        IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> mm = null;
        if (hookType == AnnotatedFieldHook.class) {
            mm = allEntries.annotatedFieldsLazyInit();
        } else if (hookType == AnnotatedMethodHook.class) {
            mm = allEntries.annotatedMethodsLazyInit();
        } else if (hookType == AnnotatedTypeHook.class) {
            mm = allEntries.annotatedTypesLazyInit();
        } else if (hookType == AssignableToHook.class) {
            mm = allEntries.assignableTosLazyInit();
        }

        if (mm != null) {
            ParameterizedType pt = (ParameterizedType) hook.getParameterizedType();
            Class<?> typeVariable = (Class<?>) pt.getActualTypeArguments()[0];
            mm.compute(typeVariable, (k, v) -> new TinyPair<>(node, mh, v));
        } else {
            if (hookType == node.cp.clazz()) {
                tf.newThrowableForMethod("Hook cannot depend on itself", method);
            }
            TypeUtil.checkClassIsInstantiable(hookType);
            IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> m = allEntries.customHooksLazyInit();
            m.compute(hookType, (k, v) -> {

                // Lazy create new node if one does not already exist for the hookType
                Node nodeRef = nodes.computeIfAbsent(hookType, ignore -> {
                    Node newNode = new Node(root.cp, hookType);
                    stack.addLast(newNode); // make sure it will be processed at some later point.
                    return newNode;
                });

                // Test if the builder of a hooks depends on the hook itself
                if (node == nodeRef) {
                    throw tf.newThrowableForMethod("Hook cannot depend on itself", method);
                }

                // Or maybe we need to this for circles??
                // If we have pure tests
                if (node != root) {
                    node.dependencies = new Tiny<>(node, node.dependencies);
                }
                return new TinyPair<>(node, mh, v);
            });
        }
    }

    static final class Node {

        /** A constructor for the builder if this node is a custom hook. */
        @Nullable
        final MethodHandle builderConstructor;

        /** The type of on node container. */
        final Class<?> containerType;

        /** The class processor for the entity that contains the methods annotated with {@link OnHook}. */
        private final ClassProcessor cp;

        /** Any dependencies on other nodes. */
        @Nullable
        private Tiny<Node> dependencies;

        /** The index of this node. */
        int index;

        /**
         * A node without a builder
         * 
         * @param cp
         *            the class processor for the node
         */
        private Node(ClassProcessor cp) {
            this.cp = cp;
            this.containerType = cp.clazz();
            this.builderConstructor = null;
        }

        private Node(ClassProcessor cps, Class<?> type) {
            this.containerType = requireNonNull(type);
            Class<?> builderClass = ClassFinder.findDeclaredClass(type, "Builder", Hook.Builder.class);
            this.cp = cps.spawn(builderClass);

            this.builderConstructor = ConstructorFinder.find(cp, tf);
            if (builderConstructor.type().returnType() != cp.clazz()) {
                throw new IllegalStateException("OOPS");
            }
        }

        @Override
        public String toString() {
            return builderConstructor == null ? "" : builderConstructor.type().toString();
        }
    }

    static final class MutableOnHookMap<V> {

        /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
        IdentityHashMap<Class<?>, V> annotatedFields;

        /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
        IdentityHashMap<Class<?>, V> annotatedMethods;

        /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
        IdentityHashMap<Class<?>, V> annotatedTypes;

        /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
        IdentityHashMap<Class<?>, V> assignableTos;

        /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
        IdentityHashMap<Class<?>, V> customHooks;

        IdentityHashMap<Class<?>, V> annotatedFieldsLazyInit() {
            IdentityHashMap<Class<?>, V> a = annotatedFields;
            if (a == null) {
                a = annotatedFields = new IdentityHashMap<>(1);
            }
            return a;
        }

        boolean isEmpty() {
            return annotatedFields == null && annotatedMethods == null && annotatedTypes == null && assignableTos == null && customHooks == null;
        }

        IdentityHashMap<Class<?>, V> annotatedMethodsLazyInit() {
            IdentityHashMap<Class<?>, V> a = annotatedMethods;
            if (a == null) {
                a = annotatedMethods = new IdentityHashMap<>(1);
            }
            return a;
        }

        IdentityHashMap<Class<?>, V> annotatedTypesLazyInit() {
            IdentityHashMap<Class<?>, V> a = annotatedTypes;
            if (a == null) {
                a = annotatedTypes = new IdentityHashMap<>(1);
            }
            return a;
        }

        IdentityHashMap<Class<?>, V> assignableTosLazyInit() {
            IdentityHashMap<Class<?>, V> a = assignableTos;
            if (a == null) {
                a = assignableTos = new IdentityHashMap<>(1);
            }
            return a;
        }

        IdentityHashMap<Class<?>, V> customHooksLazyInit() {
            IdentityHashMap<Class<?>, V> a = customHooks;
            if (a == null) {
                a = customHooks = new IdentityHashMap<>(1);
            }
            return a;
        }

        <E> ImmutableOnHookMap<E> toImmutable(Function<V, E> converter) {
            if (isEmpty()) {
                return null;
            }
            Map<Class<?>, E> annotatedFieldHooks = convert(annotatedFields, converter);
            Map<Class<?>, E> annotatedMethoddHooks = convert(annotatedMethods, converter);
            Map<Class<?>, E> annotatedTypeHooks = convert(annotatedTypes, converter);
            Map<Class<?>, E> assignableToHooks = convert(assignableTos, converter);
            return new ImmutableOnHookMap<E>(annotatedFieldHooks, annotatedMethoddHooks, annotatedTypeHooks, assignableToHooks);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private <VV, E> Map<Class<?>, E> convert(IdentityHashMap<Class<?>, VV> map, Function<VV, E> f) {
            if (map == null) {
                return null;
            }
            // Replace in map
            IdentityHashMap m = map;

            m.replaceAll((k, v) -> ((Function) f).apply(v));

            return Map.copyOf(m);
        }
    }
}
