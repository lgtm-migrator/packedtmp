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
import java.util.Map;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import packed.internal.hook.HookRequest.BaseHookCallback;
import packed.internal.hook.OnHookModel.Link;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.Tiny;
import packed.internal.util.TinyPair;

/**
 *
 */
public class HookRequestBuilder {

    private final Object[] array;

    Tiny<BaseHookCallback> baseHooksCallback;

    final UnreflectGate hookProcessor;

    /** Whether or not we are called from {@link Hook.Builder#test(java.lang.invoke.MethodHandles.Lookup, Class, Class)} */
    @SuppressWarnings("javadoc") // eclipse...TODO raise bug
    private final Mode mode;

    private final OnHookModel onHookModel;

    public HookRequestBuilder(OnHookModel model, UnreflectGate hookProcessor) {
        this(model, hookProcessor, Mode.NORMAL);
    }

    public HookRequestBuilder(OnHookModel model, UnreflectGate hookProcessor, Mode mode) {
        this.array = new Object[model.builderConstructors.length];
        this.onHookModel = requireNonNull(model);
        this.hookProcessor = requireNonNull(hookProcessor);
        this.mode = mode;
    }

    public HookRequest build() throws Throwable {
        return new HookRequest(this);
    }

    private Hook.Builder<?> builderOf(Object[] array, int index) throws Throwable {
        Object builder = array[index];
        if (builder == null) {
            builder = array[index] = onHookModel.builderConstructors[index].invoke();
        }
        return (Hook.Builder<?>) builder;
    }

    TinyPair<Hook, MethodHandle> compute() throws Throwable {
        // This code is same as process()
        for (int i = array.length - 1; i >= 0; i--) {
            for (Link link = onHookModel.customHooks[i]; link != null; link = link.next) {
                if (onHookModel.builderConstructors[i] != null) {
                    Hook.Builder<?> builder = builderOf(array, i);
                    link.mh.invoke(builder, array[link.index]);
                }
            }
            if (i > 0) {
                Object h = array[i];
                if (h != null) {
                    array[i] = ((Hook.Builder<?>) h).build();
                }
            }
        }

        TinyPair<Hook, MethodHandle> result = null;
        for (Link link = onHookModel.customHooks[0]; link != null; link = link.next) {
            result = new TinyPair<>((Hook) array[link.index], link.mh, result);
        }
        return result;
    }

    public void onAssignableTo(Class<?> hookType, Class<?> actualType) throws Throwable {
        Map<Class<?>, Link> assignableTos = onHookModel.allLinks.assignableTos;
        if (assignableTos != null) {
            for (Link link = assignableTos.get(hookType); link != null; link = link.next) {
                if (link.index == 0 && mode != Mode.TEST_CLASS) {
                    baseHooksCallback = new Tiny<>(new BaseHookCallback(actualType, null, link.mh), baseHooksCallback);
                } else {
                    Hook.Builder<?> builder = builderOf(array, link.index);
                    AssignableToHook<?> hook = ModuleAccess.hook().newAssignableToHook(hookProcessor, actualType);
                    if (link.mh.type().parameterCount() == 1) {
                        link.mh.invoke(hook);
                    } else {
                        link.mh.invoke(builder, hook);
                    }
                }
            }
        }
    }

    void foo(HookBaseType hbt, Class<?> qualifierType, Annotation annotation, Object memberOrClass) {

    }

    public void onAnnotatedField(Field field, Annotation annotation) throws Throwable {
        Map<Class<?>, Link> annotatedFields = onHookModel.allLinks.annotatedFields;
        if (annotatedFields != null) {
            for (Link link = annotatedFields.get(annotation.annotationType()); link != null; link = link.next) {
                if (link.index == 0 && mode != Mode.TEST_CLASS) {
                    baseHooksCallback = new Tiny<>(new BaseHookCallback(field, annotation, link.mh), baseHooksCallback);
                } else {
                    Hook.Builder<?> builder = builderOf(array, link.index);
                    AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hookProcessor, field, annotation);
                    if (link.mh.type().parameterCount() == 1) {
                        link.mh.invoke(hook);
                    } else {
                        link.mh.invoke(builder, hook);
                    }
                }
            }
        }
    }

    public void onAnnotatedMethod(Method method, Annotation annotation) throws Throwable {
        Map<Class<?>, Link> annotatedMethods = onHookModel.allLinks.annotatedMethods;
        if (annotatedMethods != null) {
            for (Link link = annotatedMethods.get(annotation.annotationType()); link != null; link = link.next) {
                if (link.index == 0 && mode != Mode.TEST_CLASS) {
                    baseHooksCallback = new Tiny<>(new BaseHookCallback(method, annotation, link.mh), baseHooksCallback);
                } else {
                    Hook.Builder<?> builder = builderOf(array, link.index);
                    AnnotatedMethodHook<Annotation> hook = ModuleAccess.hook().newAnnotatedMethodHook(hookProcessor, method, annotation);
                    link.mh.invoke(builder, hook);
                }
            }
        }
    }

    public void onAnnotatedType(Class<?> clazz, Annotation annotation) throws Throwable {
        Map<Class<?>, Link> annotatedTypes = onHookModel.allLinks.annotatedTypes;
        if (annotatedTypes != null) {
            for (Link link = annotatedTypes.get(annotation.annotationType()); link != null; link = link.next) {
                if (link.index == 0 && mode != Mode.TEST_CLASS) {
                    baseHooksCallback = new Tiny<>(new BaseHookCallback(clazz, annotation, link.mh), baseHooksCallback);
                } else {
                    Hook.Builder<?> builder = builderOf(array, link.index);
                    AnnotatedTypeHook<Annotation> hook = ModuleAccess.hook().newAnnotatedTypeHook(hookProcessor, clazz, annotation);
                    link.mh.invoke(builder, hook);
                }
            }
        }
    }

    public Object singleConsume(ClassProcessor cp) throws Throwable {
        //
        cp.findMethodsAndFields(onHookModel.allLinks.annotatedMethods == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                onAnnotatedMethod(f, a);
            }
        }, onHookModel.allLinks.annotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                onAnnotatedField(f, a);
            }
        });
        compute();
        Object a = array[0];
        return a == null ? null : (((Hook.Builder<?>) a).build());
    }

    public void singleConsumeNoInstantiate(ClassProcessor cp, Object instance) throws Throwable {
        array[0] = instance;
        cp.findMethodsAndFields(onHookModel.allLinks.annotatedMethods == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                onAnnotatedMethod(f, a);
            }
        }, onHookModel.allLinks.annotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                onAnnotatedField(f, a);
            }
        });
        compute();
        new HookRequest(this).invoke(instance, null);
    }

    public enum Mode {
        NORMAL, TEST_CLASS, TEST_INSTANCE;
    }
}
