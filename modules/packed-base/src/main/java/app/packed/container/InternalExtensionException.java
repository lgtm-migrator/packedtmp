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
package app.packed.container;

/**
 * An exception typically thrown by the runtime to indicate that some part of an extension was not properly implemented
 * according to the basic contract of Packed's extension mechanism. As an extension user there is normally nothing you
 * can do, except for reporting the error to the extension developer(s).
 */
//ExtensionDefinitionException
//PackletDefinitionException
//SidecarDefinitionException

// Hvis det er tilknyttet en extension saa bruger vi denne
public class InternalExtensionException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *            the message
     */
    public InternalExtensionException(String message) {
        super(message);
    }

    /**
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public InternalExtensionException(String message, Throwable cause) {
        super(message, cause);
    }
}
