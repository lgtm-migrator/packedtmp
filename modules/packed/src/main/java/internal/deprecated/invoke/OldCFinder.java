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
package internal.deprecated.invoke;

import static internal.app.packed.util.StringFormatter.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import app.packed.bean.Inject;
import internal.app.packed.framework.devtools.PackedDevToolsIntegration;
import internal.app.packed.util.StringFormatter;

/**
 *
 */
final class OldCFinder {

    /**
     * @param clazz
     *            the class to scan
     * @param allowInjectAnnotation
     *            whether or not we allow usage of {@link Inject}. If not, the specified class must have a single
     *            constructor
     * @param errorMaker
     *            invoked with an error message if something goes wrong
     * @return the constructor
     */
    // Taenker vi skal have en exception der specifikt naevner noget med constructor
    // NoConstructorExtension
    // InjectableConstructorMissingException
    // MissingInjectableConstructorException
    // ConstructorInjectionException (lyder mere som noget vi ville smide naar vi instantiere det
    public static Constructor<?> getConstructor(Class<?> clazz, boolean allowInjectAnnotation, Function<String, RuntimeException> errorMaker) {
        if (clazz.isAnnotation()) { // must be checked before isInterface
            String errorMsg = format(clazz) + " is an annotation and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isInterface()) {
            String errorMsg = format(clazz) + " is an interface and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isArray()) {
            String errorMsg = format(clazz) + " is an array and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isPrimitive()) {
            String errorMsg = format(clazz) + " is a primitive class and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            String errorMsg = format(clazz) + " is an abstract class and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        }

        // Get all declared constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        PackedDevToolsIntegration.INSTANCE.reflectMembers(clazz, constructors);

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return constructors[0];
        } else if (!allowInjectAnnotation) {
            StringBuilder sb = new StringBuilder();
            sb.append(clazz).append(" must declare exactly 1 constructor, [constructors = ");
            StringJoiner sj = new StringJoiner(", ");
            List.of(constructors).forEach(c -> sj.add(StringFormatter.formatShortParameters(c)));
            sb.append(sj).append("]");
            throw errorMaker.apply(sb.toString());
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    String errorMsg = "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(clazz);
                    throw errorMaker.apply(errorMsg);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single public constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isPublic(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "public", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single protected constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isProtected(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "protected", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Remaining constructors are either private or package private constructors
        for (Constructor<?> c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "package-private", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Only private constructors left, and we have already checked whether or not we only have a single method
        // So we must have more than 1 private methods
        throw getErrMsg(clazz, "private", errorMaker);
    }

    private static RuntimeException getErrMsg(Class<?> type, String visibility, Function<String, RuntimeException> errorMaker) {
        String errorMsg = "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple " + visibility + " constructors on class "
                + format(type);
        return errorMaker.apply(errorMsg);
    }
}
