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
import app.packed.component.ComponentAnalyzer;
import app.packed.container.BaseBundle;
import app.packed.container.Extension;

/**
 *
 */
public class Zzz extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        service();
        use(MyEx.class);
    }

    public static void main(String[] args) {
        App.of(new Z4());
        ComponentAnalyzer.stream(new Zzz()).forEach(c -> {
            System.out.println(c.path() + "  " + c.modifiers() + "  " + c.attributes());
        });
    }

    static class MyEx extends Extension {

    }
}
