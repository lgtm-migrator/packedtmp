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
package app.packed.service;

import app.packed.application.BuildException;

/**
 *
 */
// hmm. Fungere jo ikke noedvendig via install

// install(F.class) <- successed
// conf.provide() <-- fails, but installation was sucessfull
public class DublicateServiceProvideException extends BuildException {

    private static final long serialVersionUID = 1L;


    public DublicateServiceProvideException(String message, Throwable cause) {
        super(message, cause);
    }

    public DublicateServiceProvideException(String message) {
        super(message);
    }
}
