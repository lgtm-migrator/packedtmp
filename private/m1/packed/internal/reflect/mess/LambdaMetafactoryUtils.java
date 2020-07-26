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
package packed.internal.reflect.mess;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *
 */
public final class LambdaMetafactoryUtils {
    static Field allowedModesField;

    static final int ALL_MODES = (MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC);

    private static final Lookup LOOKUP = MethodHandles.lookup();

    // private static void initGetterSetterMap() {
    // GETTER_MAP.put(boolean.class, BoolGetter.class);
    // GETTER_MAP.put(byte.class, ByteGetter.class);
    // GETTER_MAP.put(short.class, ShortGetter.class);
    // GETTER_MAP.put(int.class, IntGetter.class);
    // GETTER_MAP.put(long.class, LongGetter.class);
    // GETTER_MAP.put(float.class, FloatGetter.class);
    // GETTER_MAP.put(double.class, DoubleGetter.class);
    //
    // SETTER_MAP.put(boolean.class, BoolSetter.class);
    // SETTER_MAP.put(byte.class, ByteSetter.class);
    // SETTER_MAP.put(short.class, ShortSetter.class);
    // SETTER_MAP.put(int.class, IntSetter.class);
    // SETTER_MAP.put(long.class, LongSetter.class);
    // SETTER_MAP.put(float.class, FloatSetter.class);
    // SETTER_MAP.put(double.class, DoubleSetter.class);
    // }

    private LambdaMetafactoryUtils() {}

    protected static Method findAbstractMethod(Class<?> functionalInterface) {
        for (Method method : functionalInterface.getMethods()) {
            if ((method.getModifiers() & Modifier.ABSTRACT) != 0) {
                return method;
            }
        }

        return null;
    }

    public static <T> T createLambda(Object instance, Method instanceMethod, Class<?> functionalIntfCls) {
        try {
            Lookup lookup = LOOKUP.in(instanceMethod.getDeclaringClass());

            Method intfMethod = findAbstractMethod(functionalIntfCls);
            MethodHandle methodHandle = lookup.unreflect(instanceMethod);

            MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
            MethodType instanceMethodType = MethodType.methodType(instanceMethod.getReturnType(), instanceMethod.getParameterTypes());
            CallSite callSite = LambdaMetafactory.metafactory(lookup, intfMethod.getName(), MethodType.methodType(functionalIntfCls, instance.getClass()),
                    intfMethodType, methodHandle, instanceMethodType);

            return (T) callSite.getTarget().bindTo(instance).invoke();
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
        }
    }

    public static <T> CallSite createCallsite(Object instance, Method instanceMethod, Class<?> functionalIntfCls) {
        try {
            Lookup lookup = LOOKUP.in(instanceMethod.getDeclaringClass());

            Method intfMethod = findAbstractMethod(functionalIntfCls);
            MethodHandle methodHandle = lookup.unreflect(instanceMethod);

            MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
            MethodType instanceMethodType = MethodType.methodType(instanceMethod.getReturnType(), instanceMethod.getParameterTypes());
            CallSite callSite = LambdaMetafactory.metafactory(lookup, intfMethod.getName(), MethodType.methodType(functionalIntfCls, instance.getClass()),
                    intfMethodType, methodHandle, instanceMethodType);

            return callSite;
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
        }
    }

    public static <T> T createLambda(Method instanceMethod, Class<?> functionalIntfCls) {
        try {
            Lookup lookup = LOOKUP.in(instanceMethod.getDeclaringClass());

            Method intfMethod = findAbstractMethod(functionalIntfCls);
            MethodHandle methodHandle = lookup.unreflect(instanceMethod);

            MethodType intfMethodType = MethodType.methodType(intfMethod.getReturnType(), intfMethod.getParameterTypes());
            MethodType instanceMethodType = methodHandle.type();
            CallSite callSite = LambdaMetafactory.metafactory(lookup, intfMethod.getName(), MethodType.methodType(functionalIntfCls), intfMethodType,
                    methodHandle, instanceMethodType);

            return (T) callSite.getTarget().invoke();
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to create lambda from " + instanceMethod, e);
        }
    }

    public static void main(String[] args) throws Throwable {
        System.out.println("Bye");
        Method m = LambdaMetafactoryUtils.class.getDeclaredMethod("fooo");
        // Consumer<String> r = createLambda(new LambdaMetafactoryUtils(), m, Consumer.class);

        // r.accept("Doo");

        CallSite callsite = createCallsite(new LambdaMetafactoryUtils(), m, Runnable.class);

        ((Runnable) callsite.getTarget().bindTo(new LambdaMetafactoryUtils()).invoke()).run();
        ((Runnable) callsite.getTarget().bindTo(new LambdaMetafactoryUtils()).invoke()).run();

    }

    public void fooo() {
        System.out.println("OK " + hashCode());
    }

    public void foo(String s) {
        System.out.println("OK " + s + hashCode());
    }

    interface X {
        void accept(String s);
    }
    // public static <T> T createGetter(Method getMethod) {
    // Class<?> getterCls = GETTER_MAP.getOrDefault(getMethod.getReturnType(), Getter.class);
    // return createLambda(getMethod, getterCls);
    // }
    //
    // // slower than reflect directly
    // @SuppressWarnings("unchecked")
    // public static <C, F> Getter<C, F> createGetter(Field field) {
    // field.setAccessible(true);
    // return instance -> {
    // try {
    // return (F) field.get(instance);
    // } catch (IllegalAccessException e) {
    // throw new RuntimeException(e);
    // }
    // };
    // }
    //
    // public static <T> T createSetter(Method setMethod) throws Throwable {
    // Class<?> setterCls = SETTER_MAP.getOrDefault(setMethod.getParameterTypes()[0], Setter.class);
    // return createLambda(setMethod, setterCls);
    // }
    //
    // // slower than reflect directly
    // public static <C, F> Setter<C, F> createSetter(Field field) {
    // field.setAccessible(true);
    // return (instance, value) -> {
    // try {
    // field.set(instance, value);
    // } catch (IllegalAccessException e) {
    // throw new RuntimeException(e);
    // }
    // };
    // }
}