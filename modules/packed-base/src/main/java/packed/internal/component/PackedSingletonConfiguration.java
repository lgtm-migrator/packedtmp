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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerSource;
import app.packed.service.Factory;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.moduleaccess.ModuleAccess;

/**
 *
 */
public final class PackedSingletonConfiguration<T> extends AbstractComponentConfiguration implements SingletonConfiguration<T> {

    public final ComponentModel componentModel;

    public final Factory<T> factory;

    public final T instance;

    public PackedSingletonConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent, ComponentModel componentModel, Factory<T> factory) {
        super(configSite, parent);
        this.componentModel = requireNonNull(componentModel);
        this.factory = requireNonNull(factory);
        this.instance = null;
    }

    public PackedSingletonConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent, ComponentModel componentModel, T instance) {
        super(configSite, parent);
        this.componentModel = requireNonNull(componentModel);
        this.factory = null;
        this.instance = requireNonNull(instance);
    }

    public MethodHandle fromFactory() {
        FactoryHandle<?> handle = ModuleAccess.service().toSupport(factory).handle;
        return container().fromFactoryHandle(handle);
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        return componentModel.defaultPrefix();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractComponent instantiate(AbstractComponent parent, PackedArtifactInstantiationContext paic) {
        return new PackedSingleton(parent, this, paic);
    }

    public PackedSingletonConfiguration<T> runHooks(ContainerSource source) {
        componentModel.invokeOnHookOnInstall(source, this);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedSingletonConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedSingletonConfiguration<T> setName(String name) {
        super.setName(name);
        return this;
    }
}
