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

import app.packed.base.InaccessibleMemberException;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.util.NativeImage;
import packed.internal.util.StringFormatter;

/**
 * An open class is a thin wrapper for a single class and a {@link Lookup} object.
 * <p>
 * This class is not safe for use with multiple threads.
 */
//TODO should we know whether or the lookup is Packed one or a user supplied??
// lookup.getClass().getModule==OpenClass.getModule...? nah virker ikke paa classpath
public final class OpenClass {

    /** The app.packed.base module. */
    private static final Module APP_PACKED_BASE_MODULE = OpenClass.class.getModule();

    /** A lookup object that can be used to access {@link #type}. */
    private final MethodHandles.Lookup lookup;

    /** A lookup that can be used on non-public members. */
    private MethodHandles.Lookup privateLookup;

    /** Whether or not the private lookup has been initialized. */
    private boolean privateLookupInitialized;

    /** Whether or not every unreflected action results in the member being registered for native image generation. */
    private final boolean registerForNative;

    /** The class that is wrapped. */
    private final Class<?> type;

    private OpenClass(MethodHandles.Lookup lookup, Class<?> clazz, boolean registerForNative) {
        this.lookup = requireNonNull(lookup);
        this.type = requireNonNull(clazz);
        this.registerForNative = registerForNative;
    }

    private Lookup lookup(Member member, UncheckedThrowableFactory<?> tf) {
        if (!member.getDeclaringClass().isAssignableFrom(type)) {
            throw new IllegalArgumentException("Was " + member.getDeclaringClass() + " expecting " + type);
        }

        // If we already have made a private lookup object, lets just use it. Even if we might need less access
        MethodHandles.Lookup p = privateLookup;
        if (p != null) {
            return p;
        }

        // See if we need private access, otherwise just return ordinary lookup.
        if (!needsPrivateLookup(member)) {
            return lookup;
        }

        if (!privateLookupInitialized) {
            String pckName = type.getPackageName();
            if (!type.getModule().isOpen(pckName, APP_PACKED_BASE_MODULE)) {
                String otherModule = type.getModule().getName();
                String m = APP_PACKED_BASE_MODULE.getName();
                throw new InaccessibleMemberException("In order to access '" + StringFormatter.format(type) + "', the module '" + otherModule + "' must be open to '" + m
                        + "'. This can be done, for example, by adding 'opens " + pckName + " to " + m + ";' to the module-info.java file of " + otherModule);
            }
            // Should we use lookup.getdeclaringClass???
            if (!APP_PACKED_BASE_MODULE.canRead(type.getModule())) {
                APP_PACKED_BASE_MODULE.addReads(type.getModule());
            }
            privateLookupInitialized = true;
        }

        // Create and cache a private lookup.
        try {
            // Fjernede lookup... Skal vitterligt have samlet det i en klasse
            return privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /*lookup */);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create private lookup [type=" + type + ", Member = " + member + "]", e);
        }
    }

    /**
     * Returns the class that is processed.
     * 
     * @return the class that is processed
     */
    public Class<?> type() {
        return type;
    }

    public MethodHandle unreflect(Method method) {
        return unreflect(method, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
    }

    /**
     * @param method
     *            the method to unreflect
     * @return a method handle for the unreflected method
     */
    public MethodHandle unreflect(Method method, UncheckedThrowableFactory<?> tf) {
        Lookup lookup = lookup(method, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("stuff", e);
        }

        if (registerForNative) {
            NativeImage.register(method);
        }
        return mh;
    }

    public MethodHandle unreflectConstructor(Constructor<?> constructor) {
        return unreflectConstructor(constructor, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
    }
    public MethodHandle unreflectConstructor(Constructor<?> constructor, UncheckedThrowableFactory<?> tf) {
        Lookup lookup = lookup(constructor, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.register(constructor);
        }
        return mh;
    }
    public MethodHandle unreflectGetter(Field field) {
        return unreflectGetter(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
    }

    public MethodHandle unreflectGetter(Field field, UncheckedThrowableFactory<?> tf) {
        Lookup lookup = lookup(field, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerField(field);
        }
        return mh;
    }
    public MethodHandle unreflectSetter(Field field) {
        return unreflectSetter(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
    }

    public MethodHandle unreflectSetter(Field field, UncheckedThrowableFactory<?> tf) {
        Lookup lookup = lookup(field, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerField(field);
        }
        return mh;
    }
    public VarHandle unreflectVarHandle(Field field) {
        return unreflectVarHandle(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
    }

    public <T extends RuntimeException> VarHandle unreflectVarHandle(Field field, UncheckedThrowableFactory<T> tf) {
        Lookup lookup = lookup(field, tf);

        VarHandle vh;
        try {
            vh = lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("Could not create a VarHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerField(field);
        }
        return vh;
    }

    private static boolean needsPrivateLookup(Member m) {
        // Needs private lookup, unless class is public or protected and member is public
        int decMod = m.getDeclaringClass().getModifiers();
        return !((Modifier.isPublic(decMod) || Modifier.isProtected(decMod)) && Modifier.isPublic(m.getModifiers()));
    }

    public static OpenClass of(MethodHandles.Lookup lookup, Class<?> clazz) {
        return new OpenClass(lookup, clazz, true);
    }
}

//IDeen er at man kan specificere den til OpenClass...
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