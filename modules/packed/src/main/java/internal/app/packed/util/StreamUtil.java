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
package internal.app.packed.util;

import java.util.stream.Stream;

/**
 *
 */
public class StreamUtil {

    @SuppressWarnings("unchecked")
    public static <F, T extends F> Stream<T> filterAssignable(Class<T> assignableType, Stream<F> stream) {
        return (Stream<T>) stream.filter(f -> assignableType.isAssignableFrom(f.getClass()));
    }
}
