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

import app.packed.app.App;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.util.Nullable;

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
public interface ContainerConfiguration extends ComponentConfiguration {

    /**
     * Returns the build context. A single build context object is shared among all containers that are connected using
     * {@link ComponentExtension#link(ContainerBundle, Wirelet...)}.
     * 
     * @return the build context
     */
    // Move to component???
    // It boils down to adding actors at runtime. How would that work
    ArtifactBuildContext buildContext();

    /**
     * Returns an immutable view of all of the extension types that are used by this configuration.
     * 
     * @return an immutable view of all of the extension types that are used by this configuration
     */
    Set<Class<? extends Extension>> extensions();

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
     * Returns an extension of the specified type. If this is the first time the extension is requested this method will
     * automatically instantiate an extension of the specified type and install it. Returning the instantiated extension for
     * all subsequent calls to this method with the specified type.
     * <p>
     * Ways for extensions to be installed
     * 
     * Extensions might use other extensions in which
     * 
     * Extension Method....
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if the configuration is no longer modifiable and an extension of the specified type has not already been
     *             installed
     */
    <T extends Extension> T use(Class<T> extensionType);

    /**
     * Returns a wirelet list containing any wirelets that was specified when creating this configuration. For example, via
     * {@link App#of(ArtifactSource, Wirelet...)} or {@link ComponentExtension#link(ContainerBundle, Wirelet...)}.
     * 
     * @return a wirelet list containing any wirelets that was specified when creating this configuration
     */
    WireletList wirelets();
}

/// **
// * Returns the configuration site of the container.
// *
// * @return the configuration site of the container
// */
// @Override
// ConfigSite configSite();
//
/// **
// * Returns the description of this container. Or null if the description has not been set.
// *
// * @return the description of this container. Or null if the description has not been set.
// * @see #setDescription(String)
// */
// @Override
// @Nullable
// String getDescription();
//
/// **
// * Returns the name of the container. If no name has previously been set via {@link #setName(String)} a name is
// * automatically generated by the runtime as outlined in {@link #setName(String)}.
// * <p>
// * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
// * being thrown.
// *
// * @return the name of the container
// * @see #setName(String)
// */
// @Override
// String getName();
//
/// **
// * Returns the component path of the container.
// * <p>
// * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
// * being thrown.
// *
// * @return the component path of the container
// */
// @Override
// ComponentPath path();
//
/// **
// * Sets the description of this container.
// *
// * @param description
// * the description to set
// * @see #getDescription()
// * @throws IllegalStateException
// * if this configuration can no longer be configured
// * @return this configuration
// */
// @Override
// ContainerConfiguration setDescription(String description);
//
/// **
// * Sets the name of the container. The name must consists only of alphanumeric characters and '_', '-' or '.'. The
// * string may end with a '?' indicating that the runtime might place the '?' with a runtime chosen post-fix string in
// * case other components or containers have the same name. The name is case sensitive.
// * <p>
// * This method must be invoked before trying to install new components in the container, linking new containers or
// * calling {@link #getName()}.
// * <p>
// * If no name is set using this method. A name will be automatically be assigned, in such a way that it will have a
// * unique name among other sibling container.
// *
// * @param name
// * the name of the container
// * @throws IllegalArgumentException
// * if the specified name is the empty string, or if the name contains other characters then alphanumeric
// * characters and '_', '-' or '.', or contains a '?' at any position then the last position of the string.
// * @throws IllegalStateException
// * if invoked after any components have been installed, containers linked or {@link #getName()} invoked.
// * @see #getName()
// * @return this configuration
// */
// @Override
// ContainerConfiguration setName(String name);
