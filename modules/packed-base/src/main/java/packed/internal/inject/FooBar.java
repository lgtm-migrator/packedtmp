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
package packed.internal.inject;

import app.packed.base.Key;
import app.packed.base.Named;
import app.packed.component.App;
import app.packed.cube.BaseBundle;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceWirelets;

/**
 *
 */
public class FooBar extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(NeedsString.class);
        provideInstance(123L);

        link(new Child(), ServiceWirelets.from(e -> {
            System.out.println("Exporting " + e.keys());
            e.rekey(Key.of(String.class), new Key<@Named("foo") String>() {});
            System.out.println("Exporting " + e.keys());
            e.decorate(Runnable.class, x -> new Runnable() {

                @Override
                public void run() {
                    System.out.println("Before");
                    x.run();
                    System.out.println("After");
                }
            });
        }));
        export(Runnable.class);
    }

    public static class NeedsString {
        public NeedsString(ChildServ string, @Named("foo") String s) {
            System.out.println("GOt " + string);
        }
    }

    public static void main(String[] args) {
        System.out.println(ServiceContract.of(new Child()));
        App a = App.of(new FooBar());
        a.use(Runnable.class).run();
    }

    public static class Child extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            provide(ChildServ.class).export();
            installInstance("asdasd").exportAs(new Key<@Named("Fooo") String>() {});
            installInstance("asdasd").exportAs(new Key<@Named("dFooo") String>() {});
            provideInstance(new Runnable() {
                @Override
                public void run() {
                    System.out.println("HMM");
                }
            }).as(Runnable.class).export();
        }
    }

    public static class ChildServ {

        public ChildServ(Long l) {
            System.out.println("L " + l);
        }
    }
}
