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
package packed.internal.host;

import static java.util.Objects.requireNonNull;

import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactSource;
import app.packed.component.ComponentDescriptor;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import packed.internal.artifact.PackedArtifactImage;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.component.BaseComponent;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.host.api.HostConfigurationContext;

/**
 * The defa
 */
// We don't actually store the HostConfiguration in this class.

public final class PackedHostConfiguration extends AbstractComponentConfiguration implements HostConfigurationContext {

    /**
     * @param configSite
     * @param parent
     */
    public PackedHostConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent) {
        super(configSite, parent);
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(ArtifactSource source, ArtifactDriver<?> driver, Wirelet... wirelets) {
        requireNonNull(source, "source is null");
        requireNonNull(driver, "driver is null");

        PackedArtifactImage img;
        if (source instanceof PackedArtifactImage) {
            img = ((PackedArtifactImage) source).with(wirelets);
        } else {
            img = PackedArtifactImage.of((Bundle) source, wirelets);
        }

        PackedGuestConfiguration pgc = new PackedGuestConfiguration(this, img.configuration(), img);
        pgc.initializeName(State.LINK_INVOKED, null);

        addChild(pgc);
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        // Vi burde kunne extract AppHost fra <T>
        return "Host"; // Host for now, But if we have host driver...
    }

    /** {@inheritDoc} */
    @Override
    protected BaseComponent instantiate(BaseComponent parent, PackedInstantiationContext ic) {
        return new BaseComponent(parent, this, ic, ComponentDescriptor.COMPONENT_INSTANCE);
    }

    /** {@inheritDoc} */
    @Override
    public PackedHostConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedHostConfiguration setName(String name) {
        super.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDescriptor type() {
        return ComponentDescriptor.COMPONENT_INSTANCE;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentDescriptor descritor() {
        throw new UnsupportedOperationException();
    }
}
