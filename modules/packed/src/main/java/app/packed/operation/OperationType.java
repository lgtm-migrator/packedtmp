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
package app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * An operation type represents the arguments and return variable for an operation.
 * 
 * @apiNote This class is modelled after {@link MethodType}. But uses {@link Type} instead of {@link Class} as the base
 *          element, additional in includes annotations on element.
 */

// Mit problem med den her er lidt method return type...
// Altsaa annoteringer er jo ikke noedvendigvis knyttede til retur typen, fx @Get
public final /* primitive */ class OperationType {

    /** May be used for operation types without parameter variables. */
    private static volatile Variable[] NO_PARAMETERS = {};

    /** The parameter variables. */
    private final Variable[] parameterVars;

    /** The return variable. */
    private final Variable returnVar;

    private OperationType(Variable returnVar, Variable... variables) {
        this.returnVar = returnVar;
        this.parameterVars = variables;
    }

    /**
     * Return an operation type that is identical to this one, except that the return variable has been changed to the
     * specified variable.
     *
     * @param newReturn
     *            the variable used for the new return type
     * @return the new operation type
     */
    public OperationType changeReturnVariable(Variable newReturn) {
        requireNonNull(newReturn, "newReturn is null");
        return new OperationType(newReturn, parameterVars);
    }

    /**
     * Compares the specified object with this optional type for equality. That is, it returns {@code true} if and only if
     * the specified object is also an operation type with exactly the same parameters and return variable.
     * 
     * @param obj
     *            object to compare
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof OperationType t && (this == obj || returnVar.equals(t.returnVar) && Arrays.deepEquals(parameterVars, t.parameterVars));
    }

    /**
     * Returns the hash code value for this operation type.
     * <p>
     * It is defined to be the same as the hash code of a List whose elements are the return variable followed by the
     * parameter variables.
     * 
     * @return the hash code value for this function type
     * @see Object#hashCode()
     * @see #equals(Object)
     * @see List#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 31 + returnVar.hashCode();
        for (Variable ptype : parameterVars) {
            hashCode = 31 * hashCode + ptype.hashCode();
        }
        return hashCode;
    }

    public Variable parameter(int index) {
        return parameterVars[index];
    }

    /**
     * Return an array of field descriptors for the parameter types of the method type described by this descriptor
     * 
     * @return field descriptors for the parameter types
     * @apiNote freezeable arrays might be supported in the future. In which case we will not return a copy
     */
    public Variable[] parameterArray() {
        return Arrays.copyOf(parameterVars, parameterVars.length);
    }

    /** {@return the number of parameter variables in this operation type.} */
    public int parameterCount() {
        return parameterVars.length;
    }

    /**
     * Return an immutable list of the parameter variable of this operation typer
     * 
     * @return field descriptors for the parameter types
     */
    public List<Variable> parameterList() {
        return List.of(parameterVars);
    }

    private Class<?>[] rawParameterTypeArray() {
        Class<?>[] params = new Class<?>[parameterVars.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = parameterVars[i].getType();
        }
        return params;
    }

    /** {@return the return variable.} */
    public Class<?> returnType() {
        return returnVar.getType();
    }

    /** {@return the return variable.} */
    public Variable returnVariable() {
        return returnVar;
    }

    /**
     * {@return the return variable and each parameter variable as a MethodType.}
     */
    public MethodType toMethodType() {
        return switch (parameterVars.length) {
        case 0 -> MethodType.methodType(returnVar.getType());
        case 1 -> MethodType.methodType(returnVar.getType(), parameterVars[0].getType());
        default -> MethodType.methodType(returnVar.getType(), rawParameterTypeArray());
        };
    }

    /**
     * Returns a string representation of the function type, of the form {@code "(PT0,PT1...)RT"}. The string representation
     * of a function type is a parenthesis enclosed, comma separated list of type names, followed immediately by the return
     * variable.
     * <p>
     * Each type is represented by its {@link java.lang.Class#getSimpleName simple name}.
     */
    @Override
    public String toString() {
        // TODO We should probably call something else that toString();
        StringJoiner sj = new StringJoiner(",", "(", ")" + returnVar.toString());
        for (int i = 0; i < parameterVars.length; i++) {
            sj.add(parameterVars[i].toString());
        }
        return sj.toString();
    }

    public static void mainx(String[] args) throws Throwable {
        Field f = OperationType.class.getDeclaredField("NO_PARAMETERS");
        System.out.println(f);
        VarHandle vh = MethodHandles.lookup().unreflectVarHandle(f);

        vh.getVolatile();
        MethodHandles.lookup().unreflectGetter(f).invoke();
        System.out.println(vh.hasInvokeExactBehavior());

        MethodType mt = vh.accessModeType(AccessMode.COMPARE_AND_EXCHANGE);
        System.out.println(OperationType.ofFieldAccess(f, AccessMode.COMPARE_AND_EXCHANGE_RELEASE));
        System.out.println(mt);
    }

    public static OperationType of(Class<?> returnType) {
        requireNonNull(returnType, "returnType is null");
        return new OperationType(Variable.of(returnType), NO_PARAMETERS);
    }

    public static OperationType of(Class<?> returnVar, Class<?>... vars) {
        return ofMethodType(MethodType.methodType(returnVar, vars));
    }

    /**
     * Returns a function type with the given return variable. The resulting function type has no parameter variables.
     * 
     * @param returnVariable
     *            the return variable
     * @return a operation type with the given return and no parameters
     */
    public static OperationType of(Variable returnVariable) {
        requireNonNull(returnVariable, "returnVariable is null");
        return new OperationType(returnVariable, NO_PARAMETERS);
    }

    public static OperationType of(Variable returnVariable, Variable parameter) {
        requireNonNull(returnVariable, "returnVariable is null");
        requireNonNull(parameter, "parameter");
        return new OperationType(returnVariable, parameter);
    }

    public static OperationType of(Variable returnVariable, Variable... parameters) {
        requireNonNull(returnVariable, "returnVar is null");
        requireNonNull(parameters, "parameters is null");
        Variable[] vars = new Variable[parameters.length];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = requireNonNull(parameters[i]);
        }
        return new OperationType(returnVariable, vars);
    }

    /**
     * {@return an op type representing the signature of the specified executable.}
     * 
     * @param executable
     *            the executable to return a op type for.
     */
    public static OperationType ofExecutable(Executable executable) {
        // Is wether or not we need the bean a boolean property?
        requireNonNull(executable, "executable is null");
        Variable returnVariable = executable instanceof Method m ? Variable.ofMethodReturnType(m) : Variable.ofConstructor((Constructor<?>) executable);
        Parameter[] parameters = executable.getParameters();
        if (parameters.length == 0) {
            return new OperationType(returnVariable, NO_PARAMETERS);
        } else {
            Variable[] vars = new Variable[parameters.length];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = Variable.ofParameter(parameters[i]);
            }
            return new OperationType(returnVariable, vars);
        }
    }

    // I think we can move this internally
    public static OperationType ofFieldAccess(Field field, AccessMode accessMode) {
        requireNonNull(field, "field is null");
        requireNonNull(accessMode, "accessMode is null");
        Variable fieldVar = Variable.ofField(field);
        switch (accessMode) {
        case GET:
        case GET_VOLATILE:
        case GET_ACQUIRE:
        case GET_OPAQUE:
            return of(fieldVar);
        case SET:
        case SET_VOLATILE:
        case SET_RELEASE:
        case SET_OPAQUE:
            return of(Variable.of(void.class), fieldVar);
        case COMPARE_AND_SET:
        case WEAK_COMPARE_AND_SET:
        case WEAK_COMPARE_AND_SET_ACQUIRE:
        case WEAK_COMPARE_AND_SET_PLAIN:
        case WEAK_COMPARE_AND_SET_RELEASE:
            return of(Variable.of(boolean.class), fieldVar);
        case COMPARE_AND_EXCHANGE:
        case COMPARE_AND_EXCHANGE_ACQUIRE:
        case COMPARE_AND_EXCHANGE_RELEASE:
            return of(fieldVar, fieldVar, fieldVar);
        default: // getAndUpdate
            return of(fieldVar, fieldVar);
        }
    }

    public static OperationType ofFieldGet(Field field) {
        requireNonNull(field, "field is null");
        return OperationType.of(Variable.ofField(field));
    }

    public static OperationType ofFieldSet(Field field) {
        requireNonNull(field, "field is null");
        return OperationType.of(Variable.of(void.class), Variable.ofField(field));
    }

    /**
     * {@return an operation type representing the signature of the specified executable.}
     * 
     * @param methodType
     *            the method type to convert
     */
    public static OperationType ofMethodType(MethodType methodType) {
        requireNonNull(methodType, "methodType is null");
        Variable returnVar = Variable.of(methodType.returnType());
        int count = methodType.parameterCount();
        if (count == 0) {
            return new OperationType(returnVar, NO_PARAMETERS);
        } else {
            Variable[] vars = new Variable[count];
            for (int i = 0; i < count; i++) {
                vars[i] = Variable.of(methodType.parameterType(i));
            }
            return new OperationType(returnVar, vars);
        }
    }

    class NonStatic {
        List<?> f;

        NonStatic(List<?> l) {}

        public void me(List<?> l) {}
    }

    static class Static {
        List<?> f;

        Static(List<?> l) {}

        public void me(List<?> l) {}
    }
}
