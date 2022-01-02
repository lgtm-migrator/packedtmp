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
package app.packed.bean;

import packed.internal.bean.PackedBeanHandle;

/**
 *
 */
// BeanBuilder, BeanRegistrant
@SuppressWarnings("rawtypes")
public sealed interface BeanHandle<T> permits PackedBeanHandle {

    // Taenker den foerst bliver commitet naar man laver en configuration???

    default void bindOperationMirror() {
        // bind(EntityMirror.class);
        // Mulighederne er uendelige, og
    }

    void prototype();
    
    /**
     * @param <S>
     *            the type of bean
     * @param configuration
     *            the configuration
     * @return the specified configuration
     * @throws IllegalArgumentException
     *             if the specified configuration has previously been used for building stuff
     */
    <S extends BeanConfiguration<T>> S build(S configuration);
}
/// set properties
/// Bind operation (Eller er det hooks???)
/// make method handles, or runtime factories (maybe after build)
/// 

// Inject BeanManager<T>
// MH(ExtensionContext, )