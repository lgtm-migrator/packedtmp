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
package tck.bundle.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.container.BaseBundle;
import app.packed.container.BundleDescriptor;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.C;

/**
 * 
 */
public class ServicesTest {

    /** A service will never be both requires and optional. */
    @Test
    public void requiresOverrideOptional() {
        BundleDescriptor d = BundleDescriptor.of(new BaseBundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(B.class);
                provide(A.class);
                provide(C.class);
            }
        });
        assertThat(d).isNotNull();
    }
}
