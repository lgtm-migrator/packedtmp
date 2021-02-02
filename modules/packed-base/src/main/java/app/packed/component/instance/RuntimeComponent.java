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
package app.packed.component.instance;

import java.util.Collection;

import app.packed.attribute.AttributedElement;
import app.packed.base.NamespacePath;
import app.packed.component.Component;
import app.packed.state.RunState;

/**
 *
 */
// Tror bedre man kan snakke om component instances

// Hvorfor runtime component

// Vi har et kaempe trae. Men vi instantiater kun noget af det

// Syntes instance er et daarligt signal at sende... naar man f.eks.
// kun har nogle lambdas... eller statiske componneter
public interface RuntimeComponent extends AttributedElement {

    // ComponentDefinition...
    /**
     * Returns the component that is behind this instance.
     * 
     * @return the component that is behind this instance
     */
    Component component();

    /**
     * Returns an unmodifiable view of all of this component's children.
     *
     * @return an unmodifiable view of all of this component's children
     */
    Collection<RuntimeComponent> children();

    /**
     * Returns the distance to the root component. The root component having depth 0.
     * 
     * @return the distance to the root component
     */
    // Maybe just a method on path().depth();
    int depth();

    /**
     * Returns the name of this component.
     * <p>
     * If no name is explicitly set by the user when configuring the component. The runtime will automatically generate a
     * name that is unique among other components with the same parent.
     *
     * @return the name of this component
     */
    String name();

    /**
     * Returns the path of this component instance.
     *
     * @return the path of this component instance
     */
    NamespacePath path();


}

interface ZInstance {

    // Restart, reInitialize...
    // Not sure we should capture state like that
    // Also, fx syntes maaske ikke det giver mening at en
    // function har en state...
    RunState runState();
    
    Object sourceInstances();
}

// Runlet