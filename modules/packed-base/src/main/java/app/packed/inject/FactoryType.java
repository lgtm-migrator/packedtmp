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
package app.packed.inject;

import java.lang.invoke.MethodType;

/**
 * Like {@link MethodType} but retains generic type information and annotations.
 */
// Factory kender ikke noget til keys
// withKey() er i virkeligheden bare en transformation
// InjectionType, FunctionType, ...
public final class FactoryType {

//    public Variable variable(int index) {
//        throw new UnsupportedOperationException();
//    }
//
//    public FactoryType changeReturnType(Variable variable) {
//        throw new UnsupportedOperationException();
//    }
//
//    public Variable returnType() {
//        throw new UnsupportedOperationException();
//    }

    MethodType methodType() {
        throw new UnsupportedOperationException();
    }
}
