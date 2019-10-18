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
package app.packed.container.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import app.packed.container.Extension;
import app.packed.container.ExtensionComposer;
import app.packed.container.ExtensionDescriptor;
import app.packed.container.UseExtension;
import app.packed.contract.Contract;

/** Tests {@link ExtensionDescriptor}. */
public class ExtensionDescriptorTest {

    @Test
    public void empty() {
        ExtensionDescriptor ed = ExtensionDescriptor.of(EmptyExtension.class);
        assertThat(ed.contracts()).isEmpty();
        assertThat(ed.dependencies()).isEmpty();
        assertThat(ed.type()).isSameAs(EmptyExtension.class);
    }

    @Test
    public void various() {
        ExtensionDescriptor ed = ExtensionDescriptor.of(VariousExtension.class);
        assertThat(ed.contracts()).containsExactly(VariousExtension.SomeContract.class);
        assertThat(ed.dependencies()).containsExactly(EmptyExtension.class);
        assertThat(ed.type()).isSameAs(VariousExtension.class);
    }

    /** Tests that the various collections returned are unmodifiable. */
    @Test
    public void unmodifiable() {
        ExtensionDescriptor ed = ExtensionDescriptor.of(VariousExtension.class);
        assertThatThrownBy(() -> ed.contracts().clear()).isExactlyInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ed.dependencies().clear()).isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    @UseExtension(EmptyExtension.class)
    static class VariousExtension extends Extension {
        static class Composer extends ExtensionComposer<VariousExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                exposeContract(SomeContract.class, (e, c) -> new SomeContract());
            }
        }

        static class SomeContract extends Contract {

            /** {@inheritDoc} */
            @Override
            public boolean equals(Object obj) {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public int hashCode() {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public String toString() {
                throw new UnsupportedOperationException();
            }
        }
    }

    static class EmptyExtension extends Extension {

        static class Composer extends ExtensionComposer<EmptyExtension> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {}
        }
    }
}
