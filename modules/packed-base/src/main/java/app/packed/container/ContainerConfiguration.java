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
package app.packed.container;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.lang.Nullable;

/**
 * The configuration of a container. This class is rarely used directly. Instead containers are typically configured via
 * a bundle.
 */
// Basic functionality
/// Name
// Check Configurable
/// Extensions
/// Wiring to other containers
/// Lookup

// Missing
/// Attachments!!
/// Layers!!!

// Optional<Class<? extends AnyBundle>> bundleType();
// -- or Class<?> configuratorType() <- This is the class for which all access is checked relative to
/// Why shouldnt it be able to have @Install even if !A Bundle

// Environment <- Immutable??, Attachable??
// See #Extension Implementation notes for information about how to make sure it can be instantiated...
public interface ContainerConfiguration extends ComponentConfiguration<Object> {

    /**
     * Returns the build context. A single build context object is shared among all containers for the same artifact.
     * 
     * @return the build context
     */
    // ArtifactBuildContext buildContext();

    /**
     * Returns an unmodifiable set view of the extensions that are currently in use by the container.
     * 
     * @return an unmodifiable set view of the extensions that are currently in use by the container
     */
    Set<Class<? extends Extension>> extensions();

    /**
     * Returns whether or not this container is the top level container for an artifact.
     * 
     * @return whether or not this container is the top level container for an artifact
     */
    boolean isTopContainer();

    /**
     * Links the specified bundle.
     * 
     * @param bundle
     *            the bundle to link
     * @param wirelets
     *            any wirelets
     */
    void link(Bundle bundle, Wirelet... wirelets);

    /**
     * Registers a {@link Lookup} object that will be used for accessing fields and invoking methods on registered
     * components.
     * <p>
     * The lookup object passed to this method is never made available through the public API. Its use is strictly
     * internally.
     * <p>
     * This method allows passing null, which clears any lookup object that has previously been set.
     * 
     * @param lookup
     *            the lookup object
     */
    // If you are creating resulable stuff, you should remember to null the lookup object out.
    // So child modules do not have the power of the lookup object.
    void lookup(@Nullable Lookup lookup);

    /**
     * Creates a new layer.
     * 
     * @param name
     *            the name of layer
     * @param dependencies
     *            dependencies on other layers
     * @return the new layer
     */
    // Moved to ComponentExtension??? All the linkage und so weither is there...
    ContainerLayer newLayer(String name, ContainerLayer... dependencies);

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type is
     * requested. This method will create a new instance of the extension and return it for all subsequent calls to this
     * method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see Extension#use(Class)
     * @see ContainerConfiguration#extensions()
     */
    <T extends Extension> T use(Class<T> extensionType);
}
