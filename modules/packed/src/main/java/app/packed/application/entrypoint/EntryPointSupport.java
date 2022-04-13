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
package app.packed.application.entrypoint;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.application.BuildException;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionSupport;
import app.packed.extension.ExtensionSupportContext;
import app.packed.extension.InternalExtensionException;

/**
 *
 */
@ExtensionMember(EntryPointExtension.class)
public class EntryPointSupport extends ExtensionSupport {

    /** The extension using the this class. */
    private final ExtensionSupportContext context;

    /** The entry point extension. */
    private final EntryPointExtension extension;

    EntryPointSupport(EntryPointExtension extension, ExtensionSupportContext context) {
        this.extension = requireNonNull(extension);
        this.context = requireNonNull(context);
    }

    /**
     * {@return the extension that is managing the
     */
    public Optional<Class<? extends Extension<?>>> managedBy() {
        return Optional.ofNullable(extension.shared().takeOver);
    }

    /**
     * @param beanOperation
     * @return the entry point id
     * 
     * @throws
     * @throws BuildException
     *             if another extension is already managing end points
     */
    // BuildException -> Altsaa tit er jo fordi brugeren har brugt annoteringer
    // for 2 forskellige extensions

    // return mirror?
    
    public int registerEntryPoint(boolean isMain) {
        return extension.registerEntryPoint(context.extensionType(), isMain);
    }

    /**
     * Selects
     * 
     * <p>
     * If an extension that is not the managing extension. Attempts to have an instance of this interface injected. The
     * build will fail with an {@link InternalExtensionException}.
     */
    // Behoever kun blive brugt hvis man har mere end et EntryPoint
    // Maaske tager man evt. bare det foerste entry point som default
    // hvis der ikke blive sat noget

    // @AutoService
    // Kan injectes i enhver bean som er owner = managedBy...
    // For andre beans smider man InjectionException?
    
    public interface EntryPointSelector {

        /**
         * @param id
         *            the id of the entry point that should be invoked
         * @throws IllegalArgumentException
         *             if no entry point with the specified id exists
         * @throws IllegalStateException
         *             if the method is invoked more than once
         * @see EntryPointMirror#id()
         */
        void selectEntryPoint(int id);
    }
    
    // Her vender vi den om... og bruger ExtensionSupport#registerExtensionPoint
    public interface EntryPointExtensionPoint {
        int entryPoint();
    }
}
// Ideen er at man kan wrappe sin entrypoint wirelet..
// Eller hva...
// Du faar CLI.wirelet ind som kan noget med sine hooks
//static Wirelet wrap(Wirelet w) {
//  return w;
//}
