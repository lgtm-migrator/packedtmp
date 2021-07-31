package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationRuntimeWirelets;
import app.packed.application.host.ApplicationHostMirror;
import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.state.sandbox.InstanceState;
import packed.internal.component.RealmSetup;
import packed.internal.component.bean.BeanSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.InternalWirelet;
import packed.internal.container.PackedContainerDriver;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolAccessor;

/** Build-time configuration of an application. */
public final class ApplicationSetup {

    public final PackedApplicationDriver<?> applicationDriver;

    public final BuildSetup build;

    /** The root container of the application. */
    public final ContainerSetup container;

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    boolean isImage;

    /**
     * The launch mode of the application. May be updated via usage of
     * {@link ApplicationRuntimeWirelets#launchMode(InstanceState)} at build-time. If used from an image
     * {@link ApplicationLaunchContext#launchMode} is updated instead.
     */
    InstanceState launchMode;

    // sync entrypoint
    @Nullable
    private MainThreadOfControl mainThread;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    final PoolAccessor runtimeAccessor;

    public final boolean hasRuntime;

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    ApplicationSetup(BuildSetup build, RealmSetup realm, PackedApplicationDriver<?> driver, Wirelet[] wirelets) {
        this.build = build;
        this.applicationDriver = driver;
        this.launchMode = requireNonNull(driver.launchMode());

        this.hasRuntime = driver.hasRuntime();
        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        container = new ContainerSetup(this, realm, new LifetimeSetup(null), /* fixme */ PackedContainerDriver.DRIVER, null, wirelets);
        this.runtimeAccessor = driver.hasRuntime() ? container.lifetime.pool.reserve(PackedApplicationRuntimeExtensor.class) : null;
    }

    /** {@return an application adaptor that can be exposed to end-users} */
    public BuildTimeApplicationMirror applicationMirror() {
        return new BuildTimeApplicationMirror();
    }

    public boolean hasMain() {
        return mainThread != null;
    }

    /** {@return whether or not the application is part of an image}. */
    public boolean isImage() {
        return isImage;
    }

    public MainThreadOfControl mainThread() {
        MainThreadOfControl m = mainThread;
        if (m == null) {
            m = mainThread = new MainThreadOfControl();
        }
        return m;
    }

    /**
     * A wirelet that will set the launch mode of the application. Used by
     * {@link ApplicationRuntimeWirelets#launchMode(InstanceState)}.
     */
    public static final class ApplicationLaunchModeWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final InstanceState launchMode;

        /**
         * Creates a new name wirelet
         * 
         * @param launchMode
         *            the new launch mode of the application
         */
        public ApplicationLaunchModeWirelet(InstanceState launchMode) {
            this.launchMode = requireNonNull(launchMode, "launchMode is null");
            if (launchMode == InstanceState.UNINITIALIZED) {
                throw new IllegalArgumentException(InstanceState.UNINITIALIZED + " is not a valid launch mode");
            }
        }

        @Override
        protected <T> PackedApplicationDriver<T> onApplicationDriver(PackedApplicationDriver<T> driver) {
            if (driver.launchMode() == launchMode) {
                return driver;
            }
            return super.onApplicationDriver(driver);
        }

        /** {@inheritDoc} */
        @Override
        protected void onBuild(ContainerSetup component) {
            // TODO we probably need to check that it is launchable
            checkIsApplication(component).launchMode = launchMode; // override any existing launch mode
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ContainerSetup component, ApplicationLaunchContext launch) {
            // TODO we probably need to check that it is launchable
            launch.launchMode = launchMode;
        }
    }

    /** An application mirror adaptor. */
    private final class BuildTimeApplicationMirror implements ApplicationMirror {

        /** {@inheritDoc} */
        @Override
        public ComponentMirror component(CharSequence path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ContainerMirror container() {
            return ApplicationSetup.this.container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension>> disabledExtensions() {
            // TODO add additional dsiabled extensions
            return ApplicationSetup.this.applicationDriver.bannedExtensions();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasRuntime() {
            return ApplicationSetup.this.hasRuntime;
        }

        @Override
        public Optional<ApplicationHostMirror> host() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStronglyWired() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Module module() {
            return ApplicationSetup.this.container.realm.realmType().getModule();
        }
    }

    public class MainThreadOfControl {
        public BeanSetup cs;

        public boolean isStatic;

        public MethodHandle methodHandle;

        public boolean hasExecutionBlock() {
            return methodHandle != null;
        }
    }
}
