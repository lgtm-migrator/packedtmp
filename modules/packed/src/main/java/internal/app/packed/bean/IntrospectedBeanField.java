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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;

import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnField;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.bean.InvalidBeanDefinitionException;
import app.packed.container.Extension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.bean.AssemblyMetaModel.FieldRecord;
import internal.app.packed.bean.IntrospectedBean.Contributor;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.FieldOperationTarget;

/** Responsible for introspecting fields on a bean. */
public final class IntrospectedBeanField implements OnField {

    /** Whether or not the field can be read. */
    final boolean allowGet;

    /** Whether or not the field can be written. */
    final boolean allowSet;

    /** The annotations on the field */
    final Annotation[] annotations;

    /** The extension that will operate any operations. */
    public final Contributor contributer;

    /** The bean member. */
    protected final Field field;

    /** The introspected bean. */
    public final IntrospectedBean introspectedBean;

    /** Whether or not we can create new operations from this class. */
    private boolean isConfigurationDisabled;

    private IntrospectedBeanField(IntrospectedBean introspectedBean, Contributor contributer, Field field, boolean allowGet, boolean allowSet,
            Annotation[] annotations) {
        this.introspectedBean = introspectedBean;
        this.contributer = contributer;
        this.field = field;
        this.allowGet = allowGet;
        this.allowSet = allowSet;
        this.annotations = annotations;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new BeanAnnotationReader(annotations);
    }

    /** Callback into an extension's {@link BeanIntrospector#onField(OnField)} method. */
    private void callbackOnFieldHook() {
        contributer.introspector().onField(this);
        isConfigurationDisabled = true;
        introspectedBean.resolveOperations(); // resolve bindings on any operations we have created
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    private void checkConfigurable() {
        if (isConfigurationDisabled) {
            throw new IllegalStateException("This method must be called from within " + BeanIntrospector.class + ":onField");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return field;
    }

    /** {@inheritDoc} */
    @Override
    public int modifiers() {
        return field.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newGetOperation() {
        checkConfigurable();
        MethodHandle mh = introspectedBean.oc.unreflectGetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        return newOperation(mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(AccessMode accessMode) {
        checkConfigurable();
        Lookup lookup = introspectedBean.oc.lookup(field);

        VarHandle varHandle;
        try {
            varHandle = lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a VarHandle", e);
        }

        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return newOperation(mh, accessMode);
    }

    private OperationHandle newOperation(MethodHandle mh, AccessMode accessMode) {
        FieldOperationTarget fot = new FieldOperationTarget(mh, field, accessMode);
        OperationType operationType = OperationType.ofFieldAccess(field, accessMode);
        OperationSetup operation = new OperationSetup(introspectedBean.bean, operationType, contributer.extension(), fot, null);
        introspectedBean.unBoundOperations.add(operation);
        introspectedBean.bean.operations.add(operation);
        return operation.toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation() {
        checkConfigurable();
        Lookup lookup = introspectedBean.oc.lookup(field);

        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return newOperation(methodHandle, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(field);
    }

    static void introspectAllFields(IntrospectedBean introspector, Class<?> clazz) {
        // We never process classes in the "java.base" module.
        if (clazz.getModule() != IntrospectedBean.JAVA_BASE_MODULE) {

            // Recursively call into superclass, before processing own fields
            introspectAllFields(introspector, clazz.getSuperclass());

            // PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);

            // Iterate over all declared fields
            for (Field field : clazz.getDeclaredFields()) {
                introspectSingleField(introspector, field);
            }
        }
    }

    /**
     * Introspect a single field on a bean looking for hook annotations.
     * 
     * @param field
     *            the field to introspect
     * 
     * @throws InvalidBeanDefinitionException
     *             if there are multiple {@link BindingHook} on the field. Or if there are both {@link FieldHook} and
     *             {@link BindingHook} annotations
     * 
     * @apiNote Currently we allow multiple {@link FieldHook} on a field. This might change in the future, but for now we
     *          allow it.
     */
    private static void introspectSingleField(IntrospectedBean introspector, Field field) {
        // Get all annotations on the field
        Annotation[] annotations = field.getAnnotations();

        // Iterate through the annotations and look for usage of field and binding hook (meta) annotations
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];

            // Look in the field annotation cache to see if the annotation is a meta annotation
            FieldRecord e = introspector.assemblyMetaModel.lookupFieldAnnotation(annotation.annotationType());

            // The annotation is neither a field or binding annotation
            if (e == null) {
                continue;
            }

            // A record + map that we use if have multi field hook annotations
            record MultiField(Class<? extends Extension<?>> extensionClass, boolean allowGet, boolean allowSet, Annotation... annotations) {}
            IdentityHashMap<Class<? extends Extension<?>>, MultiField> multiMatch = null;

            // Try to find additional meta annotations.
            for (int j = i + 1; j < annotations.length; j++) {
                Annotation annotation2 = annotations[j];

                // Look in the annotation cache to see if the annotation is a meta annotation
                FieldRecord e2 = introspector.assemblyMetaModel.lookupFieldAnnotation(annotation2.annotationType());

                // The annotation is neither a field or provision annotation
                if (e2 == null) {
                    continue;
                }

                if (e.isProvision() || e2.isProvision()) {
                    throw new InvalidBeanDefinitionException("Cannot use both " + annotation + " and " + annotation2);
                }

                // Okay we have more than 1 valid annotation

                // Check to see if we need to create the multi match map
                if (multiMatch == null) {
                    multiMatch = new IdentityHashMap<>();
                    // Start by adding the first match
                    multiMatch.put(e.extensionType(), new MultiField(e.extensionType(), e.isGettable(), e.isSettable(), annotation));
                }

                // Add this match
                multiMatch.compute(e2.extensionType(), (Class<? extends Extension<?>> key, MultiField value) -> {
                    if (value == null) {
                        return new MultiField(key, e2.isGettable(), e2.isSettable(), annotation2);
                    } else {
                        Annotation[] a = new Annotation[value.annotations.length + 1];
                        for (int k = 0; k < value.annotations.length; k++) {
                            a[k] = value.annotations[k];
                        }
                        a[a.length - 1] = annotation2;
                        return new MultiField(key, e2.isGettable() && value.allowGet, e2.isSettable() && e2.isSettable(), a);
                    }
                });
            }

            // All done. Let's see if we only had a single match or multiple matches
            if (multiMatch == null) {
                // Get the matching extension, installing it if needed.
                IntrospectedBean.Contributor contributor = introspector.computeContributor(e.extensionType(), false);

                // Create the wrapped field that is exposed to the extension
                IntrospectedBeanField f = new IntrospectedBeanField(introspector, contributor, field, e.isGettable() || contributor.hasFullAccess(),
                        e.isSettable() || contributor.hasFullAccess(), new Annotation[] { annotation });
                f.callbackOnFieldHook();
            } else {
                // TODO we should sort by extension order when we have more than 1 match
                for (MultiField mf : multiMatch.values()) {
                    IntrospectedBean.Contributor contributor = introspector.computeContributor(mf.extensionClass, false);

                    // Create the wrapped field that is exposed to the extension
                    IntrospectedBeanField f = new IntrospectedBeanField(introspector, contributor, field, mf.allowGet || contributor.hasFullAccess(),
                            mf.allowSet || contributor.hasFullAccess(), annotations);
                    f.callbackOnFieldHook();
                }
            }
        }
    }
}
