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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.base.Nullable;
import app.packed.container.Wirelet;
import app.packed.lifetime.sandbox.OldLifetimeKind;
import internal.app.packed.container.AssemblySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.pool.Accessor.DynamicAccessor;
import internal.app.packed.lifetime.sandbox.PackedManagedLifetime;
import internal.app.packed.oldservice.inject.ApplicationInjectionManager;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Internal configuration of an application. */
public final class ApplicationSetup {

    /** A MethodHandle for invoking {@link ApplicationMirror#initialize(ApplicationSetup)}. */
    private static final MethodHandle MH_APPLICATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationMirror.class,
            "initialize", void.class, ApplicationSetup.class);

    /** The root container of the application (created in the constructor of this class). */
    public final ContainerSetup container;

    /** The driver responsible for building the application. */
    public final PackedApplicationDriver<?> driver;

    /** Entry points in the application, is null if there are none. */
    @Nullable
    public EntryPointSetup entryPoints;

    public final BuildGoal goal;

    /** The tree this service manager is a part of. */
    public final ApplicationInjectionManager injectionManager = new ApplicationInjectionManager();

    @Nullable
    PackedApplicationLauncher launcher;

    /** The index of the application's runtime in the constant pool, or -1 if the application has no runtime, */
    @Nullable
    final DynamicAccessor runtimeAccessor;

    final ArrayList<Runnable> codegenActions = new ArrayList<>();

    /**
     * Create a new application setup
     * 
     * @param driver
     *            the application's driver
     */
    public ApplicationSetup(PackedApplicationDriver<?> driver, BuildGoal goal, AssemblySetup realm, Wirelet[] wirelets) {
        this.driver = driver;
        this.goal = requireNonNull(goal);

        // Create the root container of the application
        this.container = new ContainerSetup(this, realm, null, wirelets);

        // If the application has a runtime (PackedApplicationRuntime) we need to reserve a place for it in the application's
        // constant pool
        this.runtimeAccessor = driver.lifetimeKind() == OldLifetimeKind.MANAGED ? container.lifetime.pool.reserve(PackedManagedLifetime.class) : null;
    }

    private boolean isCodegen;

    /**
     * 
     */
    public void close() {

        // For each lifetime create lifetime operations
        //// And install them in the lifetime

        // Services?
        // get() -> MH.invokeExact(ExtensionContext);
        // ServiceManager

        // ServiceLocator Key -> MethodHandle

        // Packed relies on lazily created codegeneration

        
        isCodegen = true;
        if (goal.isLaunchable()) {

            // If this fails it is always a bug in either Packed or one of its extensions.
            // It is never a user error.
            for (Runnable r : codegenActions) {
                r.run();
            }
        }
        launcher = new PackedApplicationLauncher(this);
    }

    public void addCodegenAction(Runnable runnable) {
        if (isCodegen) {
            throw new IllegalStateException();
        }
    }

    public void checkCodegen() {
        if (!isCodegen) {
            throw new IllegalStateException();
        }
    }

    /** {@return a mirror that can be exposed to end-users.} */
    public ApplicationMirror mirror() {
        ApplicationMirror mirror = ClassUtil.mirrorHelper(ApplicationMirror.class, ApplicationMirror::new, driver.mirrorSupplier);

        // Initialize ApplicationMirror by calling ApplicationMirror#initialize(ApplicationSetup)
        try {
            MH_APPLICATION_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }
}
