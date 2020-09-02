package packed.internal.service.buildtime.service;

import java.util.List;

import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.RuntimeEntry;

public class CompositeBuildEntry<T> extends BuildEntry<T> {

    public CompositeBuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, List<ServiceDependency> dependencies) {
        super(serviceExtension, configSite, dependencies);
    }

    @Override
    public boolean hasUnresolvedDependencies() {
        return !dependencies.isEmpty();
    }

    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.PROTOTYPE;
    }

    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
