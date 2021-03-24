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
package app.packed.request;

import app.packed.application.BuildWirelets;
import app.packed.application.Main;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class HelloWorldAssembly extends BaseAssembly {

    @Override
    protected void build() {
        install(SomeComponent.class);
        install(SomeComponent.class);
        install(SomeComponent.class);
    }

    public static void main(String[] args) {
        // Job.compute()
        Main.run(new HelloWorldAssembly(), BuildWirelets.onWire(c -> {
            System.out.println(c.path() + " wired");
        }));
        System.out.println("BYE");
    }

    public static class SomeComponent {

        @Compute
        public void runMe() {
            System.out.println("HelloWorld");
        }
    }
}
