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
package internal.app.packed.bean.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.List;

import app.packed.base.Key;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.inject.DependencyNode;
import internal.app.packed.inject.DependencyProducer;
import internal.app.packed.inject.InternalDependency;

/**
 *
 */
public abstract class DependencyHolder extends KeyProvidable {

    /** Dependencies that needs to be resolved. */
    public final List<InternalDependency> dependencies;
//
//    @Nullable
//    public final Consumer<? super ComponentSetup> processor = null;

    public final boolean provideAsConstant;

    // Jeg tror man loeber alle parameterene igennem og ser om der
    // er en sidecar provide der passer dem
    // Saa man sidecar providen dertil.

    DependencyHolder(List<InternalDependency> dependencies, boolean provideAsConstant, Key<?> provideAsKey) {
        super(provideAsKey);
        this.dependencies = requireNonNull(dependencies);
        this.provideAsConstant = provideAsConstant;
        // this.processor = builder.processor;
    }

    public void onWire(BeanSetup bean) {
        // Register hooks, maybe move to component setup
        DependencyNode node = new BeanMemberDependencyNode(bean, this, createProviders());

        bean.parent.injectionManager.addConsumer(node);

//        if (processor != null) {
//            processor.accept(bean);
//        }
    }

    public abstract DependencyProducer[] createProviders();

    /**
     * Returns the modifiers of the underlying member.
     * 
     * @return the modifiers of the underlying member
     * 
     * @see Member#getModifiers()
     */
    public abstract int getModifiers();

    public abstract MethodHandle methodHandle();

}
