package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.application.Application;
import app.packed.application.ApplicationWirelets;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.Wirelet;
import app.packed.container.Container;
import app.packed.state.RunState;
import packed.internal.component.ComponentSetup;
import packed.internal.component.InternalWirelet;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireableComponentDriver.ContainerComponentDriver;
import packed.internal.component.source.SourceComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.invoke.constantpool.ConstantPoolSetup;

/** Build-time configuration for an application. */
public final class ApplicationSetup {

    /** The configuration of the main constant build. */
    public final ConstantPoolSetup constantPool = new ConstantPoolSetup();

    /** The root container of the application. */
    public final ContainerSetup container;

    /** The driver of the applications. */
    public final PackedApplicationDriver<?> driver;

    /** The launch mode of the application, may be updated via usage of {@link ApplicationWirelets#launchMode(RunState)}. */
    RunState launchMode;

    @Nullable
    private MainThreadOfControl mainThread;

    private final int modifiers;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(BuildSetup build, PackedApplicationDriver<?> driver, RealmSetup realm, ContainerComponentDriver containerDriver, int modifiers,
            Wirelet[] wirelets) {
        this.driver = requireNonNull(driver, "driver is null");
        this.container = containerDriver.newComponent(build, this, realm, null, wirelets);
        this.modifiers = modifiers;
    }

    /** {@return returns an Application adaptor that can be exposed to end-users} */
    public Application adaptor() {
        return new Adaptor(this);
    }

    public boolean hasMain() {
        return mainThread != null;
    }

    public MainThreadOfControl mainThread() {
        MainThreadOfControl m = mainThread;
        if (m == null) {
            m = mainThread = new MainThreadOfControl();
        }
        return m;
    }

    /** {@return whether or not the application is part of an image}. */
    public boolean isImage() {
        return PackedComponentModifierSet.isImage(modifiers);
    }

    /** A wirelet that will set the name of the component. Used by {@link Wirelet#named(String)}. */
    public static final class ApplicationLaunchModeWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final RunState launchMode;

        /**
         * Creates a new name wirelet
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public ApplicationLaunchModeWirelet(RunState launchMode) {
            this.launchMode = requireNonNull(launchMode, "launchMode is null");
            if (launchMode == RunState.INITIALIZING) {
                throw new IllegalArgumentException(RunState.INITIALIZING + " is not a valid launch mode");
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ComponentSetup c) {
            checkApplication(c).launchMode = launchMode;
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ComponentSetup c, ApplicationLaunchContext ic) {
            ic.launchMode = launchMode;
        }
    }

    public class MainThreadOfControl {
        public SourceComponentSetup cs;

        public boolean isStatic;

        public MethodHandle methodHandle;

        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }
    }

    /** An adaptor of {@link ApplicationSetup} exposed as {@link Application}. */
    private record Adaptor(ApplicationSetup application) implements Application {

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return application.container.adaptor();
        }

        /** {@inheritDoc} */
        @Override
        public Container container() {
            return application.container.containerAdaptor();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return application.container.name;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStronglyWired() {
            throw new UnsupportedOperationException();
        }
    }
}
