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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import app.packed.analysis.BundleDescriptor;
import app.packed.base.Contract;
import app.packed.base.Nullable;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.inject.Factory;
import packed.internal.component.AbstractComponentConfiguration;
import packed.internal.moduleaccess.ModuleAccess;

/** The default implementation of {@link ExtensionContext} with addition methods only available in app.packed.base. */
public final class PackedExtensionContext implements ExtensionContext, Comparable<PackedExtensionContext> {

    // Indicates that a bundle has already been configured...
    public static final ExtensionContext CONFIGURED = new PackedExtensionContext();

    /** The extension instance this context wraps, initialized in {@link #of(PackedContainerConfiguration, Class)}. */
    @Nullable
    private Extension extension;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** The sidecar model of the extension. */
    private final ExtensionSidecarModel model;

    /** The configuration of the container the extension is registered in. */
    private final PackedContainerConfiguration pcc;

    private PackedExtensionContext() {
        this.pcc = null;
        this.model = null;
    }

    /**
     * Creates a new extension context.
     * 
     * @param pcc
     *            the configuration of the container the extension is registered in
     * @param model
     *            a model of the extension.
     */
    private PackedExtensionContext(PackedContainerConfiguration pcc, ExtensionSidecarModel model) {
        this.pcc = requireNonNull(pcc);
        this.model = requireNonNull(model);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    void buildDescriptor(BundleDescriptor.Builder builder) {
        MethodHandle mha = model.bundleBuilderMethod;
        if (mha != null) {
            try {
                mha.invoke(extension, builder);
            } catch (Throwable e1) {
                throw new UndeclaredThrowableException(e1);
            }
        }

        for (Object s : model.contracts().values()) {
            // TODO need a context
            Contract con;
            if (s instanceof Function) {
                con = (Contract) ((Function) s).apply(extension);
            } else if (s instanceof BiFunction) {
                con = (Contract) ((BiFunction) s).apply(extension, null);
            } else {
                // MethodHandle...
                try {
                    MethodHandle mh = (MethodHandle) s;
                    if (mh.type().parameterCount() == 0) {
                        con = (Contract) mh.invoke(extension);
                    } else {
                        con = (Contract) mh.invoke(extension, null);
                    }
                } catch (Throwable e1) {
                    throw new UndeclaredThrowableException(e1);
                }
            }
            requireNonNull(con);
            builder.addContract(con);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + extension().getClass().getSimpleName() + ") is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(PackedExtensionContext c) {
        return -model.compareTo(c.model);
    }

    /**
     * Returns the configuration of the container the extension is registered in.
     * 
     * @return the configuration of the container the extension is registered in
     */
    public PackedContainerConfiguration container() {
        return pcc;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return pcc.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath containerPath() {
        return pcc.path();
    }

    /**
     * Returns the extension instance this context wraps.
     * 
     * @return the extension instance this context wraps
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension extension() {
        Extension e = extension;
        if (e == null) {
            throw new IllegalStateException("Cannot call this method from the constructor of the extension");
        }
        return e;
    }

    @Override
    public Class<? extends Extension> extensionType() {
        return model.extensionType();
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return pcc.install(factory);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        return pcc.installInstance(instance);
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    public void onChildrenConfigured() {
        model.invokePostSidecarAnnotatedMethods(ExtensionSidecarModel.ON_CHILDREN_DONE, extension);
        isConfigured = true;
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    public void onConfigured() {
        model.invokePostSidecarAnnotatedMethods(ExtensionSidecarModel.ON_MAIN, extension);
        isConfigured = true;
    }

    /**
     * Returns an optional representing this extension. This is mainly to avoid allocation, as we can have a lot of them
     * 
     * @return an optional representing this extension
     */
    public Optional<Class<? extends Extension>> optional() {
        return model.optional;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Extension> parent() {
        AbstractComponentConfiguration parent = pcc.parent;
        if (parent instanceof PackedContainerConfiguration) {
            PackedExtensionContext pe = ((PackedContainerConfiguration) parent).getExtension(model.extensionType());
            if (pe != null) {
                return Optional.of(pe.extension);
            }
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        // TODO can we call this method from the constructor????
        requireNonNull(extensionType, "extensionType is null");
        // We need to check whether or not the extension is allowed to use the specified extension every time.
        // An alternative would be to cache it in a map for each extension.
        // However this would incur extra memory usage. And if we only request an extension once
        // There would be significant overhead to instantiating a new map and caching the extension.
        // A better solution is that each extension caches the extensions they use (if they want to).
        // This saves a check + map lookup for each additional request.

        // We can use a simple bitmap here as well... But we need to move this method to PEC.
        // And then look up the context before we can check.

        if (!model.directDependencies().contains(extensionType)) {
            // We allow an extension to use itself, alternative would be to throw an exception, but for what reason?
            if (extensionType == extension().getClass()) { // extension() checks for constructor
                return (T) extension;
            }

            throw new UnsupportedOperationException("The specified extension type is not among " + model.extensionType().getSimpleName()
                    + " dependencies, extensionType = " + extensionType + ", valid dependencies = " + model.directDependencies());
        }
        return (T) pcc.useExtension(extensionType, this).extension;
    }

    /**
     * Creates and initializes a new extension and its context.
     * 
     * @param pcc
     *            the configuration of the container.
     * @param extensionType
     *            the type of extension to initialize
     * @return the new extension context
     */
    public static PackedExtensionContext of(PackedContainerConfiguration pcc, Class<? extends Extension> extensionType) {
        // Create extension context and instantiate extension
        ExtensionSidecarModel model = ExtensionSidecarModel.of(extensionType);
        PackedExtensionContext pec = new PackedExtensionContext(pcc, model);
        Extension e = pec.extension = model.newExtensionInstance(pec);
        ModuleAccess.container().extensionSetContext(e, pec);

        PackedExtensionContext existing = pcc.activeExtension;
        try {
            pcc.activeExtension = pec;
            model.invokePostSidecarAnnotatedMethods(ExtensionSidecarModel.ON_INSTANTIATION, e);
            if (pcc.wireletContext != null) {
                pcc.wireletContext.extensionInitialized(pec);
            }

            // Link extension to any parents...

            // Should we also set the active extension in the parent???
//          if (model.onLinkage != null) {
            if (model.linked != null) {
                if (pcc.parent instanceof PackedContainerConfiguration) {
                    PackedContainerConfiguration p = (PackedContainerConfiguration) pcc.parent;
                    PackedExtensionContext ep = p.getExtension(extensionType);
                    // set activate extension???
                    // If not just parent link keep checking up until root/
                    if (ep != null) {
                        try {
                            model.linked.invoke(ep.extension, e);
                        } catch (Throwable e1) {
                            e1.printStackTrace();
                        }
                        // model.onLinkage.accept(e.extension, extension);
                    }
                }
            }

        } finally {
            pcc.activeExtension = existing;
        }
        return pec;
    }
}
