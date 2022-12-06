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
package app.packed.context;

import app.packed.container.ContainerExtension;
import internal.app.packed.lifetime.PackedExtensionContext;

/**
 * All extensions operate within an ExtensionContext...
 */

// The whole service layer for extensions are provided via ExtensionContext...
// Vi har en per container?

public sealed interface ExtensionContext extends Context<ContainerExtension> permits PackedExtensionContext {}
