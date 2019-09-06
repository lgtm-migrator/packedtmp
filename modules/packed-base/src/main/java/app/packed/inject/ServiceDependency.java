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
package app.packed.inject;

import java.lang.reflect.Member;
import java.util.Optional;

import app.packed.util.ConstructorDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;

/**
 * A descriptor of a dependency. An instance of this class is typically created from a parameter on a constructor or
 * method. In which case the parameter (represented by a {@link ParameterDescriptor}) can be obtained by calling
 * {@link #variable()}. A descriptor can also be created from a field, in which case {@link #variable()} returns an
 * instance of {@link FieldDescriptor}. Dependencies can be optional in which case {@link #isOptional()} returns true.
 */
public interface ServiceDependency {

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    boolean isOptional();

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    Key<?> key();

    /**
     * The member (field, method or constructor) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a member.
     * <p>
     * If this dependency was created from a member this method will an optional containing either a {@link FieldDescriptor}
     * in case of field injection, A {@link MethodDescriptor} in case of method injection or a {@link ConstructorDescriptor}
     * in case of constructor injection.
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #variable()
     */
    // Vi skal have MemberDescriptor, og saa et sealed interface
    Optional<Member> member();

    default <T extends Member> T member(Class<T> memberType) {
        return memberType.cast(member().get());
    }

    /**
     * If this dependency represents a parameter to a constructor or method. This method will return the index of the
     * parameter, otherwise {@code -1}.
     * 
     * @apiNote While it would be natural for this method to return OptionalInt. We have found that in most use cases it has
     *          already been established whether a parameter is present via the optional return by {@link #variable()}.
     * 
     * @return the optional parameter index of the dependency
     */
    int parameterIndex();

    Optional<VariableDescriptor> variable();
}
