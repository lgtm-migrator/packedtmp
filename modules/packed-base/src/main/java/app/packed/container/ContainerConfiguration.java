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
import java.util.List;
import java.util.Set;

import app.packed.config.ConfigSite;
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

/// Components?? Or is this an extension.... It would be nice if you could install components from other extensions....
/// Which you cannot currently, as you cannot inject ContainerConfiguration... And what about attachments????
/// Maybe directly on the extension.. containerAttachements().. maybe also use?? Then we can always lazy initialize....
/// And we do not need the fields

// Optional<Class<? extends AnyBundle>> bundleType();
// -- or Class<?> configuratorType() <- This is the class for which all access is checked relative to
/// Why shouldnt it be able to have @Install even if !A Bundle

// Environment <- Immutable??, Attachable??
// See #Extension Implementation notes for information about how to make sure it can be instantiated...
public interface ContainerConfiguration {

    void checkConfigurable();

    /**
     * Returns the configuration site of the container.
     * 
     * @return the configuration site of the container
     */
    ConfigSite configurationSite();

    /**
     * Returns an immutable view of all of the extension types that are used by this container.
     * 
     * @return an immutable view of all of the extension types that are used by this container
     */
    Set<Class<? extends Extension<?>>> extensions();

    /**
     * Returns the description of this container. Or null if the description has not been set.
     *
     * @return the description of this container. Or null if the description has not been set.
     * @see #setDescription(String)
     * @see Container#description()
     */
    @Nullable
    String getDescription();

    /**
     * Returns the name of the container or null if the name has not been set.
     *
     * @return the name of the container or null if the name has not been set
     * @see #setName(String)
     */
    @Nullable
    String getName();

    /**
     * Creates a link to another bundle.
     * <p>
     * All links made using this method between two bundles are permanent. If you need dynamic stuff you can use hosts and
     * applications.
     * 
     * @param child
     *            the child bundle
     * @param wirelets
     *            optional wiring options
     * @return a bundle link
     */
    <T extends AnyBundle> T link(T child, Wirelet... wirelets);

    /**
     * Registers a {@link Lookup} object that will be used for accessing fields and invoking methods on registered
     * components.
     * <p>
     * The lookup object passed to this method is never made available through the public API. Its use is strictly
     * internally.
     * 
     * @param lookup
     *            the lookup object
     */
    void lookup(@Nullable Lookup lookup);

    /**
     * Sets the description of this container.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @see #getDescription()
     * @see Container#description()
     * @throws IllegalStateException
     *             if this configuration can no longer be configured
     */
    ContainerConfiguration setDescription(@Nullable String description);

    /**
     * Sets the {@link Container#name() name} of the container. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The string may end with a '?' indicating that the runtime might place the '?' with a runtime chosen
     * post-fix string in case other components or containers have the same name. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the container when the container is initialized, in
     * such a way that it will have a unique name among other sibling container.
     *
     * @param name
     *            the name of the container
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.', or contains a '?' at any position then the last position of the string.
     * @see #getName()
     */
    void setName(@Nullable String name);

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
     */
    <T extends Extension<T>> T use(Class<T> extensionType);

    /**
     * Returns a list of any wirelets that was used to create this configuration. For example, via
     * {@link App#of(AnyBundle, Wirelet...)}.
     * 
     * @return a list of any wirelets that was used to create this configuration
     */
    List<Wirelet> wirelets();
}
