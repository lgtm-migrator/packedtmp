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
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.InaccessibleMemberException;
import app.packed.base.TypeLiteral;
import app.packed.inject.Factory;
import app.packed.introspection.ExecutableDescriptor;
import app.packed.introspection.MethodDescriptor;
import packed.internal.classscan.util.ConstructorUtil;
import packed.internal.inject.dependency.DependencyDescriptor;

/** The backing class of {@link Factory}. */
public final class ExecutableFactoryHandle<T> extends FactoryHandle<T> {

    /**
     * Whether or not we need to check the lower bound of the instances we return. This is only needed if we allow, for
     * example to register CharSequence fooo() as String.class. And I'm not sure we allow that..... Maybe have a special
     * Factory.overrideMethodReturnWith(), and then not allow it as default..
     */
    final boolean checkLowerBound;

    private final List<DependencyDescriptor> dependencies;

    /** A factory with an executable as a target. */
    public final ExecutableDescriptor executable;

    private final Object instance = null;

    @SuppressWarnings("unchecked")
    public ExecutableFactoryHandle(MethodDescriptor methodDescriptor, List<DependencyDescriptor> dependencies) {
        super((TypeLiteral<T>) methodDescriptor.returnTypeLiteral());
        this.executable = methodDescriptor;
        this.checkLowerBound = false;
        this.dependencies = dependencies;
    }

    public ExecutableFactoryHandle(TypeLiteral<T> key, ExecutableDescriptor executable, List<DependencyDescriptor> dependencies) {
        super(key);
        this.executable = executable;
        this.checkLowerBound = false;
        this.dependencies = dependencies;
    }

    @Override
    public List<DependencyDescriptor> dependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle toMethodHandle(Lookup lookup) {
        MethodHandle methodHandle;
        try {
            if (Modifier.isPrivate(executable.getModifiers())) {
                lookup = lookup.in(executable.getDeclaringClass());
            }
            methodHandle = executable.unreflect(lookup);
        } catch (IllegalAccessException e) {
            throw new InaccessibleMemberException(
                    "No access to the " + executable.descriptorTypeName() + " " + executable + " with the specified lookup object", e);
        }

        MethodHandle mh = methodHandle;
        if (executable.isVarArgs()) {
            mh = mh.asFixedArity();
        }
        if (instance != null) {
            return mh.bindTo(instance);
        }
        return mh;
    }

    @Override
    public String toString() {
        return executable.toString();
    }

    static <T> FactoryHandle<T> find(Class<T> implementation) {
        ExecutableDescriptor executable = findExecutable(implementation);
        return new ExecutableFactoryHandle<>(TypeLiteral.of(implementation), executable, DependencyDescriptor.fromExecutable(executable));
    }

    static <T> FactoryHandle<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        ExecutableDescriptor executable = findExecutable(implementation.rawType());
        return new ExecutableFactoryHandle<>(implementation, executable, DependencyDescriptor.fromExecutable(executable));
    }

    // Should we have a strict type? For example, a static method on MyExtension.class
    // must return MyExtension... Det maa de sgu alle.. Den anden er findMethod()...
    // MyExtension.class create()
    private static ExecutableDescriptor findExecutable(Class<?> type) {
        return ExecutableDescriptor.from(ConstructorUtil.findInjectableIAE(type));
    }
}
