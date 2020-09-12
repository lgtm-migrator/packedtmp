package packed.internal.inject.factory;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.Key;
import app.packed.inject.Provide;
import app.packed.statemachine.OnStart;
import packed.internal.inject.ServiceDependency;

/** An factory support class. */
public final class FactorySupport<T> {

    /** The key that this factory will be registered under by default with an injector. */
    public final Key<T> key;

    /** A list of all of this factory's dependencies. */
    public final List<ServiceDependency> dependencies;

    /** The function used to create a new instance. */
    public final FactoryHandle<T> handle;

    public FactorySupport(FactoryHandle<T> function, List<ServiceDependency> dependencies) {
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.handle = requireNonNull(function);
        this.key = Key.fromTypeLiteral(function.typeLiteral);
    }

    public MethodType methodType() {
        return handle.methodType();

    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for annotations such as
     * {@link OnStart} and {@link Provide}.
     *
     * @return the scannable type of this factory
     */
    public Class<? super T> getScannableType() {
        return handle.returnTypeRaw();
    }
}