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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.container.AnyBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.InstantiationContext;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.PackedContainerConfiguration.NameWirelet;

/** An abstract base class for a component configuration object. */
abstract class AbstractComponentConfiguration implements ComponentHolder {

    /** The children of this component (lazily initialized), in order of insertion. */
    @Nullable
    LinkedHashMap<String, AbstractComponentConfiguration> children;

    @Nullable
    DefaultComponentConfiguration currentComponent;

    /** The depth of the component in the hierarchy. */
    private final int depth;

    /** The description of the component. */
    @Nullable
    private String description;

    /** The name of the component. */
    @Nullable
    String name;

    /** The parent of this component, or null if a root component. */
    @Nullable
    private final AbstractComponentConfiguration parent;

    /** The configuration site of this component. */
    private final InternalConfigSite site;

    /** The state of this configuration. */
    State state = State.INITIAL;

    /** Whether or not the name can be postfix'able. Useful for images only */
    boolean isNamePostfixable = false;

    /**
     * Creates a new abstract component configuration
     * 
     * @param site
     *            the configuration site of the component
     * @param parent
     *            the parent of the component, or null if the component is a root component
     */
    AbstractComponentConfiguration(InternalConfigSite site, @Nullable AbstractComponentConfiguration parent) {
        this.site = requireNonNull(site);
        this.parent = parent;
        this.depth = parent == null ? 0 : parent.depth() + 1;
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return depth;
    }

    Map<String, AbstractComponent> initializeChildren(AbstractComponent parent, InstantiationContext ic) {
        if (children == null) {
            return null;
        }
        HashMap<String, AbstractComponent> result = new HashMap<>();
        for (AbstractComponentConfiguration acc : children.values()) {
            AbstractComponent ac = acc.instantiate(parent, ic);
            result.put(ac.name(), ac);
        }
        return result;
    }

    void addChild(AbstractComponentConfiguration configuration) {
        if (children == null) {
            children = new LinkedHashMap<>();
        }
        requireNonNull(configuration.name);
        children.put(configuration.name, configuration);
    }

    public final void checkConfigurable() {
        if (state == State.FINAL) {
            throw new IllegalStateException();
        }
    }

    public final InternalConfigSite configSite() {
        return site;
    }

    @Nullable
    public final String getDescription() {
        return description;
    }

    public final String getName() {
        return initializeName(State.GET_NAME_INVOKED, null);
    }

    protected String initializeName(State state, String setName) {
        String n = name;
        if (n != null) {
            return n;
        }
        n = setName;
        if (this instanceof PackedContainerConfiguration) {
            Optional<NameWirelet> o = ((PackedContainerConfiguration) this).wirelets().last(NameWirelet.class);
            if (o.isPresent()) {
                n = o.get().name;
            }
        }

        boolean isFree = false;
        if (n == null) {
            n = initializeNameDefaultName();
            isFree = true;
        } else if (n.endsWith("?")) {
            n = n.substring(0, n.length() - 1);
            isFree = true;
        }

        if (parent != null && parent.children != null && parent.children.containsKey(n)) {
            if (!isFree) {
                throw new RuntimeException("Name already exist " + n);
            }
            int counter = 1;
            String prefix = n;
            do {
                n = prefix + counter++;
            } while (parent.children.containsKey(n));
        }
        this.state = state;
        return this.name = n;
    }

    private String initializeNameDefaultName() {
        if (this instanceof PackedContainerConfiguration) {
            @Nullable
            AnyBundle bundle = (@Nullable AnyBundle) ((PackedContainerConfiguration) this).source.source;
            if (bundle != null) {
                String nnn = bundle.getClass().getSimpleName();
                if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                    nnn = nnn.substring(0, nnn.length() - 6);
                }
                if (nnn.length() > 0) {
                    // checkName, if not just App
                    // TODO need prefix
                    return nnn;
                }
                if (nnn.length() == 0) {
                    return "Container";
                }
            }
            return "Unknown";
        } else {
            return ((DefaultComponentConfiguration) this).ccd.defaultPrefix();
        }

    }

    abstract AbstractComponent instantiate(AbstractComponent parent, InstantiationContext ic);

    /**
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    public final ComponentPath path() {
        initializeName(State.PATH_INVOKED, null);
        switch (depth) {
        case 0:
            return ComponentPath.ROOT;
        case 1:
            return new PackedComponentPath(name);
        default:
            String[] paths = new String[depth];
            AbstractComponentConfiguration acc = this;
            for (int i = depth - 1; i >= 0; i--) {
                paths[i] = acc.name;
                acc = acc.parent;
            }
            return new PackedComponentPath(paths);
        }
    }

    public AbstractComponentConfiguration setDescription(String description) {
        requireNonNull(description, "description is null");
        checkConfigurable();
        this.description = description;
        return this;
    }

    public AbstractComponentConfiguration setName(String name) {
        checkName(name);
        switch (state) {
        case INITIAL:
            initializeName(State.SET_NAME_INVOKED, name);
            return this;
        case FINAL:
            checkConfigurable();
        case GET_NAME_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via call to #getName()");
        case PATH_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via call to #path()");
        case INSTALL_INVOKED:
            throw new IllegalStateException("Cannot call this method after installing new components in the container");
        case LINK_INVOKED:
            throw new IllegalStateException("Cannot call this method after containerConfiguration.link has been invoked");
        case SET_NAME_INVOKED:
            throw new IllegalStateException("#setName(String) can only be called once");
        }
        throw new InternalError();
    }

    /**
     * Checks the name of the component.
     * 
     * @param name
     *            the name to check
     * @return the name if valid
     */
    static String checkName(String name) {
        requireNonNull(name, "name is null");
        if (name != null) {

        }
        return name;
    }

    enum State {

        /** */
        FINAL,

        /** {@link ComponentConfiguration#getName()} or {@link ContainerConfiguration#getName()} has been invoked. */
        GET_NAME_INVOKED,

        /** The initial state. */
        INITIAL,

        /** One of the install component methods has been invoked. */
        INSTALL_INVOKED,

        /** {@link ContainerConfiguration#link(AnyBundle, app.packed.container.Wirelet...)} has been invoked. */
        LINK_INVOKED,

        /** One of the install component methods has been invoked. */
        PATH_INVOKED,

        /** Set name has been invoked. */
        SET_NAME_INVOKED;
    }
}
