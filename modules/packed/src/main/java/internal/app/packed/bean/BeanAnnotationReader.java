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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;

import app.packed.bean.BeanIntrospector.AnnotationReader;

/** Implementation of {@link AnnotationReader}. */
public record BeanAnnotationReader(Annotation[] annotations) implements AnnotationReader {

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == annotationClass) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Annotation[] readAnyOf(Class<?>... annotationTypes) {
        return null;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> T readRequired(Class<T> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == annotationClass) {
                return (T) annotations[i];
            }
        }
        throw new IllegalStateException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasAnnotations() {
        return annotations.length != 0;
    }
}
