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
package packed.internal.inject.service.assembly;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceExtension;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.ServiceBuildManager;
import packed.internal.inject.service.runtime.DelegatingInjectorEntry;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/**
 * A build entry representing an exported service. Entries at runtime has never any reference to how (or if) they where
 * exported.
 */
public final class ExportedServiceAssembly<T> extends ServiceAssembly<T> {

    /** The actual entry that is exported. Is initially null for keyed exports, until it is resolved. */
    @Nullable
    public ServiceAssembly<T> exportedEntry;

    /** The key under which to export the entry, is null for entry exports. */
    @Nullable
    public final Key<?> exportAsKey;

    /**
     * Exports an entry via its key.
     * 
     * @param builder
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     * @see ServiceExtension#export(Class)
     * @see ServiceExtension#export(Key)
     */
    public ExportedServiceAssembly(ServiceBuildManager builder, Key<T> exportAsKey, ConfigSite configSite) {
        super(builder, configSite, exportAsKey);
        this.exportAsKey = requireNonNull(exportAsKey);
    }

    /**
     * Exports an existing entry.
     * 
     * @param entryToExport
     *            the entry to export
     * @param configSite
     *            the config site of the export
     * @see ServiceExtension#exportAll()
     */
    public ExportedServiceAssembly(ServiceBuildManager builder, ServiceAssembly<T> entryToExport, ConfigSite configSite) {
        super(builder, configSite, entryToExport.key());
        this.exportedEntry = entryToExport;
        this.exportAsKey = null;
        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
    }

    @Override
    public MethodHandle dependencyAccessor() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
    public Injectable getInjectable() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingInjectorEntry<>(this, exportedEntry.toRuntimeEntry(context));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return exportedEntry.isConstant();
    }
}
