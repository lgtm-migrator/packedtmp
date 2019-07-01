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
package app.packed.micro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import app.packed.app.App;
import app.packed.component.ComponentConfiguration;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ArtifactImage;
import app.packed.container.Bundle;
import app.packed.container.ContainerExtension;
import app.packed.container.ContainerExtensionActivator;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.hook.OnHook;

/**
 *
 */
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class FromImage {

    static final ArtifactImage EMPTY = ArtifactImage.of(new Bundle() {});

    static final ArtifactImage USE_EXTENSION = ArtifactImage.of(new Bundle() {
        @Override
        public void configure() {
            use(MyExtension.class);
        }
    });
    static final ArtifactImage INSTALL = ArtifactImage.of(new Bundle() {
        @Override
        public void configure() {
            install("foo");
        }
    });
    static final ArtifactImage INSTALL_AUTO_ACTIVATE = ArtifactImage.of(new Bundle() {
        @Override
        public void configure() {
            install(new MyStuff());
        }
    });

    @Benchmark
    public App empty() {
        return App.of(EMPTY);
    }

    @Benchmark
    public App useExtension() {
        return App.of(USE_EXTENSION);
    }

    @Benchmark
    public App install() {
        return App.of(INSTALL);
    }

    @Benchmark
    public App newExtensionAutoActivate() {
        return App.of(INSTALL_AUTO_ACTIVATE);
    }

    static class MyStuff {

        @ActivateMyExtension
        public void foo() {

        }
    }

    public static class MyExtension extends ContainerExtension<MyExtension> {
        protected void set(ComponentConfiguration a) {}
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @ContainerExtensionActivator(Builder.class)
    public @interface ActivateMyExtension {

    }

    static class Builder extends ContainerExtensionHookProcessor<MyExtension> {

        @OnHook
        public void anno(AnnotatedMethodHook<ActivateMyExtension> h) {

        }

        /** {@inheritDoc} */
        @Override
        public BiConsumer<ComponentConfiguration, MyExtension> onBuild() {
            return (a, b) -> {
                b.set(a);
            };
        }
    }
}
