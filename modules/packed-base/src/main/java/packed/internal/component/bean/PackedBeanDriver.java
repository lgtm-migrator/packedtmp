package packed.internal.component.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.component.BeanConfiguration;
import app.packed.component.BeanDriver;
import app.packed.component.BeanKind;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import packed.internal.application.BuildSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.RealmSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.ThrowableUtil;

/** Implementation of {@link BeanDriver}. */
public final class PackedBeanDriver<C extends BeanConfiguration> extends PackedComponentDriver<C> implements BeanDriver<C> {

    /** The bean type. */
    private final Class<?> beanType;

    /** The bean binder used for creating this driver. */
    final PackedBeanDriverBinder<?, C> binder;

    /** The actual binding. Either a Class, Factory or (generic) instance. */
    final Object binding;

    public PackedBeanDriver(@Nullable Wirelet wirelet, PackedBeanDriverBinder<?, C> binder, Class<?> beanType, Object binding) {
        super(wirelet);
        this.binder = requireNonNull(binder);
        this.beanType = requireNonNull(beanType);
        this.binding = requireNonNull(binding);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> beanType() {
        return beanType;
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind kind() {
        return binder.kind();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentSetup newComponent(BuildSetup build, RealmSetup realm, LifetimeSetup lifetime, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        return new BeanSetup(build, lifetime, realm, this, parent, wirelets);
    }

    @Override
    public C toConfiguration(ComponentSetup context) {
        C c;
        try {
            // TODO.. vi bruger ikke context'en lige nu. Men
            c = (C) binder.constructor().invoke(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        VH_COMPONENT_CONFIGURATION_COMPONENT.set(c, context);
        return c;
    }

    @Override
    public PackedBeanDriver<C> with(Wirelet... wirelet) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected ComponentDriver<C> withWirelet(Wirelet w) {
        throw new UnsupportedOperationException();
    }
}