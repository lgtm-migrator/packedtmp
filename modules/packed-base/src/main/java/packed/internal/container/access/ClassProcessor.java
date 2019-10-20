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
package packed.internal.container.access;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import app.packed.lang.NativeImage;
import app.packed.lang.reflect.UncheckedIllegalAccessException;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableFactory;

/**
 *
 */
// Make sure the constructor is registered if we are generating a native image
// NativeImage.registerConstructor(constructor);
public class ClassProcessor {

    /** The app.packed.base module. */
    private static final Module THIS_MODULE = ClassProcessor.class.getModule();

    private final MethodHandles.Lookup lookup;

    private MethodHandles.Lookup privateLookup;

    private final Class<?> clazz;

    private boolean isInitialized;

    private final boolean registerForNative;

    public ClassProcessor(MethodHandles.Lookup lookup, Class<?> clazz, boolean registerForNative) {
        this.lookup = requireNonNull(lookup);
        this.clazz = requireNonNull(clazz);
        this.registerForNative = registerForNative;
    }

    public <T extends Throwable> void checkPackageOpen(ThrowableFactory<T> tf) throws T {
        String pckName = clazz.getPackageName();
        if (!clazz.getModule().isOpen(pckName, THIS_MODULE)) {
            String otherModule = clazz.getModule().getName();
            String m = THIS_MODULE.getName();
            throw tf.newThrowable("In order to access '" + StringFormatter.format(clazz) + "', the module '" + otherModule + "' must be open to '" + m
                    + "'. This can be done, for example, by adding 'opens " + pckName + " to " + m + ";' to the module-info.java file of " + otherModule);
        }
    }

    private <T extends Throwable> Lookup lookup(Member member, ThrowableFactory<T> tf) throws T {
        if (member.getDeclaringClass() != clazz) {
            throw new IllegalArgumentException();
        }
        if (!isInitialized) {
            checkPackageOpen(tf);
            // Should we use lookup.getdeclaringClass???
            if (!THIS_MODULE.canRead(clazz.getModule())) {
                THIS_MODULE.addReads(clazz.getModule());
            }
        }
        MethodHandles.Lookup p = privateLookup;
        if (p != null) {
            return p;
        }

        if (!needsPrivateLookup(member)) {
            return lookup;
        }
        try {
            return p = MethodHandles.privateLookupIn(clazz, lookup);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create private lookup", e);
        }
    }

    /**
     * @param method
     *            the method to unreflect
     * @return a method handle for the unreflected method
     */
    public <T extends Throwable> MethodHandle unreflect(Method method, ThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(method, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("stuff", e);
        }

        if (registerForNative) {
            NativeImage.registerMethod(method);
        }
        return mh;
    }

    public <T extends Throwable> MethodHandle unreflectConstructor(Constructor<?> constructor, ThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(constructor, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerConstructor(constructor);
        }
        return mh;
    }

    public <T extends Throwable> MethodHandle unreflectGetter(Field field, ThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(field, tf);
        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }
    }

    public <T extends Throwable> MethodHandle unreflectSetter(Field field, ThrowableFactory<T> tf) throws T {

        Lookup lookup = lookup(field, tf);
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }
    }

    public <T extends Throwable> VarHandle unreflectVarhandle(Field field, ThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(field, tf);
        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a VarHandle", e);
        }
    }

    private static boolean needsPrivateLookup(Member m) {
        // Needs private lookup, unless class is public or protected and member is public
        int decMod = m.getDeclaringClass().getModifiers();
        return !((Modifier.isPublic(decMod) || Modifier.isProtected(decMod)) && Modifier.isPublic(m.getModifiers()));
    }
}
// Check to see, if we need to use a private lookup

// if (needsPrivateLookup(constructor)) {
// // TODO check module access on lookup object, lol see next comment. No need to check
//
// try {
// lookup = MethodHandles.privateLookupIn(onType, lookup);
// } catch (IllegalAccessException e) {
// // This should never happen, because we have checked all preconditions
// // And we use our own lookup object which have Module access mode enabled.
// // Maybe something with unnamed modules...
// throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details",
// e);
// }
// }
//
// // Finally, lets unreflect the constructor
// MethodHandle methodHandle;
// try {
// methodHandle = lookup.unreflectConstructor(constructor);
// } catch (IllegalAccessException e) {
// throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details",
// e);
// }