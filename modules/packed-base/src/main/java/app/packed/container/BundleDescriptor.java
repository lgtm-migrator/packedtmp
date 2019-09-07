/*
c
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
package app.packed.container;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.artifact.ArtifactImage;
import app.packed.container.extension.Extension;
import app.packed.contract.Contract;
import app.packed.contract.ContractSet;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.access.SharedSecrets;
import packed.internal.container.NonInstantiatingArtifactDriver;
import packed.internal.container.PackedContainerConfiguration;

/**
 * A bundle descriptor.
 *
 * <p>
 * A bundle descriptor describes a bundle and defines methods to obtain each of its components. The bundle descriptor
 * for a bundle is obtained by invoking the {@link java.lang.Module Module}'s {@link java.lang.Module#getDescriptor
 * getDescriptor} method.
 *
 * <p>
 * In other words a bundle must provide descriptors that are equivalent on each run.
 * <p>
 * {@code BundleDescriptor} objects are immutable and safe for use by multiple concurrent threads.
 * </p>
 */
// Pretty pringting http://www.lihaoyi.com/post/CompactStreamingPrettyPrintingofHierarchicalData.html
// Abstract Bundle Descriptor

// Description, Tags, runtimeType = {Container/Injector}, BundleFactory.class, Descriptor=InjectorBundleDescriptor
// (maaske faas den fra BundleFactory)
// Bundles do now support selectively deciding which bundles can import other bundles.
// This is supported by modularity.
// For example, that only ZBundle can import CXbundle. This is modules..

// includes implementation details....
// name, id, type, stuff I think this is in the descriptor???

// Hvis vi extender contracten her... Boer vi vel ogsaa kunne overskrive denne... Hvad vi jo kan...
// Problemet er at Contract er en abstract klasse....
// Maaske en AbstractContract.....
// Vi vil gerne kunne lave descriptors... med hjemmelavet stuff..... Har jeg lidt svaert ved at se hvordan kan fungere
//
// AbstractBundleDescriptor + BundleDescriptor...

// Does not include dependency graf I think...
// Maybe have a BundleDescriptor.Options object and the BundleDescriptor.of(Bundle b, Options... options);
// BundleDescriptor.graph() throws UOE if Options.IncludeDependencyGraph has not been set.

// Could be made into a Visitor instead..... Or in addition to...
// Skal igen bare vaere en wrapper oven paa AnyBundle. Okay her bliver det lidt problematisk med at man kan definere sin
// egen. Boern... altid en hovedpine

// Maaske ender man ikke med at kunne det. Men det er en god ovelse for at separare ting.

// I think add @Description as annotation??? IDK

// ArtifactModel.. Hmm, not sure..
// For example, we do not have a path
// We also take something that is not an artifact
// More Like ContainerDescriptor????
// Because we can also create from a ContainerImage
/// Yes but that image is created from a bundle of some kind.
public class BundleDescriptor {

    /** The type of the bundle. */
    private final Class<? extends Bundle> bundleType;

    private final ContractSet contracts;

    /** The (optional) description of the bundle. */
    @Nullable
    private final String description;

    @Nullable
    private String mainEntryPoint;// <--- CanonicalName#MethodName(without args)

    private final String name;

    private final LinkedHashSet<Class<? extends Extension>> extensions;

    /**
     * Creates a new descriptor from the specified builder.
     * 
     * @param builder
     *            a builder object
     */
    protected BundleDescriptor(BundleDescriptor.Builder builder) {
        requireNonNull(builder, "builder is null");
        this.contracts = ContractSet.of(builder.contracts.values());
        this.bundleType = builder.bundleType();
        this.description = builder.getBundleDescription();
        this.name = builder.name == null ? "?" : builder.name;
        this.extensions = builder.extensions;
    }

    /**
     * Returns the module that this bundle is a member of. This is always the module in which the bundle type is located in.
     * <p>
     * If the bundle is in an unnamed module then the {@linkplain ClassLoader#getUnnamedModule() unnamed} {@code Module} of
     * the class loader for the bundle implementation is returned.
     *
     * @return the module that the bundle is a member of
     * @see Class#getModule()
     */
    public final Module bundleModule() {
        return bundleType.getModule();
    }

    /**
     * Returns the type of the bundle.
     *
     * @return the type of the bundle
     */
    public final Class<? extends Bundle> bundleType() {
        return bundleType;
    }

    // Kan ikke rigtig se hvordan det skulle fungere.... med mindre vi har

    // <T extends AnyBundleDescriptor> List<T> children(Class<T> descriptorType) {
    // Men hvem bestemmer hvilken descriptor type vi laver????
    // Hvis det er en tom skal, der tager en Builder???

    // De er vel named.... Saa Map<String, Descriptor...
    public List<BundleDescriptor> children() {
        // Saa skal vi vel ogsaa have navne...
        // Maaske kan vi have Container? <- Indicating that it will be created with Container and then some postfix
        throw new UnsupportedOperationException();
    }

    public ContractSet contracts() {
        return contracts;

    }

    /**
     * Returns any description that has been set for the bundle via {@link BaseBundle#setDescription(String)}.
     * 
     * @return a optional description of the bundle
     * 
     * @see BaseBundle#setDescription(String)
     */
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * Returns the name of the bundle.
     * 
     * @return the name of the bundle
     */
    public final String name() {
        return name;
    }

    /** Prints this descriptor to {@code system.out}. */
    public final void print() {
        System.out.println(toString());
    }

    public String toJSON() {
        // Kan maaske have noget funktionality til at lave diffs....
        // Er nok mere vigtig paa contracts...
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("bundle { name: ").append(name);
        sb.append(", type: ").append(format(bundleType));
        if (bundleModule().isNamed()) {
            sb.append(", module: ").append(bundleModule().getName());
        }

        sb.append(" }");
        return sb.toString();
    }

    /**
     * Returns the version of this bundle. The version of a bundle is always identical to the version of the module to which
     * the bundle belongs. If the bundle is in the unnamed module or on the class path this method returns
     * {@link Optional#empty()}.
     * 
     * @return the version of the bundle, or an empty optional if the bundle does not have a version
     * @see ModuleDescriptor#version()
     */
    public final Optional<Version> version() {
        // To keep things simple we do not currently allow people override the version.
        ModuleDescriptor descriptor = bundleModule().getDescriptor();
        return descriptor == null ? Optional.empty() : descriptor.version();
    }

    public static ContractSet constractOf(BaseBundle bundle) {
        return BundleDescriptor.of(bundle).contracts();
    }

    public Set<Class<? extends Extension>> extensionsUsed() {
        // Do we want some kind of order???
        return extensions;
    }

    // Or just have a descriptor() on ContainerImage();
    public static BundleDescriptor of(ArtifactImage image) {
        return of((ContainerSource) image);
    }

    /**
     * Returns a bundle descriptor for the specified bundle.
     *
     * @param bundle
     *            the bundle to return a descriptor for
     * @return a descriptor for the specified bundle
     */
    // ContainerSource????
    // For example, we should be able to take an image...
    public static BundleDescriptor of(Bundle bundle) {
        return of((ContainerSource) bundle);
    }

    @SuppressWarnings("unchecked")
    private static BundleDescriptor of(ContainerSource source) {
        requireNonNull(source, "source is null");
        PackedContainerConfiguration conf = new PackedContainerConfiguration(BundleDescriptorArtifactDriver.INSTANCE, source);
        Class<? extends Bundle> bundleType;
        boolean isImage;
        if (source instanceof Bundle) {
            bundleType = (Class<? extends Bundle>) source.getClass();
            isImage = false;
            conf = new PackedContainerConfiguration(BundleDescriptorArtifactDriver.INSTANCE, source);
        } else if (source instanceof ArtifactImage) {
            ArtifactImage img = (ArtifactImage) source;
            bundleType = img.sourceType();
            isImage = true;
            conf = SharedSecrets.artifact().getConfiguration(img);
        } else {
            throw new IllegalArgumentException();
        }
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundleType);
        conf.buildDescriptor(builder, isImage);
        return builder.build();
    }
    // /**
    // * <p>A stream builder has a lifecycle, which starts in a building
    // * phase, during which elements can be added, and then transitions to a built
    // * phase, after which elements may not be added. The built phase begins
    // * when the {@link #build()} method is called, which creates an ordered
    // * {@code Stream} whose elements are the elements that were added to the stream
    // * builder, in the order they were added.
    //
    // /**
    // * Builds the stream, transitioning this builder to the built state.
    // * An {@code IllegalStateException} is thrown if there are further attempts
    // * to operate on the builder after it has entered the built state.
    // *
    // * @return the built stream
    // * @throws IllegalStateException if the builder has already transitioned to
    // * the built state
    // */
    // Stream<T> build();
    //
    // }

    /**
     *
     * @apiNote A {@code Builder} checks the components and invariants as components are added to the builder. The rationale
     *          for this is to detect errors as early as possible and not defer all validation to the {@link #build build}
     *          method.
     */
    public static class Builder {

        /** An optional description of the bundle. */
        @Nullable
        private String bundleDescription;

        /** The bundleType */
        private final Class<? extends Bundle> bundleType;

        private IdentityHashMap<Class<? extends Contract>, Contract> contracts = new IdentityHashMap<>();

        private String name;

        private Map<Key<?>, ServiceDescriptor> services;

        public final LinkedHashSet<Class<? extends Extension>> extensions = new LinkedHashSet<>();

        public Builder(Class<? extends Bundle> bundleType) {
            this.bundleType = requireNonNull(bundleType, "bundleType is null");
        }

        public void addContract(Contract contract) {
            requireNonNull(contract, "contract is null");
            if (contracts.putIfAbsent(contract.getClass(), contract) != null) {
                throw new IllegalStateException("A contract of the specified type has already been added, type " + contract.getClass());
            }
        }

        public Builder addServiceDescriptor(ServiceDescriptor descriptor) {
            requireNonNull(descriptor, "descriptor is null");
            Map<Key<?>, ServiceDescriptor> s = services;
            if (s == null) {
                s = services = new HashMap<>();
            }
            s.put(descriptor.key(), descriptor); // Do we want a defensive copy???
            return this;
        }

        public BundleDescriptor build() {
            return new BundleDescriptor(this);
        }

        /**
         * @return the bundleType
         */
        public final Class<? extends Bundle> bundleType() {
            return bundleType;
        }

        @Nullable
        public final String getBundleDescription() {
            return bundleDescription;
        }

        public Builder setBundleDescription(@Nullable String description) {
            this.bundleDescription = description;
            return this;
        }

        public Builder setName(String name) {
            this.name = requireNonNull(name);
            return this;
        }
    }
}

/** An dummy artifact driver for creating artifact images. */
final class BundleDescriptorArtifactDriver extends NonInstantiatingArtifactDriver<ArtifactImage> {

    /** The single instance. */
    static final BundleDescriptorArtifactDriver INSTANCE = new BundleDescriptorArtifactDriver();

    /** Singleton */
    private BundleDescriptorArtifactDriver() {}
}

//
/// **
// * Returns the runtime type of the bundle. Is currently one of {@link Container} or {@link Injector}.
// *
// * @return the runtime type of the bundle
// */
// public final Class<?> runtimeType() {
// return Bundle.class.isAssignableFrom(bundleType) ? Container.class : Injector.class;
// }
//
/// **
// * Returns the id of the bundle. If the bundle is in a named module it the name of the module concatenated with
// * {@code "." + bundleType.getSimpleName()}. If this bundle is not in a named module it is just
// * {bundleType.getSimpleName()}
// *
// * @return the id of the bundle
// */
// public final String bundleId() {
// // Think we are going to drop this....
// if (bundleModule().isNamed()) {
// return bundleModule().getName() + "." + bundleType.getSimpleName();
// }
// return bundleType.getSimpleName();
// }
// Det gode ved at have en SPEC_VERSION, er at man kan specificere man vil bruge.
// Og dermed kun importere praecis de interfaces den definere...
// Deploy(someSpec?) ved ikke lige med API'en /
// FooBarBundle.API$2_2
// FooBarBundle.API$2_3-SNAPSHOT hmmm, saa forsvinder den jo naar man releaser den???
// Maaske hellere have den markeret med @Preview :D
/// Bundlen, kan maaske endda supportere flere versioner??Som i flere versioner??

// The union of exposedServices, optionalService and requiredService must be empty
// Hmm, vi gider ikke bygge dobbelt check..., og vi gider ikke lave en descriptor hver gang.
// Saa koden skal nok ligge andet steds..
//
/// **
// * Returns any annotations that are present on the bundle. For example, {@link Deprecated}
// *
// * @return any annotations that are present on the bundle
// */
//// Nah lad os ditche dest
// public AnnotatedElement annotations() {
// return bundleType;
// }
