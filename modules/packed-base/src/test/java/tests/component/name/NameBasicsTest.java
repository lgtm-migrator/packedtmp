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
package tests.component.name;

import org.junit.jupiter.api.Test;

import app.packed.container.Wirelet;
import testutil.util.AbstractArtifactTest;

/**
 * Various basic test for naming of components.
 */
// These are basically container names, and not component names
public class NameBasicsTest extends AbstractArtifactTest {

    @Test
    public void basics() {
        appOf(c -> {}, Wirelet.name("Boo")).nameIs("Boo");
        appOf(c -> {}, Wirelet.name("Boo"), Wirelet.name("Goo")).nameIs("Goo");

        // Tests that getName returns wirelet name
        appOf(c -> c.getNameIs("Boo"), Wirelet.name("Boo")).nameIs("Boo");
        appOf(c -> c.getNameIs("Goo"), Wirelet.name("Boo"), Wirelet.name("Goo")).nameIs("Goo");

        appOf(c -> c.setName("Foo").getNameIs("Foo")).nameIs("Foo");
        appOf(c -> c.setName("Foo").getNameIs("Boo"), Wirelet.name("Boo")).nameIs("Boo");
        appOf(c -> c.setName("Foo").getNameIs("Goo"), Wirelet.name("Boo"), Wirelet.name("Goo")).nameIs("Goo");
    }

    /** Tests that we can use question marks in names */
    @Test
    public void questionMarks() {
        appOf(c -> {}, Wirelet.name("Boo?")).nameIs("Boo");
        appOf(c -> {}, Wirelet.name("Boo?"), Wirelet.name("Goo?")).nameIs("Goo");

        // Tests that getName returns wirelet name
        appOf(c -> c.getNameIs("Boo"), Wirelet.name("Boo?")).nameIs("Boo");
        appOf(c -> c.getNameIs("Goo"), Wirelet.name("Boo?"), Wirelet.name("Goo")).nameIs("Goo");

        appOf(c -> c.setName("Foo?").getNameIs("Foo")).nameIs("Foo");
        appOf(c -> c.setName("Foo?").getNameIs("Boo"), Wirelet.name("Boo?")).nameIs("Boo");
        appOf(c -> c.setName("Foo?").getNameIs("Goo"), Wirelet.name("Boo?"), Wirelet.name("Goo?")).nameIs("Goo");
    }

    /** Tests valid names for components. */
    @Test
    public void validNames() {
        // TODO implement

        // Foo??
        // Foo-?
        // Foo-

        // Maybe also test wirelets...
    }
}
