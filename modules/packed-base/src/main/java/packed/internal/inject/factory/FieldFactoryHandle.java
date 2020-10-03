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
package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.Nullable;
import app.packed.base.TypeLiteral;
import app.packed.introspection.FieldDescriptor;
import packed.internal.inject.dependency.DependencyDescriptor;

/** An invoker that can read and write fields. */
final class FieldFactoryHandle<T> extends FactoryHandle<T> {

    /** The field we invoke. */
    final FieldDescriptor field;

    /** A var handle that can be used to read the field. */
    @Nullable
    final VarHandle varHandle;
    private final List<DependencyDescriptor> dependencies;

    @SuppressWarnings("unchecked")
    FieldFactoryHandle(FieldDescriptor field, List<DependencyDescriptor> dependencies) {
        super((TypeLiteral<T>) field.getTypeLiteral());
        this.field = field;
        this.varHandle = null;
        this.dependencies = dependencies;

    }

    FieldFactoryHandle(TypeLiteral<T> typeLiteralOrKey, FieldDescriptor field, VarHandle varHandle, List<DependencyDescriptor> dependencies) {
        super(typeLiteralOrKey);
        this.field = requireNonNull(field);
        this.varHandle = varHandle;
        this.dependencies = dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public MethodType methodType() {
        return MethodType.methodType(varHandle.varType());
    }

    /**
     * Compiles the code to a single method handle.
     * 
     * @return the compiled method handle
     */
    @Override
    public MethodHandle toMethodHandle() {
        MethodHandle mh = varHandle.toMethodHandle(Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET);
        // if (instance != null) {
        // mh = mh.bindTo(instance);
        // }
        return mh;
    }

    /**
     * Returns a new internal factory that uses the specified lookup object to instantiate new objects.
     * 
     * @param lookup
     *            the lookup object to use
     * @return a new internal factory that uses the specified lookup object
     */
    @Override
    public FieldFactoryHandle<T> withLookup(Lookup lookup) {
        VarHandle handle;
        try {
            if (Modifier.isPrivate(field.getModifiers())) {
                lookup = lookup.in(field.getDeclaringClass());
            }
            handle = field.unreflectVarHandle(lookup);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException("No access to the field " + field + ", use lookup(MethodHandles.Lookup) to give access", e);
        }
        return new FieldFactoryHandle<>(returnType(), field, handle, dependencies());
    }

    /** {@inheritDoc} */
    @Override
    public List<DependencyDescriptor> dependencies() {
        return dependencies;
    }
}
