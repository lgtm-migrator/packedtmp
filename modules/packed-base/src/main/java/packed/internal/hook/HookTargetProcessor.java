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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import packed.internal.reflect.ClassProcessor;
import packed.internal.util.UncheckedThrowableFactory;

/**
 * A single one of these is created by class that is analyzed. Is used to make sure that no instance
 * {@link MethodHandle} and {@link VarHandle}. Is created after the component has analyzed. This is done in order to
 * avoid someone trying to create instances of them, outside of stuff because of grall..
 */
// HookGate
public final class HookTargetProcessor implements AutoCloseable {

    final ClassProcessor cp;

    /** Whether or not the processor is closed. */
    private boolean isClosed;

    private final UncheckedThrowableFactory<? extends RuntimeException> tf;

    @SuppressWarnings("unchecked")
    public HookTargetProcessor(ClassProcessor cp, UncheckedThrowableFactory<?> tf) {
        this.cp = requireNonNull(cp);
        // A hack to allow us to throw AssertionError, as we have no way to indicate
        // Error || RuntimeException
        this.tf = (UncheckedThrowableFactory<? extends RuntimeException>) tf;
    }

    public void checkOpen() {
        if (isClosed) {
            throw new IllegalStateException("The underlying hook processor is no longer open after the hook has been built.");
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }

    public UncheckedThrowableFactory<? extends RuntimeException> tf() {
        return tf;
    }

    public MethodHandle unreflect(Method method) {
        checkOpen();
        return cp.unreflect(method, tf);
    }

    public MethodHandle unreflectConstructor(Constructor<?> constructor) {
        checkOpen();
        return cp.unreflectConstructor(constructor, tf);
    }

    public MethodHandle unreflectGetter(Field field) {
        checkOpen();
        return cp.unreflectGetter(field, tf);
    }

    public MethodHandle unreflectSetter(Field field) {
        checkOpen();
        return cp.unreflectSetter(field, tf);
    }

    public VarHandle unreflectVarhandle(Field field) {
        checkOpen();
        return cp.unreflectVarhandle(field, tf);
    }
}
