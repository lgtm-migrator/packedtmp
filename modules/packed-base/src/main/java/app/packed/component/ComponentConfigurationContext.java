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
package app.packed.component;

import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.sandbox.ExportedServiceConfiguration;

/**
 * Component configuration context objects used component configuration classes.
 * <p>
 * This class mainly exists to allow people to create their own configuration classes.
 */

// ComponentComposer

//Component component(); seeThrough???
//If we have that we can also navigate?? I guess thats okay

// Has a lot of options..
// prefixed with container() <--- throws UOE if no Container tag
// prefixed with source() <--- throws UOE if no source tag
// prefixed with guest() <--- throws UOE if no guest tag

// component Name();
// componentPath();

// sourceExport();
// sourceExportAs();
// sourceProvide();
// sourceProvideAs();
public /* sealed */ interface ComponentConfigurationContext {
    // Hmmmmmm, build() is normally something else
    BuildInfo build();

    /**
     * Checks that the component is still configurable. Throwing an {@link IllegalStateException} if it is not.
     * <p>
     * A component is typically only configurable inside of {@link Assembly#build()}.
     * 
     * @throws IllegalStateException
     *             if the component is no longer configurable.
     */
    void checkConfigurable();

    /**
     * Returns an unmodifiable view of the extensions that are currently in use.
     * 
     * @return an unmodifiable view of the extensions that are currently in use
     * 
     * @see ContainerAssembly#extensions()
     */
    // Maybe it is just an Attribute.. component.with(Extension.USED_EXTENSIONS)
    // for bundle components. Makes sense because we would need for interating
    // through the build
    Set<Class<? extends Extension>> containerExtensions();

    /**
     * @param <T>
     * @param extensionClass
     * @return the extension
     * @throws UnsupportedOperationException
     *             if the component does not have the {@link ComponentModifier#CONTAINER} modifier
     * @see ContainerConfiguration#use(Class)
     */
    <T extends Extension> T containerUse(Class<T> extensionClass);

    /**
     * Returns the name of the component. If no name has previously been set via {@link #setName(String)} a name is
     * automatically generated by the runtime as outlined in {@link #setName(String)}.
     * <p>
     * Trying to call {@link #setName(String)} after invoking this method will result in an {@link IllegalStateException}
     * being thrown.
     * 
     * @return the name of the component
     * @see #setName(String)
     */
    // Ditch this one?? Maybe have it via asComponent();
    String getName();

    /**
     * @param bundle
     *            the bundle
     * @param wirelets
     *            wirelets
     * 
     * @apiNote Previously this method returned the specified bundle. However, to encourage people to configure the bundle
     *          before calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have
     *          void return type.
     */
    void link(Assembly<?> bundle, Wirelet... wirelets);

    /**
     * Returns an immutable set containing all the modifiers of this component.
     * 
     * @return a immutable set containing all the modifiers of this component
     */
    ComponentModifierSet modifiers();

    /**
     * Returns the full path of the component.
     * <p>
     * Once this method has been invoked, the name of the component can no longer be changed via {@link #setName(String)}.
     * <p>
     * If building an image, the path of the instantiated component might be prefixed with another path.
     * 
     * <p>
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    NamespacePath path();

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the component when the component is initialized, in
     * such a way that it will have a unique name other sibling components.
     *
     * @param name
     *            the name of the component
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Component#name()
     */
    void setName(String name);

    <T> ExportedServiceConfiguration<T> sourceExport();

    void sourceProvide();

    void sourceProvideAs(Key<?> key);

    Optional<Key<?>> sourceProvideAsKey();

    /**
     * Wires a new child component using the specified driver
     * 
     * @param <C>
     *            the type of configuration returned by the driver
     * @param driver
     *            the driver to use for creating the component
     * @param wirelets
     *            any wirelets that should be used when creating the component
     * @return a configuration for the component
     */
    <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets);

    default <T extends Wirelet> WireletHandle<T> wirelets(Class<T> wirelet) {
        throw new UnsupportedOperationException();
    }
}
//
///**
//* Returns the configuration site that created this configuration.
//* 
//* @return the configuration site that created this configuration
//*/
//ConfigSite configSite();