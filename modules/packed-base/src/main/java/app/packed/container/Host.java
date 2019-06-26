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
 *
 */
// Host... Something that hosts app

// We should also be able to have a standalone host??
// Host
//// App1
//// App2
//// App3
// ???? But I think that would mean that Host needs lifecycle controls... Which I'm a bit reluctant to add.

// So Maybe
// Container
//// Host
interface Host {

    public static void main(AnyBundle b) {}
}
