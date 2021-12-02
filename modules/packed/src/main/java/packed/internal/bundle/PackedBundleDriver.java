package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.extension.Extension;
import packed.internal.component.ComponentSetup;
import packed.internal.util.LookupUtil;

/** A special component driver that create containers. */
public final class PackedBundleDriver extends ContainerDriver {

    /** A driver for configuring containers. */
    public static final PackedBundleDriver DRIVER = new PackedBundleDriver();

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            PackedBundleDriver.class);

    /** A handle that can access ComponentConfiguration#component. */
    private static final VarHandle VH_COMPONENT_CONFIGURATION_COMPONENT = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ComponentConfiguration.class, "component", ComponentSetup.class);

    @Override
    public Set<Class<? extends Extension>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    public final ContainerConfiguration toConfiguration(ComponentSetup cs) {
        ContainerConfiguration c = newConfiguration();
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, cs);
        return c;
    }
    
    
    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    public static PackedBundleDriver getDriver(Assembly assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedBundleDriver) VH_ASSEMBLY_DRIVER.get(assembly);
    }
}