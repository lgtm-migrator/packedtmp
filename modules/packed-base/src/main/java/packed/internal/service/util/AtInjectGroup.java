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
package packed.internal.service.util;

import java.util.ArrayList;
import java.util.List;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.HookGroupBuilder;
import app.packed.hook.OnHook;
import app.packed.reflect.FieldDescriptor;
import app.packed.reflect.MethodDescriptor;
import app.packed.service.Inject;
import app.packed.service.ServiceDependency;

/**
 *
 */
public final class AtInjectGroup {

    /** An immutable map of all providing members. */
    public final List<AtInject> members;

    /**
     * Creates a new provides group
     * 
     * @param builder
     *            the builder to create the group for
     */
    private AtInjectGroup(Builder builder) {
        this.members = builder.members == null ? List.of() : List.copyOf(builder.members);
    }

    /** A builder for {@link AtInjectGroup}. */
    public final static class Builder implements HookGroupBuilder<AtInjectGroup> {

        /** A set of all keys for every provided service. */
        private final ArrayList<AtInject> members = new ArrayList<>();

        /**
         * Creates a new group from this builder.
         * 
         * @return the new group
         */
        @Override
        public AtInjectGroup build() {
            return new AtInjectGroup(this);
        }

        @OnHook
        void onFieldInject(AnnotatedFieldHook<Inject> hook) {
            hook.checkNotFinal().checkNotStatic();
            FieldDescriptor field = hook.field();
            members.add(new AtInject(hook.setter(), field, List.of(ServiceDependency.fromField(field))));
        }

        @OnHook
        void onMethodProvide(AnnotatedMethodHook<Inject> hook) {
            MethodDescriptor method = hook.method();
            List<ServiceDependency> dependencies = ServiceDependency.fromExecutable(method);

            // TestNotStatic... Hmm kan ikke kalde hook.checkNotStatic mere...

            // We have static @Inject methods that constructs
            if (!hook.method().isStatic()) {
                members.add(new AtInject(hook.methodHandle(), method, dependencies));
            }
        }
    }

}
