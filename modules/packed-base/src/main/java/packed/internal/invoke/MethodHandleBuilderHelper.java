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
package packed.internal.invoke;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.InjectionContext;
import app.packed.inject.Service;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.util.MethodHandleUtil;

/**
 *
 */
//Allow multiple constructors, For example take a list of MethodType...
//Custom extensionClasss

//Maybe a little customization of error messages...
//Maybe just a protected method that creates the message that can then be overridden.

//We want to create a ConstructorFinder instance that we reuse..
//So lookup object is probably an optional argument
//The rest is static, its not for injection, because we need
class MethodHandleBuilderHelper {

    final MethodType input;

    final MethodHandle executable;

    final List<Parameter> parameters;
    final int add;
    final int[] permutationArray;
    final MethodHandleBuilder aa;

    final Class<?> declaringClass;

    MethodHandleBuilderHelper(ClassMemberAccessor oc, Executable e, MethodHandleBuilder aa) {
        this.aa = aa;
        input = aa.targetType();

        boolean isInstanceMethod = false;

        // Setup MethodHandle for constructor or method
        if (e instanceof Constructor<?> con) {
            executable = oc.unreflectConstructor(con, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        } else {
            Method m = (Method) e;
            executable = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            isInstanceMethod = !Modifier.isStatic(m.getModifiers());

            // If Instance method callsite type must always have the receiver at index 0
//            if (isInstanceMethod) {
//                if (m.getDeclaringClass() != aa.targetType().parameterType(0)) {
//                    throw new IllegalArgumentException(
//                            "First signature parameter type must be " + m.getDeclaringClass() + " was " + aa.targetType().parameterType(0));
//                }
//            }
        }
        this.declaringClass = e.getDeclaringClass();
        parameters = List.of(e.getParameters());

        add = isInstanceMethod ? 1 : 0;
        permutationArray = new int[parameters.size() + add];
        if (isInstanceMethod) {
            permutationArray[0] = 0;
        }
    }

    enum Transformer {
        NONE, OPTIONAL, PROVIDER, COMPOSITE;
    }

    MethodHandle find() {
        MethodHandle mh = executable;
        IntStack is = new IntStack();

        // vi pusher den rigtig vaerdi vi skal bruge for alle af dem
        // dvs is.push(index)
        // Er der andre en InjectionContext der laver nyt?
        // Taenker jeg ikke

        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            DependencyDescriptor sd = DependencyDescriptor.fromVariable(p, i);
            Class<?> askingForType = sd.key().rawType();
            MethodHandleBuilder.AnnoClassEntry anno = find(aa, p);

            if (anno == null) {
                Key<?> kk = sd.key();

                // Injection Context
                if (kk.equalsTo(InjectionContext.class)) {
                    // TODO we have a non-constant injection context, when we have a dynamic injector
                    // Vi just add it as a normal entry with no indexes, will be picked up in the next section
                    HashSet<Key<?>> keys = new HashSet<>();
                    Map<Key<?>, Service> services = new HashMap<>();
                    for (Entry<Key<?>, Infuser.Entry> e : aa.keys.entrySet()) {
                        if (!e.getValue().isHidden()) {
                            keys.add(e.getKey());
                            services.put(e.getKey(), ServiceSetup.simple(e.getKey(), false));
                        }
                    }

                    PackedInjectionContext pic = new PackedInjectionContext(declaringClass, Map.copyOf(services));
                    aa.keys.putIfAbsent(kk, new Infuser.Entry(MethodHandles.constant(InjectionContext.class, pic), false, false));
                }

                Infuser.Entry entry = aa.keys.get(kk);
                if (entry != null) {
                    // Vi have an explicit registered service.

                    if (entry.transformer() != null) {
                        MethodHandle transformer = entry.transformer();
                        if (sd.isOptional()) {
                            // We need to the return value of transformer to an optional
                            transformer = MethodHandles.filterReturnValue(transformer, MethodHandleBuilderConstants.optionalOfTo(askingForType));
                        }
                        mh = MethodHandles.collectArguments(mh, is.size() + add, transformer);
                    } else {
                        // We use a provided value directly. Wrap it in an Optional if needed

                        Class<?> actual = kk.rawType();
                        Class<?> expected = input.parameterType(entry.indexes()[0]);

                        // Upcast if needed, I don't think we need to do this if we create an optional
                        if (actual != expected) {
                            // if (actual.isAssignableFrom(expected)) {
                            MethodType newType = mh.type().changeParameterType(is.size() + add, expected);
                            mh = mh.asType(newType);
                            // } // else should fail...
                        }
                        if (sd.isOptional()) {
                            mh = MethodHandleUtil.replaceParameter(mh, is.size() + add, MethodHandleBuilderConstants.optionalOfTo(askingForType));
                        }
                    }
                    is.push(entry.indexes());
                } else {
                    // Det er saa her, at vi kalder ind paa en virtuel injector...
                    if (sd.isOptional()) {
                        mh = MethodHandles.insertArguments(mh, is.size() + add, Optional.empty());
                    } else {
                        // Could be inner class
                        if (kk.rawType() == declaringClass.getDeclaringClass() && !kk.hasQualifiers()) {
                            // Better error message
                            throw new IllegalArgumentException("Could not inject inner class " + kk + " Available keys = " + aa.keys.keySet());
                        }
                        throw new IllegalArgumentException("Could not inject " + kk + " Available keys = " + aa.keys.keySet());
                    }
                }
            } else {
                // Annnotation
                // Vi supportere kun lige nu noget der sporger paa typen (class)
                // Som vi binder til en supplied methodhandle som vi derefter kalder

                MethodHandle tmp = MethodHandles.insertArguments(anno.mh, 1, askingForType);
                tmp = MethodHandles.explicitCastArguments(tmp, MethodType.methodType(askingForType, tmp.type().parameterArray()[0]));
                if (sd.isOptional()) {
                    // We need to the return value of transformer to an optional, may be null
                    tmp = MethodHandles.filterReturnValue(tmp, MethodHandleBuilderConstants.optionalOfNullableTo(askingForType));
                }

                mh = MethodHandleUtil.replaceParameter(mh, is.size() + add, tmp);
                is.push(anno.index);
            }
        }

        // Providere.. Der binder man en MethodHandle

        if (add == 1) {
            // We may need to cast the receiver
            if (input.parameterType(0) != mh.type().parameterType(0)) {
                mh = mh.asType(mh.type().changeParameterType(0, input.parameterType(0)));
            }
//            if (input.parameterCount() > 1 && mh.type().parameterCount() > 1) {
//                if (input.parameterType(1) != mh.type().parameterType(1)) {
//                    mh = mh.asType(mh.type().changeParameterType(1, input.parameterType(1)));
//                }
//            }

//              throw new IllegalArgumentException(
//                      "First signature parameter type must be " + m.getDeclaringClass() + " was " + aa.targetType().parameterType(0));
//          }
            mh = MethodHandles.permuteArguments(mh, input, is.toArrayAdd0());
        } else { // static
            // USed for example, for constructors to change the actually type being made
            // F.x Extension instead of ServiceExtension (so we can use invokeExact
            if (input.returnType() != mh.type().returnType()) {
                mh = MethodHandleUtil.castReturnType(mh, input.returnType()); // need to upcast to extension to invokeExact
            }
            mh = MethodHandles.permuteArguments(mh, input, is.toArray());
        }
        return mh;
    }

    private MethodHandleBuilder.@Nullable AnnoClassEntry find(MethodHandleBuilder aa, Parameter p) {
        for (Annotation a : p.getAnnotations()) {
            if (aa.annoations.containsKey(a.annotationType())) {
                return aa.annoations.get(a.annotationType());
            }
        }
        return null;
    }
}
