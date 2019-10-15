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
package packed.internal.util;

import java.util.ServiceLoader;

import app.packed.container.Bundle;
import app.packed.util.BaseSupport;

/**
 *
 */
public class BaseSupportLoader {

    public static void main(String[] args) {
        for (BaseSupport bs : ServiceLoader.load(BaseSupport.class, BaseSupportLoader.class.getClassLoader())) {
            System.out.println(bs);

        }
    }
}

class DefaultBS extends BaseSupport {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        this.scanBundle(new Bundle() {

            @Override
            protected void configure() {}
        });
    }

}
