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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import packed.internal.inject.Injectable;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Vi gemmer alt det her i en region...
// Fordi raekkefoelgen af initialisering gaar paa tvaers af containere
// Idet de kan dependende paa hinanden
public final class RegionAssembly {

    /** Components that contains constants that should be stored in a region. Is only written by {@link SourceAssembly}. */
    final ArrayList<SourceAssembly> runtimeInstances = new ArrayList<>();

    int nextIndex;

    // List of services that must be instantiated and stored in the region
    // They are ordered in the order they should be initialized
    // For now written by DependencyCycleDetector via BFS
    public final ArrayList<Injectable> constants = new ArrayList<>();

    /** Everything that needs to be injected and store, but which is not a service. */
    final ArrayList<SourceAssembly> sourceInjectables = new ArrayList<>();

    /*---***************************/

    // Taenker den her er paa injection manager
    public final ArrayList<Injectable> allInjectables = new ArrayList<>();

    // Vi bliver noedt til at kalde ned recursivt saa vi kan finde raekkefolgen af service inst

    RuntimeRegion newRegion(PackedInitializationContext pic, ComponentNode root) {
        RuntimeRegion region = new RuntimeRegion(nextIndex);

        // Not sure we want to create the guest here, we do it for now though
        if (root.modifiers().isGuest()) {
            region.store(0, new PackedGuest(null));
        }

        // We start by storing all constant instances in the region array
        for (SourceAssembly sa : runtimeInstances) {
            region.store(sa.regionIndex, sa.instance);
        }

        // All constants that must be instantiated and stored
        // Order here is very important. As for every constant.
        // Its dependencies are guaranteed to have been already stored
        for (Injectable injectable : constants) {
            MethodHandle mh = injectable.buildMethodHandle();

            Object instance;
            try {
                instance = mh.invoke(region);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }

            if (instance == null) {
                throw new NullPointerException(injectable + " returned null");
            }

            int index = injectable.regionIndex();
            region.store(index, instance);
        }

        // Last all source singletons that have not already been used as services
        // These can really invoked in any order
        for (SourceAssembly i : sourceInjectables) {
            if (i.regionIndex > -1 && !region.isSet(i.regionIndex)) {
                Injectable injectable = i.injectable;

                MethodHandle mh = injectable.buildMethodHandle();
                Object instance;
                try {
                    instance = mh.invoke(region);
                } catch (Throwable e1) {
                    throw ThrowableUtil.orUndeclared(e1);
                }
                requireNonNull(instance);
                region.store(i.regionIndex, instance);
            }
        }

        return region;
    }

    public int reserve() {
        return nextIndex++;
    }
}
