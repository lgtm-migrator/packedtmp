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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.container.Extension;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/**
 * A cache of {@link Extension} implementations. Is mainly used for instantiating new instances of extensions.
 */
// Raekkefoelge af installeret extensions....
// Maaske bliver vi noedt til at have @UsesExtension..
// Saa vi kan sige X extension skal koeres foerend Y extension
final class ExtensionModel<T> {

    /** A cache of values. */
    private static final ClassValue<ExtensionModel<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected ExtensionModel<?> computeValue(Class<?> type) {
            return new ExtensionModel(type);
        }
    };

    /** The method handle used to create a new instance of the extension. */
    private final MethodHandle constructor;

    /** Whether or not the constructor needs an instanceof {@link PackedContainerConfiguration}. */
    private final boolean needsPackedContainerConfiguration;

    /** The type of extension. */
    private final Class<? extends Extension> extensionType;

    /**
     * Creates a new extension class cache.
     * 
     * @param type
     *            the extension type
     */
    private ExtensionModel(Class<? extends Extension> type) {
        this.extensionType = requireNonNull(type);
        boolean needsPackedContainerConfiguration = false;

        Constructor<?> constructor;
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            try {
                constructor = type.getDeclaredConstructor(PackedContainerConfiguration.class);
                needsPackedContainerConfiguration = true;
            } catch (NoSuchMethodException ee) {
                throw new IllegalArgumentException("The extension " + StringFormatter.format(type) + " must have a no-argument constructor to be installed.");
            }
        }
        this.needsPackedContainerConfiguration = needsPackedContainerConfiguration;

        Lookup lookup = MethodHandles.lookup();
        try {
            constructor.setAccessible(true);
            this.constructor = lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException | InaccessibleObjectException e) {
            throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(type) + ", the module '"
                    + type.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
        }

        NativeImage.registerConstructor(constructor);
    }

    /**
     * Creates a new extension of the specified type.
     * 
     * @param <T>
     *            the type of extension
     * @param extensionType
     *            the type of extension
     * @return a new instance of the extension
     */
    @SuppressWarnings("unchecked")
    static <T extends Extension> T newInstance(PackedContainerConfiguration pcc, Class<T> extensionType) {
        // Time goes from around 1000 ns to 12 ns when we cache, with LambdaMetafactory wrapped in supplier we can get to 6 ns
        ExtensionModel<T> ecc = (ExtensionModel<T>) CACHE.get(extensionType);
        try {
            if (ecc.needsPackedContainerConfiguration) {
                return (T) ecc.constructor.invoke(pcc);
            } else {
                return (T) ecc.constructor.invoke();
            }
        } catch (Throwable t) {
            ThrowableUtil.rethrowErrorOrRuntimeException(t);
            throw new UndeclaredThrowableException(t, "Could not instantiate extension '" + StringFormatter.format(ecc.extensionType) + "'");
        }
    }
}
