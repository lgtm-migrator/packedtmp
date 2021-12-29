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
package zandbox.packed.hooks;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.inject.service.ServiceExtension;

/**
 *
 */
public class ZestMe extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Foo.class);
        //use(MyExt.class);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        ExtensionDescriptor ed = ExtensionDescriptor.of(rMyExt.class);
        App.run(new ZestMe() /* , BuildWirelets.printDebug().all() */);
        // System.out.println(ed.dependencies());
        System.out.println(System.currentTimeMillis() - start);

        // System.out.println(ExtensionDescriptor.of(MyExt.class).dependencies());
    }

    public static class Foo {

    }

    @DependsOn(extensions = ServiceExtension.class)
    public static class MyExt extends Extension {

        MyExt() {}

        /** {@inheritDoc} */
        @Override
        protected void onNew() {
            System.out.println("ADDED");
        }

        /** {@inheritDoc} */
        @Override
        protected void onClose() {
            System.out.println("Configured");
        }

    }
}
