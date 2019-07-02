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
package zets.Extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.container.ContainerExtension;
import zets.name.spi.AbstractBaseTest;

/**
 *
 */
public class ExtensionDependenciesTest extends AbstractBaseTest {

    /** Test that we can depend on an uninstalled extension via {@link ContainerExtension#onAdd}. */
    @Test
    public void testCanCallUseFromOnExtensionAdded() {
        appOf(c -> {
            c.use(Ex1.class);
            assertThat(c.extensions()).containsExactly(Ex1.class, Ex2.class, Ex3.class);
        });

        System.out.println("Bye");
    }

    /** While we do not advertise it. We do allow cyclic dependencies between extensions. */
    @Test
    public void testAllowCyclicDependenciesExtension() {
        appOf(c -> {
            c.use(ExRecursive1.class);
            assertThat(c.extensions()).containsExactly(ExRecursive1.class, ExRecursive2.class);
        });

        System.out.println("Bye");
    }

    static class Ex1 extends ContainerExtension<Ex1> {
        /** {@inheritDoc} */
        @Override
        protected void onAdd() {
            use(Ex2.class);
        }

    }

    static class Ex2 extends ContainerExtension<Ex2> {
        /** {@inheritDoc} */
        @Override
        protected void onAdd() {
            use(Ex3.class);
        }
    }

    static class Ex3 extends ContainerExtension<Ex3> {
        /** {@inheritDoc} */
        @Override
        protected void onAdd() {
            // use(Ex2.class);
        }
    }

    static class ExRecursive1 extends ContainerExtension<ExRecursive1> {
        /** {@inheritDoc} */
        @Override
        protected void onAdd() {
            use(ExRecursive2.class);
        }
    }

    static class ExRecursive2 extends ContainerExtension<ExRecursive2> {
        /** {@inheritDoc} */
        @Override
        protected void onAdd() {
            use(ExRecursive1.class);
        }
    }
}
