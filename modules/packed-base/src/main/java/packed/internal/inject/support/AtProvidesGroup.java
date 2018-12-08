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
package packed.internal.inject.support;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import packed.internal.invokers.ExecutableInvoker;
import packed.internal.invokers.FieldInvoker;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * Information about fields and methods annotated with {@link Provides}. Used for both services, components, import and
 * export stages.
 */

// TODO: merge fields + methods?
public final class AtProvidesGroup {

    /** An empty group. */
    private static final AtProvidesGroup EMPTY = new AtProvidesGroup(new Builder());

    /** All fields annotated with {@link Provides}. */
    public final List<AtProvides> fields;

    /** Whether or not there are any non-static fields or methods. */
    public final boolean hasInstanceMembers;

    /** A set of all keys provided. */
    public final Map<Key<?>, AtProvides> keys;

    /** All methods annotated with {@link Provides}. */
    public final List<AtProvides> methods;

    private AtProvidesGroup(Builder builder) {
        this.methods = builder.methods == null ? List.of() : List.copyOf(builder.methods);
        this.fields = builder.fields == null ? List.of() : List.copyOf(builder.fields);
        this.keys = builder.keys == null ? Map.of() : Map.copyOf(builder.keys);
        this.hasInstanceMembers = builder.hasInstanceMembers;
    }

    /**
     * Returns whether or not the group is empty.
     * 
     * @return whether or not the group is empty
     */
    public boolean isEmpty() {
        return keys.size() == 0;
    }

    /** A builder for an {@link AtProvidesGroup}. */
    public static class Builder {

        /** All fields annotated with {@link Provides}. */
        private ArrayList<AtProvides> fields;

        /** Whether or not there are any non-static fields or methods. */
        private boolean hasInstanceMembers;

        /** A set of all keys provided. */
        private HashMap<Key<?>, AtProvides> keys;

        /** All methods annotated with {@link Provides}. */
        private ArrayList<AtProvides> methods;

        public AtProvides addIfAnnotated(Lookup lookup, Field field, Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a.annotationType() == Provides.class) {
                    Key<?> key = Key.fromField(field);

                    InternalFieldDescriptor descriptor = InternalFieldDescriptor.of(field);
                    hasInstanceMembers |= !descriptor.isStatic();

                    FieldInvoker<?> fii = new FieldInvoker<>(descriptor).withLookup(lookup);
                    AtProvides ap = new AtProvides(fii, descriptor, key, (Provides) a, List.of());

                    // Check this
                    // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
                    // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
                    // }

                    ///////// Add the field
                    if (fields == null) {
                        fields = new ArrayList<>(1);
                        if (keys == null) {
                            keys = new HashMap<>();
                        }
                    }
                    if (keys.putIfAbsent(key, ap) != null) {
                        throw new InvalidDeclarationException(ErrorMessageBuilder.of(field.getDeclaringClass())
                                .cannot("have multiple members providing services with the same key (" + key.toStringSimple() + ").")
                                .toResolve("either remove @Provides on one of the members, or use a unique qualifier for each of the members"));
                    }
                    fields.add(ap);
                    return ap;
                }
            }
            return null;
        }

        public AtProvides addIfAnnotated(Lookup lookup, Method method, Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a.annotationType() == Provides.class) {
                    Key<?> key = Key.fromMethodReturnType(method);

                    InternalMethodDescriptor descriptor = InternalMethodDescriptor.of(method);
                    hasInstanceMembers |= !descriptor.isStatic();

                    ExecutableInvoker<?> fii = new ExecutableInvoker<Object>(descriptor).withLookup(lookup);

                    AtProvides ap = new AtProvides(fii, descriptor, key, (Provides) a, List.of());

                    if (methods == null) {
                        methods = new ArrayList<>(1);
                        if (keys == null) {
                            keys = new HashMap<>();
                        }
                    }
                    if (keys.putIfAbsent(key, ap) != null) {
                        throw new InvalidDeclarationException(ErrorMessageBuilder.of(method.getDeclaringClass())
                                .cannot("have multiple members providing services with the same key (" + key.toStringSimple() + ").")
                                .toResolve("either remove @Provides on one of the members, or use a unique qualifier for each of the members"));
                    }
                    methods.add(ap);
                    return ap;
                }
            }
            return null;
        }

        public AtProvidesGroup build() {
            if (fields == null && methods == null) {
                return EMPTY;
            }
            // TODO check that we do not have multiple fields/methods with the same key....
            return new AtProvidesGroup(this);
        }
    }
}
