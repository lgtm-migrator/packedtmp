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
package packed.internal.util.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import app.packed.lang.reflect.FieldDescriptor;

/** Tests {@link FieldDescriptor}. */
public class InternalFieldDescriptorTest extends AbstractDescriptorTest {

    static final Class<?> C = InternalFieldDescriptorTest.class;

    String never;

    String string;

    @Test
    public void basics() throws Exception {
        validateField(C.getDeclaredField("string"), FieldDescriptor.of(C, "string"));
        validateField(C.getDeclaredField("C"), FieldDescriptor.of(C, "C"));
    }

    static void validateField(Field f, FieldDescriptor d) {
        validateMember(f, d);
        assertThat(d.descriptorTypeName()).isEqualTo("field");// always field
        assertThat(d.index()).isEqualTo(0);
        assertThat(d.getParameterizedType()).isEqualTo(f.getGenericType());
        assertThat(d.getType()).isEqualTo(f.getType());

        assertThat(d.hashCode()).isEqualTo(f.hashCode());
        assertThat(d.isNamePresent()).isTrue();
        assertThat(d.newField()).isEqualTo(f);

        assertThat(d).isEqualTo(d);
        assertThat(d).isEqualTo(FieldDescriptor.of(d.getDeclaringClass(), d.getName()));
        assertThat(d).isNotEqualTo(FieldDescriptor.of(C, "never"));
        assertThat(d).isNotEqualTo("me");

        String packageName = f.getDeclaringClass().getCanonicalName();
        assertThat(d.toString()).isEqualTo(packageName + "#" + f.getName());
    }

}
