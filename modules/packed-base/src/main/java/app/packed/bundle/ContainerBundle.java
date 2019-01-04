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
package app.packed.bundle;

import app.packed.container.ComponentConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.Provides;
import app.packed.lifecycle.OnStart;
import app.packed.util.TypeLiteral;
import packed.internal.container.ContainerBuilder;

/**
 *
 */
public abstract class ContainerBundle extends Bundle {

    /** {@inheritDoc} */
    @Override
    ContainerBuilder injectorBuilder() {
        throw new UnsupportedOperationException();
    }

    /**
     * Installs the specified component implementation. This method is short for
     * {@code install(Factory.findInjectable(implementation))} which basically finds a valid constructor/static method (as
     * outlined in {@link Factory#findInjectable(Class)}) to instantiate the specified component implementation.
     *
     * @param <T>
     *            the type of component to install
     * @param implementation
     *            the component implementation to install
     * @return a component configuration that can be use to configure the component in greater detail
     */
    protected final <T> ComponentConfiguration<T> install(Class<T> implementation) {
        return injectorBuilder().install(implementation);
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provides}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    protected final <T> ComponentConfiguration<T> install(Factory<T> factory) {
        return injectorBuilder().install(factory);
    }

    /**
     * Install the specified component instance.
     * <p>
     * If this install operation is the first install operation of the container. The component will be installed as the
     * root component of the container. All subsequent install operations on this bundle will have have component as its
     * parent. If you wish to have a specific component as a parent, the various install methods on
     * {@link ComponentConfiguration} can be used to specify a specific parent.
     *
     * @param <T>
     *            the type of component to install
     * @param instance
     *            the component instance to install
     * @return this configuration
     */
    protected final <T> ComponentConfiguration<T> install(T instance) {
        return injectorBuilder().install(instance);
    }

    protected final <T> ComponentConfiguration<T> install(TypeLiteral<T> implementation) {
        return injectorBuilder().install(implementation);
    }

    protected final void installContainer(Class<? extends ContainerBundle> bundleType, BundlingStage... stages) {
        injectorBuilder().installContainer(bundleType, stages);
    }

    protected final void installContainer(ContainerBundle bundle, BundlingStage... stages) {
        injectorBuilder().installContainer(bundle, stages);
    }
}
