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
package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import app.packed.base.Nullable;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.Builder;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.bean.BeanKind;
import app.packed.inject.Factory;
import packed.internal.container.ContainerSetup;
import packed.internal.container.PackedExtensionPointUseSite;
import packed.internal.container.RealmSetup;
import packed.internal.inject.factory.InternalFactory;

/** Implementation of BeanHandle.Builder. */
public final class PackedBeanHandleBuilder<T> implements BeanHandle.Builder<T> {

    /** The bean class, is typical void.class for functional beans. */
    final Class<?> beanClass;

    /** The container the new bean will be installed into. */
    final ContainerSetup container;

    /** The kind of bean. */
    final BeanKind kind;

    /** The operator of the bean, or {@code null} for {@link BeanExtension}. */
    @Nullable
    final PackedExtensionPointUseSite operator;

    @Nullable
    PackedExtensionPointUseSite owner;

    /** The source ({@code null}, {@link Class}, {@link InternalFactory} (cracked factory), Instance) */
    @Nullable
    public final Object source;

    /** The type of source the driver is created from. */
    public final SourceType sourceType;

    private PackedBeanHandleBuilder(@Nullable UseSite operator, BeanKind kind, ContainerSetup container, Class<?> beanType, SourceType sourceType,
            @Nullable Object source) {
        this.operator = (@Nullable PackedExtensionPointUseSite) operator;
        this.kind = requireNonNull(kind, "kind is null");
        this.container = requireNonNull(container);
        this.beanClass = requireNonNull(beanType);
        this.source = requireNonNull(source);
        this.sourceType = sourceType;
    }

    /** {@inheritDoc} */
    public Class<?> beanClass() {
        return beanClass;
    }

    /** {@inheritDoc} */
    public BeanKind beanKind() {
        return kind;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandle<T> build() {
        RealmSetup realm;
        if (owner == null) {
            realm = container.realm;
        } else {
            realm = this.owner.extension().extensionTree;
        }
        // Can we call it more than once??? Why not
        realm.wirePrepare();

        // Skal lave saa mange checks som muligt inde vi laver BeanSetup
        BeanSetup bean;
        if (owner == null) {
            bean = new BeanSetup(this, realm);
        } else {
            bean = new ExtensionBeanSetup(this, realm);
        }
        realm.wireCommit(bean);
        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @Override
    public Builder<T> ownedBy(UseSite context) {
        requireNonNull(context, "context is null");
        this.owner = (PackedExtensionPointUseSite) context;
        return this;
    }

    static BeanKind checkKind(BeanKind kind, int type) {
        return kind;
    }

    public static <T> PackedBeanHandleBuilder<T> ofClass(@Nullable UseSite operator, BeanKind kind, ContainerSetup container,
            Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return new PackedBeanHandleBuilder<>(operator, kind, container, implementation, SourceType.CLASS, implementation);
    }

    public static <T> PackedBeanHandleBuilder<T> ofFactory(@Nullable UseSite operator, BeanKind kind, ContainerSetup container,
            Factory<T> factory) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        InternalFactory<T> fac = InternalFactory.crackFactory(factory);
        return new PackedBeanHandleBuilder<>(operator, kind, container, fac.rawReturnType(), SourceType.FACTORY, fac);
    }

    public static <T> PackedBeanHandleBuilder<T> ofInstance(@Nullable UseSite operator, BeanKind kind, ContainerSetup container, T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }
        // TODO check kind
        // cannot be operation, managed or unmanaged, Functional
        return new PackedBeanHandleBuilder<>(operator, kind, container, instance.getClass(), SourceType.INSTANCE, instance);
    }

    public static PackedBeanHandleBuilder<?> ofNone(@Nullable UseSite operator, BeanKind kind, ContainerSetup container) {
        return new PackedBeanHandleBuilder<>(operator, kind, container, void.class, SourceType.NONE, null);
    }

    public enum SourceType {
        CLASS, FACTORY, INSTANCE, NONE;
    }
}
