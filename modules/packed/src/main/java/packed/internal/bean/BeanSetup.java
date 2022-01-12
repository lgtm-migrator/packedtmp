package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.hooks.usage.BeanType;
import app.packed.bean.mirror.BeanElementMirror;
import app.packed.component.ComponentMirror;
import app.packed.component.UserOrExtension;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.bean.hooks.usesite.BootstrappedClassModel;
import packed.internal.bean.inject.DependencyDescriptor;
import packed.internal.bean.inject.DependencyProducer;
import packed.internal.bean.inject.InjectionNode;
import packed.internal.component.ComponentSetup;
import packed.internal.container.ContainerSetup;
import packed.internal.container.RealmSetup;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.lifetime.PoolEntryHandle;

/** The build-time configuration of a bean. */
public final class BeanSetup extends ComponentSetup implements DependencyProducer {

    /**
     * Factory that was specified if this bean was not created from a single bean instance.
     * <p>
     * We only keep this around to find the default key that the bean will be exposed as if registered as a service. We lazy
     * calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** A model of the hooks on the bean. */
    public final BootstrappedClassModel hookModel;

    /* Information about the type of bean. */

    public final BeanType beanType;

    public final UserOrExtension owner;

    /** An injection node, if instances of the source needs to be created at runtime (not a constant). */
    @Nullable
    private final InjectionNode injectionNode;

    /** A service object if the source is provided as a service. */
    // Would be nice if we could move it somewhere else.. Like Listener
    @Nullable
    private ServiceSetup service;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    @Nullable
    public final PoolEntryHandle singletonHandle;

    public BeanSetup(ContainerSetup container, RealmSetup realm, LifetimeSetup lifetime, PackedBeanMaker<?> beanHandle) {
        super(container.application, realm, lifetime, container);
        this.beanType = BeanType.BASE;
        // Tror ikke vi skal have den her med. Maa tage det fra realmen
        this.owner = beanHandle.userOrExtension;
        this.factory = beanHandle.factory;
        this.singletonHandle = beanHandle.kind == BeanType.BASE ? lifetime.pool.reserve(beanHandle.beanType) : null;

        // Eller skal det her maaske i realmen????
        if (owner.isExtension()) {
            container.useExtensionSetup(owner.extension(), null).beans.beans.put(Key.of(beanHandle.beanType), this);
        }

        if (factory == null) {
            // We already have a bean instance, no need to have an injection node for creating a new bean instance.
            this.injectionNode = null;

            // Store the supplied bean instance in the lifetime (constant) pool. (Or maybe
            lifetime.pool.addConstant(pool -> singletonHandle.store(pool, beanHandle.source));
            // Or maybe just bind the instance directly in the method handles.
        } else {
            // Extract a MethodHandlefrom the factory
            MethodHandle mh = realm.accessor().toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.dependenciesOld();
            this.injectionNode = new InjectionNode(this, dependencies, mh);
            container.beans.addNode(injectionNode);
        }

        // Find a hook model for the bean type and wire it
        this.hookModel = realm.accessor().modelOf(beanHandle.beanType);
        hookModel.onWire(this);

        // Set the name of the component if it have not already been set using a wirelet
        initializeNameWithPrefix(hookModel.simpleName());
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return injectionNode;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // If we have a singleton accessor return a method handle that can read the single bean instance
        // Otherwise return a method handle that can instantiate a new bean
        if (singletonHandle != null) {
            return singletonHandle.poolReader(); // MethodHandle(ConstantPool)T
        } else {
            return injectionNode.buildMethodHandle(); // MethodHandle(ConstantPool)T
        }
    }

    /** {@inheritDoc} */
    @Override
    public BeanMirror mirror() {
        return new BuildTimeBeanMirror();
    }

    public ServiceSetup provide() {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
            Key<?> key;
            if (factory != null) {
                key = Key.convertTypeLiteral(factory.typeLiteral());
            } else {
                key = Key.of(hookModel.clazz); // Move to model?? What if instance has Qualifier???
            }
            s = service = parent.beans.getServiceManagerOrCreate().provideSource(this, key);
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) parent.beans.getServiceManagerOrCreate().exports().export(service);
    }

    public void sourceProvide() {
        realm.checkOpen();
        provide();
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        realm.checkOpen();
        provide().as(key);
    }

    public Optional<Key<?>> sourceProvideAsKey() {
        return service == null ? Optional.empty() : Optional.of(service.key());
    }

    /** A build-time bean mirror. */
    public final class BuildTimeBeanMirror extends AbstractBuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanType() {
            return hookModel.clazz;
        }

        /** {@inheritDoc} */
        public final ContainerMirror container() {
            return parent.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension<?>>> registrant() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BeanKind kind() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Collection<ComponentMirror> children() {
            return List.of();
        }

        /** {@inheritDoc} */
        @Override
        public Set<BeanElementMirror> hooks() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public <T extends BeanElementMirror> Set<?> hooks(Class<T> hookType) {
            return null;
        }
    }
}
