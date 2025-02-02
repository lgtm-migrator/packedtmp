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
package app.packed.operation;

import java.lang.invoke.MethodType;
import java.util.Map;

import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.errorhandling.ErrorHandler;
import internal.app.packed.operation.PackedOperationTemplate;

/**
 * An operation template defines the basic behaviour of an operation and is typically reused across multiple operations.
 * 
 * 
 * <p>
 */
// I can't see why we should not define context here
// I think we should have a builder probably.
// So we can condense information


// Components
//// ExtensionContext  (Or InvocationContext??? IDK bliver jo brugt across multi usage) 
//// BeanInstance
//// Wirelet[] (For containers)
//// Contexts
//// Other arguments

//// ErrorHandling

// InvocationSite, InvocationType, Invocation contexts

//InvocationType
//Context's
//Name
//Codegen Type... (MH I guess)

//Tror invocation orderen er fixed for de forskellige typer...
//Args er altid til sidst...

// OT.forBeanOperation()
// OT.forNewApplication()
// OT.forNewContainer

public sealed interface OperationTemplate permits PackedOperationTemplate {

    boolean requiresExtensionContext();

    int beanInstanceIndex();

    // Tror vi styrer return type her.
    // Man boer smide custom fejl beskeder

    /**
     * 
     * @return the method type representing the invocation
     */
    MethodType invocationType();

    default /* OrderedMap */ Map<Class<? extends Context<?>>, ContextTemplate> contexts() {
        throw new UnsupportedOperationException();
    }

    default OperationTemplate addContext(ContextTemplate context) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param type
     * @return
     * 
     * @see OnBinding#provideFromInvocationArgument(int)
     */
    // Kan man have have loese args som ikke er del af en context???
    OperationTemplate withArg(Class<?> type);

    default OperationTemplate withBeanInstance() {
        return withBeanInstance(Object.class);
    }

    default OperationTemplate withClassifier(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    OperationTemplate withBeanInstance(Class<?> beanClass);

    OperationTemplate withReturnType(Class<?> type);

    default OperationTemplate withReturnTypeObject() {
        return withReturnType(Object.class);
    }

    // 3 choices? 
    // No ErrorHandling (Exception will propagate directly)
    // ParentHandling
    // This errorHandler
    
    // All but noErrorHandling will install an outward interceptor
    default OperationTemplate handleErrors(ErrorHandler errorHandler) {
        throw new UnsupportedOperationException();
    }
    
    // Takes EBC returns void
    static OperationTemplate defaults() {
        return PackedOperationTemplate.DEFAULTS;
    }

    static OperationTemplate raw() {
        return new PackedOperationTemplate(-1, -1, MethodType.methodType(void.class));
    }

    enum ArgumentKind {

        ARGUMENT,

        /** The invocation argument is a bean instance. */
        BEAN_INSTANCE,

        /** The invocation argument is an extension bean context. */
        // Maaske noget andet end context, given dens mening
        EXTENSION_BEAN_CONTEXT; // InvocationContext
    }
}
