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
package app.packed.lang.reflect;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Member;

/**
 *
 */
public abstract class AbstractDescriptorTest {

    static void validateMember(Member expected, Member actual) {
        assertThat(expected.getDeclaringClass()).isSameAs(actual.getDeclaringClass());
        assertThat(expected.getModifiers()).isEqualTo(actual.getModifiers());
        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.isSynthetic()).isEqualTo(actual.isSynthetic());
    }
}
