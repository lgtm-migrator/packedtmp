package packed.internal.container;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.container.BaseContainerConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.container.Extension;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.LookupUtil;

/** A special component driver that create containers. */
public class PackedContainerDriver<C extends ContainerConfiguration> extends PackedComponentDriver<C> implements ContainerDriver<C> {

    /** A handle that can access ContainerConfiguration#container. */
    private static final VarHandle VH_ABSTRACT_CONTAINER_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ContainerConfiguration.class,
            "container", ContainerSetup.class);

    public PackedContainerDriver(@Nullable Wirelet wirelet) {
        super(wirelet);
    }

    @Override
    public Set<Class<? extends Extension>> disabledExtensions() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        return new ContainerSetup(build, realm, lifetime, this, parent, wirelets);
    }

    @SuppressWarnings("unchecked")
    @Override
    public C toConfiguration(ComponentSetup context) {
        BaseContainerConfiguration cc = new BaseContainerConfiguration();
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(cc, context);
        VH_ABSTRACT_CONTAINER_CONFIGURATION.set(cc, context);
        return (C) cc;
    }

    @Override
    public ContainerDriver<C> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ComponentDriver<C> withWirelet(Wirelet w) {
        throw new UnsupportedOperationException();
    }

}