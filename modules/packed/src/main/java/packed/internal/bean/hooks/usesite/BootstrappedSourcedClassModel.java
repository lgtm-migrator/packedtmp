package packed.internal.bean.hooks.usesite;

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import app.packed.bean.hooks.OldBeanFieldHook;
import app.packed.bean.hooks.OldBeanMethodHook;
import packed.internal.bean.hooks.FieldHookModel;
import packed.internal.bean.hooks.MethodHookBootstrapModel;
import packed.internal.container.ExtensionModel;
import packed.internal.util.OpenClass;

public class BootstrappedSourcedClassModel {

    /**
     * Creates a new component model instance.
     * 
     * @param realm
     *            a model of the container source that is trying to install the component
     * @param oc
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static HookModel newModel(OpenClass oc, @Nullable ExtensionModel extension) {
        return new Builder(oc, extension).build();
    }

    private static class Builder extends HookModel.Builder {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodHookBootstrapModel> METHOD_ANNOTATIONS = new ClassValue<>() {

            @Override
            protected MethodHookBootstrapModel computeValue(Class<?> type) {
                OldBeanMethodHook ams = type.getAnnotation(OldBeanMethodHook.class);
                return ams == null ? null : new MethodHookBootstrapModel.Builder(ams).build();
            }
        };


        /** A cache of any extensions a particular annotation activates. */
        static final ClassValue<FieldHookModel> FIELD_ANNOTATIONS = new ClassValue<>() {

            @Override
            protected FieldHookModel computeValue(Class<?> type) {
                OldBeanFieldHook afs = type.getAnnotation(OldBeanFieldHook.class);
                return afs == null ? null : new FieldHookModel.Builder(afs).build();
            }
        };
        
        private Builder(OpenClass cp, @Nullable ExtensionModel extension) {
            super(cp, extension);
        }

        protected @Nullable MethodHookBootstrapModel getMethodModel(Class<? extends Annotation> annotationType) {
            return METHOD_ANNOTATIONS.get(annotationType);
        }

        @Override
        protected @Nullable FieldHookModel getFieldModel(Class<? extends Annotation> annotationType) {
            return FIELD_ANNOTATIONS.get(annotationType);
        }
    }
}
