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
package internal.app.packed.util;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;

/**
 *
 */
public class KeyTst extends BaseAssembly {

    public static void main(String[] args) {
        App.mirror(new KeyTst());
    }

    public static class MyExt extends Extension<MyExt> {
        public MyExt() {
            System.out.println("p = " + parent());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void build() {
        link(new Chi());
//        use(MyExt.class);
    }

    static class Chi extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            use(MyExt.class);
            link2(new Chi(), l -> {
                bean().filter(l);
                bean().filter(l);
            });
        }

    }
}
