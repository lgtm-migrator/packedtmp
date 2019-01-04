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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import app.packed.bundle.BundlingImportStage;
import app.packed.util.Key;

/**
 * A number of utility methods
 */
// Could also name it importPeek().... exportPeek();
// And then we would only have one class
// InjectorBindingStages
// ServiceBindingStages

// BindingStages

// BindingStages.importsPeek
// BindingStages.exportsPeek

// StartingStages.
// BindingFilters

// ServiceFilters

// ServiceBundlingFilters
public final class ServiceBundlingStages {

    // public static IIS PRINT_KEY = peek(
    private ServiceBundlingStages() {}

    /**
     * This method exists mainly to support debugging, where you want to see which services are available at a particular
     * place in the pipeline: <pre>
     * {@code 
     * Injector injector = some injector to import;
     *
     * Injector.of(c -> {
     *   c.bindInjector(injector, ServiceImportStages.peek(e -> System.out.println("Importing service " + e.getKey())));
     * });}
     * </pre>
     * <p>
     * This method is typically TODO before after import events
     * 
     * @param action
     *            the action to perform for each service descriptor
     * @return a peeking stage
     */
    public static BundlingImportStage peekImports(Consumer<? super ServiceDescriptor> action) {
        requireNonNull(action, "action is null");
        return new BundlingImportStage() {
            @Override
            public void onEachService(ServiceConfiguration<?> sc) {
                action.accept(ServiceDescriptor.of(sc));
            }
        };
    }

    /**
     * Returns a stage that will rebind any service with the specified {@code from} key to the specified {@code from} key.
     * If no service with the specified key is encountered, the stage does nothing.
     * 
     * @param from
     *            the key of the service to rebind
     * @param to
     *            the key that the service should be rebound to
     * @return the new filter
     */
    public static BundlingImportStage rebindImport(Key<?> from, Key<?> to) {
        requireNonNull(from, "from is null");
        requireNonNull(to, "to is null");
        return new BundlingImportStage() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void onEachService(ServiceConfiguration<?> sc) {
                if (sc.getKey().equals(from)) {
                    sc.as((Key) to);
                }
            }
        };
    }

    /**
     * Returns a new stage that reject services with any of the specified keys.
     * 
     * @param keys
     *            the keys that should be rejected
     * @return the new import stage
     */
    public static BundlingImportStage removeImports(Class<?>... keys) {
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return removeImports(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that reject services with any of the specified keys.
     * 
     * @param keys
     *            the keys that should be rejected
     * @return the new import stage
     */
    public static BundlingImportStage removeImports(Key<?>... keys) {
        Set<Key<?>> set = Set.of(keys);
        return removeImports(d -> set.contains(d.getKey()));
    }

    // process(Key<K>, Consumer<K, K>); <- Som @Provides X process(X x) {return x}
    // process(Key<K>, Function<K, K>); <- Som @Provides X process(X x) {return something}

    /**
     * Returns a new import stage that rejects any service accepted by the specified predicate.
     * 
     * @param predicate
     *            the predicate to test against
     * @return the new import stage
     */
    public static BundlingImportStage removeImports(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return retainImports(e -> !predicate.test(e));
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified class keys.
     * 
     * @param keys
     *            the keys for which services will be accepted
     * @return the new import stage
     */
    public static BundlingImportStage retainImports(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return retainImports(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified keys.
     * 
     * @param keys
     *            the keys for which services will be accepted
     * @return the new import stage
     */
    public static BundlingImportStage retainImports(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Set.of(keys);
        return retainImports(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that only accepts the services accepted by the specified predicate.
     * <p>
     * For example, {@code accept(s -> s.getKey().isQualifiedWith(Blue.class))} will return an import stage that only
     * accepts services whose key are qualified with {@code @Blue}.
     * 
     * @param predicate
     *            the predicate that selects which services are accepted by this stage
     * @return the new import stage
     */
    public static BundlingImportStage retainImports(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return new BundlingImportStage() {
            @Override
            public void onEachService(ServiceConfiguration<?> sc) {
                if (!predicate.test(ServiceDescriptor.of(sc))) {
                    sc.asNone();
                }
            }
        };
    }
}
