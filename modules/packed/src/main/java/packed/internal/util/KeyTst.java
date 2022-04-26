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

import app.packed.application.ApplicationMirror;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.inject.Ancestral;
import app.packed.inject.InjectionContext;

/**
 *
 */
public class KeyTst extends BaseAssembly {

    
    public static void main(String[] args) {
        ApplicationMirror.of(new KeyTst());
    }

    public static class MyExt extends Extension<MyExt> {
        public MyExt(InjectionContext c, Ancestral<MyExt> parent) {
            System.out.println(c.keys());
            System.out.println("p = " + parent.ancestorOrNull());
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
        }
        
    }
}
