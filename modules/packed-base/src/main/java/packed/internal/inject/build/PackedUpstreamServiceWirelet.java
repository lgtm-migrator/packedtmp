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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.config.ConfigSite;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.inject.InjectionExtension;
import app.packed.inject.ServiceDescriptor;
import app.packed.inject.UpstreamServiceWirelets;
import app.packed.util.Key;

/** The common superclass for upstream service wirelets. */
public abstract class PackedUpstreamServiceWirelet extends ExtensionWirelet<InjectionExtension, InjectionPipeline> {

    /** {@inheritDoc} */
    @Override
    public final InjectionPipeline newPipeline(InjectionExtension extension) {
        return new InjectionPipeline(extension);
    }

    /**
     * Processes an imported injector.
     * 
     * @param ii
     *            the imported injector to process
     */
    abstract void process(ImportedInjector ii);

    public static class FilterOnKey extends PackedUpstreamServiceWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        void process(ImportedInjector ii) {
            for (Key<?> key : set) {
                ii.entries.remove(key);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void process(InjectionPipeline p) {

        }
    }

    /** A wirelet for {@link UpstreamServiceWirelets#peek(Consumer)}. */
    public static class Peek extends PackedUpstreamServiceWirelet {

        /** The peek action to execute. */
        private final Consumer<? super ServiceDescriptor> action;

        /**
         * Creates a new upstream peek wirelet.
         * 
         * @param action
         *            the peek action to execute
         */
        public Peek(Consumer<? super ServiceDescriptor> action) {
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        void process(ImportedInjector ii) {
            for (BSEImported<?> e : ii.entries.values()) {
                action.accept(new ServiceConfigurationWrapper(e));
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void process(InjectionPipeline extension) {
            // TODO Auto-generated method stub
        }
    }

    /** A descriptor that wraps a service configuration. */
    static class ServiceConfigurationWrapper implements ServiceDescriptor {

        /** The configuration we read through to. */
        private final BSE<?> configuration;

        /**
         * Creates a new wrapper
         * 
         * @param configuration
         *            the configuration to wrap
         */
        ServiceConfigurationWrapper(BSE<?> configuration) {
            this.configuration = requireNonNull(configuration, "configuration is null");
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return configuration.configSite();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return Optional.ofNullable(configuration.getDescription());
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return configuration.getKey();
        }
        //
        // /** {@inheritDoc} */
        // @Override
        // public Set<String> tags() {
        // return Collections.unmodifiableSet(configuration.tags());
        // }
    }
}
