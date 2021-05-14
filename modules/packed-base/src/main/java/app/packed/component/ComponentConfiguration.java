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

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.attribute.Attribute;
import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.inject.sandbox.ExportedServiceConfiguration;

/**
 * The base class for component configuration classes.
 * <p>
 * The class is basically a thin wrapper on top of {@link ComponentConfigurationContext}. All component configuration
 * classes must extend, directly or indirectly, from this class.
 * <p>
 * Instead of extending this class directly, you typically want to extend {@link BaseComponentConfiguration} instead.
 */
public abstract class ComponentConfiguration {

    /** The configuration context of the component. */
    protected final ComponentConfigurationContext context;

    /**
     * Create a new component configuration.
     * 
     * @param context
     *            the configuration context
     */
    protected ComponentConfiguration(ComponentConfigurationContext context) {
        this.context = requireNonNull(context, "context is null");
    }

    protected void onInitialized() {}
    
    /**
     * Ivoked A callback method invoked by Packed immediatly before it is marked as no longer configurable
     */
    protected void onConfigured() {}
    
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

        void checkIsWiring();

        /**
         * @param assembly
         *            the assembly
         * @param wirelets
         *            optional wirelets
         * @return a descriptor for the component that was linked
         */
        ComponentMirror link(Assembly<?> assembly, Wirelet... wirelets);

        /** { @return a model view for the underlying component} */
        ComponentMirror mirror();

        /**
         * Sets the {@link ComponentMirror#name() name} of the component. The name must consists only of alphanumeric characters and
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
         * @see ComponentMirror#name()
         */
        void named(String name);

        /**
         * Returns the full path of the component.
         * <p>
         * Once this method has been invoked, the name of the component can no longer be changed via {@link #named(String)}.
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

        <T> void setRuntimeAttribute(Attribute<T> attribute, T value);

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

        default <T extends Wirelet> SelectWirelets<T> selectWirelets(Class<T> wirelet) {
            throw new UnsupportedOperationException();
        }
    }
}
// I don't expect this class to have any $ methods
// They should most likely be located in the driver instead
// A component configuration is just a thin wrapper

// Nice man kan faktisk lave en Assembly tager en component configuration med package private metoder
// Man saa kan expose... Men uden at expose nogle metoder paa selve configurations objektet...