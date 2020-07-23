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
package packed.internal.moduleaccess;

import java.util.concurrent.ConcurrentHashMap;

import app.packed.base.TypeLiteral;
import app.packed.hook.AnnotatedFieldHook;

/** A mechanism for calling package private methods in public packages without using reflection. */
// Rename to ModuleAccess.
public final class ModuleAccess {

    /** All secrets, we never remove them to make sure we never add anything twice. */
    private final static ConcurrentHashMap<Class<? extends SecretAccess>, SecretAccess> TMP = new ConcurrentHashMap<>();

    /** Never instantiate. */
    private ModuleAccess() {}

//    /**
//     * Returns an access object that can access methods in app.packed.artifact.
//     * 
//     * @return an access object that can access methods in app.packed.artifact
//     */
//    public static AppPackedArtifactAccess artifact() {
//        return ArtifactSingletonHolder.INSTANCE;
//    }

//    /**
//     * Returns an access object for app.packed.container.
//     * 
//     * @return an access object for app.packed.container
//     */
//    public static AppPackedContainerAccess container() {
//        return ContainerSingletonHolder.INSTANCE;
//    }

    /**
     * Returns an access object for app.packed.container.
     * 
     * @return an access object for app.packed.container
     */
    public static AppPackedHookAccess hook() {
        return HookSingletonHolder.INSTANCE;
    }

    /**
     * Initializes an access object.
     * 
     * @param <T>
     *            the type of access
     * @param accessType
     *            the type of access
     * @param access
     *            the access object
     */
    public static <T extends SecretAccess> void initialize(Class<T> accessType, T access) {
        // check instanceof
        // check same module
        // access.getInterface()
        if (TMP.putIfAbsent(accessType, access) != null) {
            throw new ExceptionInInitializerError("An instance of " + accessType + " has already been set ");
        }
    }

//    /**
//     * Returns an access object for app.packed.lifecycle.
//     * 
//     * @return an access object for app.packed.lifecycle
//     */
//    public static AppPackedLifecycleAccess lifecycle() {
//        return LifecycleSingletonHolder.INSTANCE;
//    }

//    /**
//     * Returns an access object for app.packed.service.
//     * 
//     * @return an access object for app.packed.service
//     */
//    public static AppPackedServiceAccess service() {
//        return ServiceSingletonHolder.INSTANCE;
//    }

//    public static AppPackedInjectAccess inject() {
//        return InjectSingletonHolder.INSTANCE;
//    }

    private static <T extends SecretAccess> T singleton(Class<T> accessType, Class<?> initalizeClass) {
        // Start by making sure the class is initialized
        try {
            Class.forName(initalizeClass.getName(), true, initalizeClass.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e); // Should never happen
        }

        SecretAccess access = TMP.remove(accessType);
        if (access == null) {
            throw new ExceptionInInitializerError("An instance of " + accessType + " has not been set");
        }
        return accessType.cast(access);
    }

    /**
     * Returns an access object for app.packed.util.
     * 
     * @return an access object for app.packed.util
     */
    public static AppPackedBaseAccess base() {
        return BaseSingletonHolder.INSTANCE;
    }
//
//    /** Singleton holder for {@link AppPackedArtifactAccess}. */
//    private static class ArtifactSingletonHolder {
//
//        /** The singleton instance. */
//        private static final AppPackedArtifactAccess INSTANCE = singleton(AppPackedArtifactAccess.class, ArtifactDriver.class);
//    }

//    /** Holder of the {@link AppPackedContainerAccess} singleton. */
//    private static class ContainerSingletonHolder {
//
//        /** The singleton instance. */
//        private static final AppPackedContainerAccess INSTANCE = singleton(AppPackedContainerAccess.class, ContainerBundle.class);
//    }

    /** Holder of the {@link AppPackedHookAccess} singleton. */
    private static class HookSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedHookAccess INSTANCE = singleton(AppPackedHookAccess.class, AnnotatedFieldHook.class);
    }

//    /** Holder of the {@link AppPackedLifecycleAccess} singleton. */
//    private static class LifecycleSingletonHolder {
//
//        /** The singleton instance. */
//        private static final AppPackedLifecycleAccess INSTANCE = singleton(AppPackedLifecycleAccess.class, RunState.class);
//    }
//
//    /** Holder of the {@link AppPackedServiceAccess} singleton. */
//    private static class ServiceSingletonHolder {
//
//        /** The singleton instance. */
//        private static final AppPackedServiceAccess INSTANCE = singleton(AppPackedServiceAccess.class, ServiceMode.class);
//    }

//    /** Holder of the {@link AppPackedInjectAccess} singleton. */
//    private static class InjectSingletonHolder {
//
//        /** The singleton instance. */
//        private static final AppPackedInjectAccess INSTANCE = singleton(AppPackedInjectAccess.class, ServiceMode.class);
//    }

    /** Holder of the {@link AppPackedBaseAccess} singleton. */
    private static class BaseSingletonHolder {

        /** The singleton instance. */
        private static final AppPackedBaseAccess INSTANCE = singleton(AppPackedBaseAccess.class, TypeLiteral.class);
    }
}
