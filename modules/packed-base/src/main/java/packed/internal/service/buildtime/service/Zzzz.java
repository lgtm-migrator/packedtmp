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
package packed.internal.service.buildtime.service;

import java.util.concurrent.atomic.AtomicInteger;

import app.packed.component.App;
import app.packed.container.BaseBundle;

/**
 *
 */
public class Zzzz extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provideInstance("GooBar");
        provideInstance(123);
        provide(Dooox.class);
        providePrototype(XX.class);
        provide(Dooo.class);
        provide(Dx.class);
        export(Dooox.class);
    }

    public static void main(String[] args) {
        try (App app = App.of(new Zzzz())) {
            System.out.println(app.use(Dooox.class));
        }
        System.out.println("BYE");
    }

    public static class XX {
        public XX() {
            new Exception().printStackTrace();
            System.out.println("_______ NEW XX_______");
        }
    }

    public static class Dx {
        public Dx(XX x) {

        }
    }

    public static class Dooox {
        static final AtomicInteger I = new AtomicInteger();
        int i = I.getAndIncrement();

        public Dooox(XX ss, XX xx, String s, int s13, int d23, XX xx5, XX xx4, XX xx1) {
            System.out.println("NEW " + s + " " + " " + s13 + "  " + xx);
        }
    }

    public static class Dooo {
        public Dooo(Dooox fo1, String foo, Dx d) {
            // System.out.println("Inst " + fo1.i + " " + +fo2.i);
        }
    }
}
