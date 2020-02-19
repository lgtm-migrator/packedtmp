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
package app.packed.artifact;

import app.packed.container.ContainerSource;

/**
 *
 */
//Ved ikke hvor glad jeg er for navnet

// Syntes ikke det er fedt den extender

//TODO remove ArtifactSource
/// This is not an artifact source... but it creates one...
@FunctionalInterface
public interface ArtifactComposer<T> extends ArtifactSource, ContainerSource {

    /**
     * Configure the artifact.
     * 
     * @param configurator
     *            the configurator used to configure the artifact
     */
    void compose(T configurator);
}
