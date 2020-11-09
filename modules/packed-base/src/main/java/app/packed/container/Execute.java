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
package app.packed.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;

import app.packed.component.BuildException;
import app.packed.sidecar.ActivateMethodSidecar;
import app.packed.sidecar.MethodSidecar;
import app.packed.statemachine.OnInitialize;
import packed.internal.component.source.SourceModelMethod;

/**
 * Trying to build a container with more than a single method annotated with Execute will fail with
 * {@link BuildException}.
 * <p>
 * If the container fails to start, the method will never be invoked.
 * <p>
 * When the annotated method returns the container will automatically be stopped. Unless {@link #stopOnSucces()} has
 * been set to false. If the annotated method fails with an exception the container will automatically be shutdown with
 * the exception being the cause.
 * <p>
 * Annotated methods will never be invoked more than once??? Well if we have some retry mechanism
 */
// A single method. Will be executed.
// and then shutdown container down again

// Panic if it fails???? or do we not wrap exception??? I think we wrap...
// We always wrap in container panic exception
// @EntryPoint
// What happens with CLI
// We can have multiple entry points
// Some of them deamons and some of them not...

// Det er maaske mere noget med state end kun container...
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActivateMethodSidecar(sidecar = MySidecar.class)
public @interface Execute {

    // Skal det styres paa shell niveau?? Eller wirelet niveau..
    boolean spawnThread() default false;

    // remainRunning
    boolean stopOnSucces() default true;
}

class MySidecar extends MethodSidecar {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        MethodHandle mh = directMethodHandle();
        SourceModelMethod.Builder.registerProcessor(this, c -> {
            c.region.lifecycle.methodHandle = mh;
        });
    }

    @OnInitialize
    protected void onInit(Runnable r) {

    }

}
// ExecutionResult<T>... Maaske bare CompletableFuture

// Vi skal have en maade hvorpaa en shell driver skal kunne faa et resultat.

// CLI vs Execute

// Cli must be void...