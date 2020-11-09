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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.component.Component;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import app.packed.container.Container;
import app.packed.inject.ServiceLocator;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.inject.service.ServiceComposer;
import packed.internal.util.LookupUtil;

/**
 * An instantiation context is created every time an artifact is being instantiated.
 * <p>
 * Describes which phases it is available from
 * <p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
// Ideen er vi skal bruge den til at registrere fejl...

// MethodHandle stableAccess(Object[] array) <-- returns 
public final class PackedInitializationContext {

    /** A MethodHandle for invoking {@link #component()}. */
    public static final MethodHandle MH_COMPONENT = LookupUtil.lookupVirtual(MethodHandles.lookup(), "component", Component.class);

    /** A MethodHandle for invoking {@link #container()}. */
    public static final MethodHandle MH_CONTAINER = LookupUtil.lookupVirtual(MethodHandles.lookup(), "container", Container.class);

    /** A MethodHandle for invoking {@link #services()}. */
    public static final MethodHandle MH_SERVICES = LookupUtil.lookupVirtual(MethodHandles.lookup(), "services", ServiceLocator.class);

    /** The component node we are building. */
    PackedComponent component;

    final ComponentBuild root;

    private final WireletPack wirelets;

    private PackedInitializationContext(ComponentBuild root, WireletPack wirelets) {
        this.root = root;
        this.wirelets = wirelets;
    }

    /**
     * Returns the top component.
     * 
     * @return the top component
     */
    public Component component() {
        return component;
    }

    public Container container() {
        if (component.hasModifier(ComponentModifier.CONTAINER)) {
            return component.region.container();
        }
        throw new UnsupportedOperationException("This component does not have a container");
    }

    // Initialize name, we don't want to override this in Configuration context. We don't want the conf to change if
    // image...
    // Check for any runtime wirelets that have been specified.
    // This is probably not the right way to do it. Especially with hosts.. Fix it when we get to hosts...
    // Maybe this can be written in PodInstantiationContext
    String rootName(ComponentBuild configuration) {
        String n = configuration.name;
        String ol = wirelets() == null ? null : wirelets().nameWirelet();
        if (ol != null) {
            n = ol;
            if (n.endsWith("?")) {
                n = n.substring(0, n.length() - 1);
            }
        }
        return n;
    }

    /**
     * Returns a service locator for the system. If the service extension is not installed, returns
     * {@link ServiceLocator#of()}.
     * 
     * @return a service locator for the system
     */
    public ServiceLocator services() {
        ServiceComposer sc = root.cube.getServiceManager();
        return sc == null ? ServiceLocator.of() : sc.newServiceLocator(component, component.region);
    }

    /**
     * Returns a list of wirelets that used to instantiate. This may include wirelets that are not present at build time if
     * using an image.
     * 
     * @return a list of wirelets that used to instantiate
     */
    public WireletPack wirelets() {
        return wirelets;
    }

    public static PackedInitializationContext process(ComponentBuild root, Wirelet[] imageWirelets) {
        PackedInitializationContext pic = new PackedInitializationContext(root,
                root.build.isImage() ? WireletPack.forImage(root, imageWirelets) : root.wirelets);

        // Instantiates the whole component tree (well @Initialize does not yet work)
        // pic.component is set from PackedComponent
        new PackedComponent(null, root, pic);

        // TODO initialize

        if (root.modifiers().isContainer()) {
            pic.component.region.container().onInitialized(root, pic);
        }
        return pic; // don't know do we want to gc PIC at fast as possible
    }
}
