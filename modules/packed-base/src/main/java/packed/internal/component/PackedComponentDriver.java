package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfiguration.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** The abstract base class for component drivers */
public abstract class PackedComponentDriver<C extends ComponentConfiguration> implements ComponentDriver<C> {

    /** A handle that can access Assembly#driver. */
    private static final VarHandle VH_ASSEMBLY_DRIVER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), Assembly.class, "driver",
            PackedComponentDriver.class);

    public final int modifiers;

    /** Optional wirelets that will be applied to any component created by this driver. */
    @Nullable
    final Wirelet wirelet;

    public PackedComponentDriver(@Nullable Wirelet wirelet, int modifiers) {
        this.wirelet = wirelet;
        this.modifiers = modifiers;
    }

    public abstract ComponentSetup newComponent(ApplicationSetup application, RealmSetup realm, @Nullable ComponentSetup parent, Wirelet[] wirelets);

    public abstract C toConfiguration(ComponentConfigurationContext context);

    /** {@inheritDoc} */
    @Override
    public ComponentDriver<C> with(Wirelet... wirelets) {
        // Vi kan faktisks godt lave nogle checks allerede her
        // Application Wirelets kan f.eks. kun bindes til en ContainerComponentDriver.
        Wirelet w = wirelet == null ? Wirelet.combine(wirelets) : wirelet.andThen(wirelets);
        return withWirelet(w);
    }

    protected abstract ComponentDriver<C> withWirelet(Wirelet w);

    /**
     * Extracts the component driver from the specified assembly.
     * 
     * @param assembly
     *            the assembly to extract the component driver from
     * @return the component driver of the specified assembly
     */
    public static PackedComponentDriver<?> getDriver(Assembly<?> assembly) {
        requireNonNull(assembly, "assembly is null");
        return (PackedComponentDriver<?>) VH_ASSEMBLY_DRIVER.get(assembly);
    }

    public static class BoundClassComponentDriver<C extends ComponentConfiguration> extends PackedComponentDriver<C> {

        public final Object binding;

        final MethodHandle mh;

        public BoundClassComponentDriver(PackedClassComponentBinder<?, C> driver, Object binding) {
            super(null, driver.modifiers());
            this.mh = driver.constructor();
            this.binding = requireNonNull(binding);
        }

        @Override
        public ComponentSetup newComponent(ApplicationSetup application, RealmSetup realm, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
            requireNonNull(parent);
            return new SourcedComponentSetup(application, realm, this, parent, wirelets);
        }

        @Override
        public C toConfiguration(ComponentConfigurationContext context) {
            // Vil godt lave den om til CNC (Hvad det end betyder). Maaske at vi gerne vil bruge invokeExact
            try {
                return (C) mh.invoke(context);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected ComponentDriver<C> withWirelet(Wirelet w) {
            throw new UnsupportedOperationException();
        }
    }

    /** A special component driver that create containers. */
    // Leger med tanken om at lave en specifik public interface container driver
    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        public ContainerComponentDriver(Wirelet wirelet) {
            super(wirelet, PackedComponentModifierSet.I_CONTAINER);
        }

        /** {@inheritDoc} */
        public ContainerSetup newComponent(ApplicationSetup application, RealmSetup realm, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
            return new ContainerSetup(application, realm, this, parent, wirelets);
        }

        @Override
        public ContainerConfiguration toConfiguration(ComponentConfigurationContext context) {
            return new ContainerConfiguration(context);
        }

        @Override
        protected ContainerComponentDriver withWirelet(Wirelet w) {
            return new ContainerComponentDriver(w);
        }
    }

}
