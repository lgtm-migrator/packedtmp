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
package app.packed.container.extension;

/**
 * An extension thrown to indicate that some part of an an extension was not properly implemented according to the
 * extension requirements in packed. As a user of the extension there is normally nothing else you can do, but report
 * the error to the developer of the extension.
 */
public class ExtensionDeclarationException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public ExtensionDeclarationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ExtensionDeclarationException(String message, Throwable cause) {
        super(message, cause);
    }
}
