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
package app.packed.cli;

import app.packed.bundle.ExtensionMember;
import app.packed.component.Wirelet;

/**
 *
 */

// Kunne vaere fedt hvis man kunne gemme den...
// Maaske har vi en CLI extension der kan gemme den...
// Saa MainArgs er en Packlet??? IDK

// Tror det bliver en wirelet
// Og saa tror den autoaktive
// Saa behoever man heller ikke provide den op og ned
// @ExtensionType (Det er ikke en service....)

// Kraever vi har en cli extension...
// Ved ikke rigtig hvad den skal...
// CliExtension.addHelp();

// Lidt speciel, maaske vi godt vil bibeholder @WireletConsume...
// Saa kan vi baade faa den injected i extensionen. og brugerkode

@ExtensionMember(CliExtension.class)
public final class MainArgs extends Wirelet {

    public static MainArgs of(String... args) {
        throw new UnsupportedOperationException();
    }
}
// Er ikke en service, men en extension type