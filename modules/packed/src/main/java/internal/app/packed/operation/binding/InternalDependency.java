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
package internal.app.packed.operation.binding;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.application.BuildException;
import app.packed.framework.Nullable;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import app.packed.service.Key;
import app.packed.service.TypeToken;
import internal.app.packed.errorhandling.ErrorMessageBuilder;
import internal.app.packed.util.BasePackageAccess;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.QualifierUtil;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/**
 * A descriptor of a dependency. An instance of this class is typically created from a parameter on a constructor or
 * method. In which case the parameter (represented by a {@link Parameter}) can be obtained by calling
 * {@link #variable()}. A descriptor can also be created from a field, in which case {@link #variable()} returns an
 * instance of. Dependencies can be optional in which case {@link #isOptional()} returns true.
 */
// Declaring class for use with Type Variables???
// Det her er ogsaa en Const..

// Supporterer vi noget mht til AnnotatedProvider?????
// Er det et nyt lag....
// Det der er, er at vi jo faktisk kan overskriver provideren...
// Dvs den er ikke statisk....

// int = Integer
// OptionalInt = Optional<Integer> = @Nullable Integer
// @Nullable int (Forbidden)

// Saa faar ogsaa lige pludselig.... Vi behoever vist en ny historie her......

// DefaultValue... (We need some default values converters...... Unless we have default converters...)
// Prime annotation (Den ødelægger jo lidt Key/Lazy/OSV, hvad hvis den nu vil noget andet...)
// Provider
// Lazy (Hvordan supportere vi denne???)

// Composite..... wtf... Det kan jo være 4 dependencies (Hirakiske dependencies)....
// Bliver helt klart noedt til at rethinke it.
// 1 parameter != 1 dependency...
// Requesting entity != key we ask a provided service for..
//// For example, we ask for a composite -> 5 calls (maybe to 3 different service providers)

//// Maaske skal den slet ikke exposes til brugere....
//// Men kun vaere en vi har internt.....

//// Den er vel meget god til Factory???
//// Men hvis vi nu bruger en composite... Giver den jo ingen mening...
//// Hvad hvis vi har den samme dependency 2 gange...
//// Hvad hvis vi bruger @Prime annotation.. Saa faar vi jo nogle andre dependencies.
// Som ikke er synlige...

//Dependency Chain.... 

//Dependency er flyttet til en intern klasse. Fordi den er begyndt at blive lidt for kompleks.
// Naar vi tilfoere composites. Hvor der ikke rigtig laengere er en parameter til en service mapning.

public final class InternalDependency {

    /** A cache of service dependencies. */
    private static final ClassValue<InternalDependency> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected InternalDependency computeValue(Class<?> type) {
            if (type == Optional.class) {
                throw new IllegalArgumentException("Cannot determine type variable <T> for type Optional<T>");
            } else if (type == OptionalInt.class) {
                return new InternalDependency(int.class, Key.of(Integer.class), Optionality.OPTIONAL_INT);
            } else if (type == OptionalLong.class) {
                return new InternalDependency(long.class, Key.of(Long.class), Optionality.OPTIONAL_LONG);
            } else if (type == OptionalDouble.class) {
                return new InternalDependency(double.class, Key.of(Double.class), Optionality.OPTIONAL_DOUBLE);
            }
            return new InternalDependency(type, Key.of(type), Optionality.REQUIRED);
        }
    };

    /** The key of this dependency. */
    private final Key<?> key;

    /** The optionality of this dependency. */
    private final Optionality optionality;

    /** The variable of this dependency. */
    @Nullable
    private final Variable variable;

    final Type type;

    /**
     * Creates a new service dependency.
     * 
     * @param key
     *            the key
     * @param optionality
     *            the optional type
     * @param variable
     *            an optional field or parameter
     */
    private InternalDependency(Type type, Key<?> key, Optionality optionality) {
        this.type = requireNonNull(type);
        this.key = requireNonNull(key, "key is null");
        this.optionality = requireNonNull(optionality);
        this.variable = null;
    }

    /**
     * Returns an object representing an optional dependency could not be fulfilled. For example, this method will return
     * {@link OptionalInt#empty()} if a dependency was created from a field with a {@link OptionalInt} type. And
     * {@code null} if a parameter is annotated with {@link Nullable}.
     * <p>
     * If this dependency is not optional this method throws an {@link UnsupportedOperationException}.
     * 
     * @return a matching optional empty type. Or null if the variable is annotated with {@link Nullable}.
     * @throws UnsupportedOperationException
     *             if this dependency is not optional
     * @see Nullable
     * @see Optional#empty()
     * @see OptionalLong#empty()
     * @see OptionalInt#empty()
     * @see OptionalDouble#empty()
     */
    @Nullable
    public Object emptyValue() {
        return optionality.empty(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj.getClass() != InternalDependency.class) {
            return false;
        }
        InternalDependency other = (InternalDependency) obj;
        // Hmm hashcode and equals for optional????
        return Objects.equals(key, other.key) && optionality == other.optionality && Objects.equals(variable, other.variable);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 31 + key.hashCode();
        result = 31 * result + optionality.ordinal();
        return 31 * result + Objects.hashCode(variable);
    }

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    public boolean isOptional() {
        return optionality != Optionality.REQUIRED;
    }

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceDependency[");
        if (optionality == Optionality.OPTIONAL_INT || optionality == Optionality.OPTIONAL_LONG || optionality == Optionality.OPTIONAL_DOUBLE) {
            sb.append(optionality.name());
        } else {
            sb.append(key);
        }
        if (variable != null) {
            sb.append(", ");
            sb.append(variable);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * The variable (field or parameter) from which this dependency originates. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     */
//    public Optional<Variable> variable() {
//        return Optional.empty();
//        // return Optional.ofNullable(variable);
//    }

    /**
     * TODO add nullable... Returns the specified object if not optional, or a the specified object in an optional type
     * (either {@link Optional}, {@link OptionalDouble}, {@link OptionalInt} or {@link OptionalLong}) if optional.
     *
     * @param object
     *            the object to potentially wrap in an optional type @return the specified object if not optional, or a the
     *            specified object in an optional type if optional.
     * 
     * @return the wrapped object if needed
     * @throws ClassCastException
     *             if this dependency is an optional type and type of this dependency does not match the specified object.
     */
    @Nullable
    public Object wrapIfOptional(Object object) {
        return optionality.wrapIfOptional(requireNonNull(object, "object is null"));
    }

    public static List<InternalDependency> fromOperationType(OperationType t) {
        Variable[] parameters = t.parameterArray();
        return switch (parameters.length) {
        case 0 -> List.of();
        case 1 -> List.of(fromVariable(parameters[0]));
        case 2 -> List.of(fromVariable(parameters[0]), fromVariable(parameters[1]));
        default -> {
            InternalDependency[] sd = new InternalDependency[parameters.length];
            for (int i = 0; i < sd.length; i++) {
                sd[i] = fromVariable(parameters[i]);
            }
            yield List.of(sd);
        }
        };
    }

    public static <T> InternalDependency fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        Type type = TypeVariableExtractor.of(baseClass, baseClassTypeVariableIndex).extract(actualClass);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) actualClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[0].getAnnotations();
        Annotation[] qa = QualifierUtil.findQualifier(annotations);

        Optionality optionalType = null;
        if (type instanceof ParameterizedType pt && pt.getRawType() == Optional.class) {
            type = pt.getActualTypeArguments()[0];
            // TODO check that we do not have optional of OptionalX, also ServiceRequest can never be optionally
            // Also Provider cannot be optionally...
            // TODO include annotation
            // Cannot have Nullable + Optional....
            optionalType = Optionality.OPTIONAL;
        } else if (type == OptionalInt.class) {
            optionalType = Optionality.OPTIONAL_INT;
            type = Integer.class;
        } else if (type == OptionalLong.class) {
            optionalType = Optionality.OPTIONAL_LONG;
            type = Long.class;
        } else if (type == OptionalDouble.class) {
            optionalType = Optionality.OPTIONAL_DOUBLE;
            type = Double.class;
        }
        if (optionalType == null) {
            optionalType = Optionality.REQUIRED;
        }
        // TODO check that there are no qualifier annotations on the type.
        return new InternalDependency(type, BasePackageAccess.base().toKeyNullableQualifier(type, qa), optionalType);
    }

    public static <T> List<InternalDependency> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        ArrayList<InternalDependency> result = new ArrayList<>();
        for (int i = 0; i < baseClassTypeVariableIndexes.length; i++) {
            result.add(fromTypeVariable(actualClass, baseClass, baseClassTypeVariableIndexes[i]));
        }
        return List.copyOf(result);
    }

    public static <T> InternalDependency fromVariable(Variable v) {
        requireNonNull(v, "variable is null");

        TypeToken<?> tl = v.typeToken();

        Annotation[] qualifiers = QualifierUtil.findQualifier(v.getAnnotations());

        // Illegal
        // Optional<Optional*>
        Optionality optionallaity = null;
        Class<?> rawType = tl.rawType();

        // if (desc instanceof ParameterDescriptor) {
        // ParameterDescriptor pd = (ParameterDescriptor) desc;
        // if (pd.isVarArgs()) {
        // throw new InvalidDeclarationException(ErrorMessageBuilder.of(desc).cannot("use varargs for injection for " +
        // pd.getDeclaringExecutable()));
        // }
        // }
        if (rawType.isPrimitive()) {
            tl = tl.wrap();
        } else if (rawType == Optional.class) {
            optionallaity = Optionality.OPTIONAL;
            Type cl = ((ParameterizedType) tl.type()).getActualTypeArguments()[0];
            tl = BasePackageAccess.base().toTypeLiteral(cl);
            if (ClassUtil.isOptional(tl.rawType())) {
                throw new BuildException(ErrorMessageBuilder.of(v).cannot("have multiple layers of optionals such as " + cl).toString());
            }
        } else if (rawType == OptionalLong.class) {
            optionallaity = Optionality.OPTIONAL_LONG;
            tl = TypeToken.of(Long.class);
        } else if (rawType == OptionalInt.class) {
            optionallaity = Optionality.OPTIONAL_INT;
            tl = TypeToken.of(Integer.class);
        } else if (rawType == OptionalDouble.class) {
            optionallaity = Optionality.OPTIONAL_DOUBLE;
            tl = TypeToken.of(Double.class);
        }

        if (v.isAnnotationPresent(Nullable.class)) {
            if (optionallaity != null) {
                // TODO fix name() to something more readable
                throw new BuildException(ErrorMessageBuilder.of(v).cannot("both be of type " + optionallaity.name() + " and annotated with @Nullable")
                        .toResolve("remove the @Nullable annotation, or make it a non-optional type").toString());
            }
            optionallaity = Optionality.OPTIONAL_NULLABLE;
        }

        if (optionallaity == null) {
            optionallaity = Optionality.REQUIRED;
        }
        // TL is free from Optional
        Key<?> key = Key.convertTypeLiteralNullableAnnotation(v, tl, qualifiers);

        return new InternalDependency(v.getType(), key, optionallaity);
    }

    /**
     * Returns a service dependency on the specified class.
     *
     * @param key
     *            the class to return a dependency for
     * @return a service dependency for the specified class
     */
    public static InternalDependency of(Class<?> key) {
        requireNonNull(key, "key is null");
        return CLASS_CACHE.get(key);
    }

    private enum Optionality {
        REQUIRED {
            @Override
            public Object empty(InternalDependency dependency) {
                throw new UnsupportedOperationException("This dependency is not optional, dependency = " + dependency);
            }
        },
        OPTIONAL {
            @Override
            public Object empty(InternalDependency dependency) {
                return Optional.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return Optional.of(object);
            }
        },
        OPTIONAL_INT {
            @Override
            public Object empty(InternalDependency dependency) {
                return OptionalInt.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return OptionalInt.of((Integer) object);
            }
        },
        OPTIONAL_LONG {
            @Override
            public Object empty(InternalDependency dependency) {
                return OptionalLong.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return OptionalLong.of((Long) object);
            }
        },
        OPTIONAL_DOUBLE {
            @Override
            public Object empty(InternalDependency dependency) {
                return OptionalDouble.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return OptionalDouble.of((Double) object);
            }
        },
        OPTIONAL_NULLABLE {
            @Override
            public Object empty(InternalDependency dependency) {
                return null;
            }
        };

        public abstract Object empty(InternalDependency dependency);

        public Object wrapIfOptional(Object object) {
            return null;
        }
    }

    public Class<?> rawType() {
        return (Class<?>) type;
    }
}
