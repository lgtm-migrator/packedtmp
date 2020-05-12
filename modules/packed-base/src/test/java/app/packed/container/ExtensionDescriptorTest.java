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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import app.packed.base.Contract;
import app.packed.sidecar.Expose;

/** Tests {@link PackedExtensionDescriptor}. */
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

    @ExtensionSidecar(dependencies = EmptyExtension.class)
    static class VariousExtension extends Extension {

        @Expose
        SomeContract expose() {
            return new SomeContract();
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

    }
}
