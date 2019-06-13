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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.container.AnyBundle;
import app.packed.container.App;
import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;

/**
 *
 */
public class ContainerFactory {

    public static App appOf(AnyBundle bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        DefaultContainerConfiguration configuration = new DefaultContainerConfiguration(null, WiringType.APP_OF, bundle.getClass(), bundle, wirelets);
        return new DefaultApp(configuration.buildContainer());
    }

    public static Injector injectorOf(Bundle bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        DefaultContainerConfiguration builder = new DefaultContainerConfiguration(null, WiringType.INJECTOR_OF, bundle.getClass(), bundle, wirelets);
        bundle.doConfigure(builder);
        return builder.buildInjector();
    }

    public static Injector injectorOf(Consumer<? super InjectorConfigurator> configurator, Wirelet... wirelets) {
        requireNonNull(configurator, "configurator is null");
        // Hmm vi burde have en public version af ContainerBuilder
        // Dvs. vi naar vi lige praecis har fundet ud af hvordan det skal fungere...
        DefaultContainerConfiguration builder = new DefaultContainerConfiguration(null, WiringType.INJECTOR_OF, configurator.getClass(), null, wirelets);
        configurator.accept(new InjectorConfigurator(builder));
        return builder.buildInjector();
    }

    public static BundleDescriptor of(Bundle bundle) {
        requireNonNull(bundle, "bundle is null");
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundle.getClass());
        DefaultContainerConfiguration conf = new DefaultContainerConfiguration(null, WiringType.DESCRIPTOR, bundle.getClass(), bundle);
        conf.buildDescriptor(builder);
        return builder.build();
    }

}
