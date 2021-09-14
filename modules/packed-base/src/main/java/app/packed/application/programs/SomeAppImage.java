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
package app.packed.application.programs;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.base.Completion;
import app.packed.build.BuildException;
import app.packed.bundle.BaseBundle;
import app.packed.bundle.Bundle;
import app.packed.bundle.Wirelet;

/**
 * A specialization of {@link ApplicationImage} that is targeted use from the main method of a Java program. This is
 * typically used for running GraalVM native image.
 * 
 * @see SomeApp
 */
// Optimized for running once

// Kan have en masse specielle metoder... Som kun er targeted main 

// @UseSiteIgnoreForStackWalks (ClassValue er O(1) i fremtidige versioner)
// Ideen er lidt at kunne annotere allerede klasser som ikke er direkte Assembly bruger klasser...
// IDK

// Typically used for CLI applications
public /* primitive */ final class SomeAppImage {

    /** The image we are wrapping. */
    private final ApplicationImage<Completion> image;

    /**
     * Creates a new main image.
     * 
     * @param assembly
     *            the assembly
     * @param wirelets
     *            optional wirelets
     */
    private SomeAppImage(Bundle<?> assembly, Wirelet... wirelets) {
        this.image = SomeApp.driver().imageOf(assembly, wirelets);
    }

    /**
     * A helper method that makes it easier to provide command-line arguments to your program. Is typically used from a
     * program's main method:
     * 
     * <pre> {@code
     * private final static Image<Void> MAIN = Main.imageOf(new SomeAssembly());
     *
     * public static void main(String[] args) {
     *   MAIN.use(args, any additional wirelets...);
     * }}</pre>
     * <p>
     * Invoking this method is identical to invoking
     * {@code image.use(assembly, Wirelet.combine(MainArgs.of(args), wirelets))}.
     * 
     * @param args
     *            command line arguments
     * @param wirelets
     *            optional wirelets
     * @return the result of using the image
     * @throws IllegalStateException
     *             if the image has already been used
     */
    public void use(String[] args, Wirelet... wirelets) {
        image.use(/* CliWirelets.args(args).andThen( */ wirelets);
    }

    public void use(Wirelet... wirelets) {
        image.use(wirelets);
    }

    /**
     * Creates a new main image from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly to use for building the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     * @throws BuildException
     *             if the image could not be build
     * @see ApplicationDriver#imageOf(Bundle, Wirelet...)
     */
    public static SomeAppImage of(Bundle<?> assembly, Wirelet... wirelets) {
        return new SomeAppImage(assembly, wirelets);
    }
}

class MyAppMain extends BaseBundle {

    private static final SomeAppImage MAIN = SomeAppImage.of(new MyAppMain());

    @Override
    protected void build() {
        installInstance("HelloWorld");
    }

    public static void main(String[] args) {
        MAIN.use(args);
    }
}