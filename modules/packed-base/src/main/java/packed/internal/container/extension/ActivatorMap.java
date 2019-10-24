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
package packed.internal.container.extension;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import app.packed.container.Extension;
import app.packed.container.UseExtension;
import app.packed.container.UseExtensionLazily;
import app.packed.lang.Nullable;
import packed.internal.util.tiny.TinyNode;

/**
 *
 */
public final class ActivatorMap {

    /** A cache of any extensions a particular annotation activates. */
    static final ClassValue<Class<? extends Extension>[]> EXTENSION_ACTIVATORS = new ClassValue<>() {

        @Override
        protected Class<? extends Extension>[] computeValue(Class<?> type) {
            UseExtension ae = type.getAnnotation(UseExtension.class);
            return ae == null ? null : ae.value();
        }
    };

    @Nullable
    private final Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedFields;

    @Nullable
    private final Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedMethods;

    @Nullable
    private final Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedTypes;

    private ActivatorMap(@Nullable Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedFields,
            @Nullable Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedMethods,
            @Nullable Map<Class<? extends Annotation>, Set<Class<? extends Extension>>> annotatedTypes) {
        this.annotatedFields = annotatedFields;
        this.annotatedMethods = annotatedMethods;
        this.annotatedTypes = annotatedTypes;
    }

    @Nullable
    public Set<Class<? extends Extension>> onAnnotatedField(Class<? extends Annotation> annotationType) {
        return annotatedFields == null ? null : annotatedFields.get(annotationType);
    }

    @Nullable
    public Set<Class<? extends Extension>> onAnnotatedMethod(Class<? extends Annotation> annotationType) {
        return annotatedMethods == null ? null : annotatedMethods.get(annotationType);
    }

    @Nullable
    public Set<Class<? extends Extension>> onAnnotatedType(Class<? extends Annotation> annotationType) {
        return annotatedTypes == null ? null : annotatedTypes.get(annotationType);
    }

    @Nullable
    static <T> Set<Class<? extends T>> findNonAutoExtending(Set<Class<? extends T>> set) {
        TinyNode<Class<? extends T>> n = null;
        if (set != null) {
            for (Class<? extends T> c : set) {
                if (!ActivatorMap.isAutoActivate(c)) {
                    n = new TinyNode<>(c, n);
                }
            }
        }
        return TinyNode.toSetOrNull(n);
    }

    public static boolean isAutoActivate(Class<?> clazz) {
        return EXTENSION_ACTIVATORS.get(clazz) != null;
    }

    public static ActivatorMap of(Class<?> cl) {
        UseExtensionLazily uel = cl.getAnnotation(UseExtensionLazily.class);
        if (uel == null) {
            return null;
        }

        HashMap<Class<? extends Annotation>, TinyNode<Class<? extends Extension>>> annotatedFields = new HashMap<>(0);
        HashMap<Class<? extends Annotation>, TinyNode<Class<? extends Extension>>> annotatedMethods = new HashMap<>(0);
        HashMap<Class<? extends Annotation>, TinyNode<Class<? extends Extension>>> annotatedTypes = new HashMap<>(0);

        for (Class<? extends Extension> c : uel.value()) {
            ExtensionModel<? extends Extension> em = ExtensionModel.of(c);
            if (em.nonActivatingHooks != null) {
                stats(c, annotatedFields, em.nonActivatingHooks.annotatedFields);
                stats(c, annotatedMethods, em.nonActivatingHooks.annotatedMethods);
                stats(c, annotatedTypes, em.nonActivatingHooks.annotatedTypes);
            }
        }
        if (annotatedFields.size() == 0 && annotatedMethods.size() == 0 && annotatedTypes.size() == 0) {
            System.err.println("Why use " + uel);
        }
        return new ActivatorMap(TinyNode.toMapOrNull(annotatedFields), TinyNode.toMapOrNull(annotatedMethods), TinyNode.toMapOrNull(annotatedTypes));
    }

    @Nullable
    private static <T> void stats(Class<? extends Extension> extensionType, HashMap<Class<? extends T>, TinyNode<Class<? extends Extension>>> map,
            @Nullable Set<Class<? extends T>> set) {
        if (set != null) {
            for (Class<? extends T> c : set) {
                map.compute(c, (k, v) -> new TinyNode<>(extensionType, v));
            }
        }
    }
}