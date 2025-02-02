package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

import app.packed.application.BuildException;
import app.packed.container.Assembly;
import app.packed.container.AssemblyHook;
import app.packed.container.ContainerConfiguration;
import app.packed.container.DelegatingAssembly;
import internal.app.packed.bean.BeanHookModel;
import internal.app.packed.util.ThrowableUtil;

/** A model of an {@link Assembly}. */
public final /* primitive */ class AssemblyModel {

    /** Cached models of assembly classes. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            ArrayList<AssemblyHook.Processor> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof AssemblyHook h) {
                    for (Class<? extends AssemblyHook.Processor> b : h.value()) {
                        if (AssemblyHook.Processor.class.isAssignableFrom(b)) {
                            MethodHandle constructor;

                            if (!AssemblyModel.class.getModule().canRead(type.getModule())) {
                                AssemblyModel.class.getModule().addReads(type.getModule());
                            }

                            Lookup privateLookup;
                            try {
                                privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /* lookup */);
                            } catch (IllegalAccessException e1) {
                                throw new RuntimeException(e1);
                            }
                            // TODO fix visibility
                            // Maybe common findConstructorMethod
                            try {
                                constructor = privateLookup.findConstructor(b, MethodType.methodType(void.class));
                            } catch (NoSuchMethodException e) {
                                throw new BuildException("A container hook must provide an empty constructor, hook = " + h, e);
                            } catch (IllegalAccessException e) {
                                throw new BuildException("Can't see it sorry, hook = " + h, e);
                            }
                            constructor = constructor.asType(MethodType.methodType(AssemblyHook.Processor.class));

                            AssemblyHook.Processor instance;
                            try {
                                instance = (AssemblyHook.Processor) constructor.invokeExact();
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            if (!hooks.isEmpty() && DelegatingAssembly.class.isAssignableFrom(type)) {
                throw new BuildException("Delegating assemblies cannot use @" + AssemblyHook.class.getSimpleName() + " annotations, assembly type =" + type);
            }
            return new AssemblyModel(type, hooks.toArray(s -> new AssemblyHook.Processor[s]));
        }
    };

    /** Any hooks that have been specified on the assembly. */
    private final AssemblyHook.Processor[] hooks;

    public final BeanHookModel hookModel;

    private AssemblyModel(Class<?> assemblyClass, AssemblyHook.Processor[] hooks) {
        this.hooks = requireNonNull(hooks);
        this.hookModel = BeanHookModel.of(assemblyClass);
    }

    public void postBuild(ContainerConfiguration configuration) {
        for (AssemblyHook.Processor h : hooks) {
            h.afterBuild(configuration);
        }
    }

    public void preBuild(ContainerConfiguration configuration) {
        for (AssemblyHook.Processor h : hooks) {
            h.beforeBuild(configuration);
        }
    }

    public static AssemblyModel of(Class<?> assemblyOrComposer) {
        return MODELS.get(assemblyOrComposer);
    }
}
