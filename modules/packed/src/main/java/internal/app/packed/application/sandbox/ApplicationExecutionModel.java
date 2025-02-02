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
package internal.app.packed.application.sandbox;

/**
 *
 */
public enum ApplicationExecutionModel {
    
    // Application has a single entry point that will be executed
    // Whereafter it will fail
    ENTRY_POINT,
    
    // Does not have an entry point. But will run as a daemon until
    // shutdown either internally or externally
    DAEMON,
        
    STATELESS;
}

// Hvad med en CLI application, der enten starter en daemon eller printer "fooooo"

// Maa vaere entry point
// Er det mere lifetime???

// EntryPoint single (Main) vs Many (CLI)
// Daemon, fx Session
