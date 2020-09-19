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
package packed.internal.component;

import app.packed.component.App;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;
import app.packed.inject.Provide;

/**
 *
 */
public class Zzz extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        // service();
        // provideInstance("sdasd");
        install(Foo.class);
        use(MyEx.class);
    }

    public static void main(String[] args) {
        App.of(new Zzz());
//        ComponentAnalyzer.stream(new Zzz()).forEach(c -> {
//            System.out.println(c.path() + "  " + c.modifiers() + "  " + c.attributes());
//        });
    }

    static class MyEx extends Extension {}

    public static class Foo {
        public Foo(String s1, String s2, String s3) {
            System.out.println("OK " + s1);
        }

        @Provide
        public static String foo() {
            return "asdasd";
        }
    }
}
