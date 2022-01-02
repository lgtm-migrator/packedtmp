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

import app.packed.component.UserOrExtension;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.extension.ExtensionSupport;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanHandle;

public class BeanSupport extends ExtensionSupport {
    final UserOrExtension agent;
    final BeanExtension2 extension;

    BeanSupport(BeanExtension2 extension, UserOrExtension agent) {
        this.extension = extension;
        this.agent = agent;
    }

    public final <T> ExtensionBeanConfiguration<T> install(Class<?> implementation) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> install(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public final <T> ExtensionBeanConfiguration<T> installInstance(Object instance) {
        throw new UnsupportedOperationException();
    }
    
    // ***********************  ***********************
    // Agent must have a direct dependency on the class that uses the support class (maybe transitive is okay)
    public final <T> BeanHandle<T> register(UserOrExtension agent, Class<T> implementation) {
        return PackedBeanHandle.ofFactory(extension.container, agent, implementation);
    }
    
    public final <T> BeanHandle<T> register(UserOrExtension agent, Factory<T> factory) {
        return PackedBeanHandle.ofFactory(extension.container, agent, factory);
    }
    
    public final <T> BeanHandle<T> registerInstance(UserOrExtension agent, T instance) {
        return PackedBeanHandle.ofInstance(extension.container, agent, instance);
    }
    
    public final <T> BeanHandle<T> registerSynthetic(UserOrExtension agent) {
        throw new UnsupportedOperationException();
    }
}

//public class BeanSupport2 extends ExtensionSupport {
//    final UserOrExtension agent;
//    final BeanExtension2 extension;
//
//    BeanSupport2(BeanExtension2 extension, UserOrExtension agent) {
//        this.extension = extension;
//        this.agent = agent;
//    }
//
//    // Beans where the owner is itself
//
//    public final MethodHandle accessor(ContainerBeanConfiguration<?> configuration) {
//        throw new UnsupportedOperationException();
//    }
//
//    public final <T> ContainerBeanConfiguration<T> install(Class<T> implementation) {
//        requireNonNull(implementation, "implementation is null");
//        return extension.wire(new ContainerBeanConfiguration<>(), agent, implementation);
//    }
//
//    // Must have been created by this subtension
//    // (ExtensionContext) -> Object
//    public final MethodHandle newInstance(UnmanagedBeanConfiguration<?> configuration) {
//        return newInstanceBuilder(configuration).build();
//    }
//
//    public final MethodHandleBuilder newInstanceBuilder(UnmanagedBeanConfiguration<?> configuration) {
//        throw new UnsupportedOperationException();
//    }
//
//    public final MethodHandle processor(ManagedBeanConfiguration<?> configuration) {
//        throw new UnsupportedOperationException();
//    }
//
//    public final <T> BeanHandle<T> register(UserOrExtension agent, Class<T> implementation) {
//        requireNonNull(implementation, "implementation is null");
//        throw new UnsupportedOperationException();
//    }
//    
//    public final <T, B extends BeanConfiguration<T>> B register(UserOrExtension agent, BeanDriver driver, B configuration, Class<T> implementation) {
//        requireNonNull(implementation, "implementation is null");
//        extension.wire(configuration, agent, implementation);
//        throw new UnsupportedOperationException();
//    }
//
//    public final <T, B extends BeanConfiguration<T>> B registerChild(UserOrExtension agent, ComponentConfiguration parent, BeanDriver driver, B configuration,
//            Class<T> implementation) {
//        throw new UnsupportedOperationException();
//    }
//}