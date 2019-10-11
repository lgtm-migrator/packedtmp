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
package packed.internal.container.extension;

import app.packed.container.extension.Extension;
import app.packed.container.extension.UseExtension;

/**
 *
 */
public class TestIt {

    public static void main(String[] args) {
        System.out.println(ExtensionDependencyValidator.dependenciesOf(MyExtension.class));

        // System.out.println(ExtensionDependencyValidator.dependenciesOf(MyExtension2.class));
    }

    @UseExtension({ MyExtension2.class, MyExtension3.class })
    public class MyExtension extends Extension {}

    @UseExtension(MyExtension3.class)
    public class MyExtension2 extends Extension {}

    @UseExtension(MyExtension4.class)
    public class MyExtension3 extends Extension {}

    @UseExtension(MyExtension.class)
    public class MyExtension4 extends Extension {}

}
