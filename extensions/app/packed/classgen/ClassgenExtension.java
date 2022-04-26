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
package app.packed.classgen;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles.Lookup.ClassOption;
import java.util.ArrayList;

import app.packed.component.Realm;
import app.packed.container.Extension;

/**
 *
 */
//Codegen could be source code...

// Skal vi supportere noget caching?????

// Vi supportere faktisk ikke caching paa tvaers af extensions????
// Taenker vi maa definere et eller andet statisk

// ClassgenToken.of(Lookup lookup);
// CT.source()...
// Altsaa parameteren er vel en source og en slags description???

// Maaske extension ClassgenToken()

// Problemet er f.eks. naar vi koere tests...

// Vi gider jo aergelig talt ikke generere FooRepositori i hver eneste test

// Det er vel det foerste eksempel paa global state for extensions...
public class ClassgenExtension extends Extension<ClassgenExtension> {

    // void resolveINDYsImmediatly()...

    // void disableRuntime();

    // Hvis man bruger graal er den automatiske disabled...

    final ArrayList<PackedGeneratedClass> generated = new ArrayList<>();

    @Override
    protected ClassgenExtensionMirror mirror() {
        return mirrorInitialize(new ClassgenExtensionMirror());
    }

    public Lookup defineHiddenClass(Lookup caller, byte[] bytes, boolean initialize, ClassOption... options) throws IllegalAccessException {
        return defineHiddenClass(Realm.application(), caller, bytes, initialize, options);
    }

    Lookup defineHiddenClass(Realm realm, Lookup caller, byte[] bytes, boolean initialize, ClassOption... options)
            throws IllegalAccessException {
        Lookup lookup = caller.defineHiddenClass(bytes, initialize, options);
        generated.add(new PackedGeneratedClass(realm, lookup.lookupClass()));
        return lookup;
    }

    // Or event handler API... ClassDefined
    public void addListener() {}

}
