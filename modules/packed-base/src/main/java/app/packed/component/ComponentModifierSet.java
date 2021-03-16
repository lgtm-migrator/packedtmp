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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import packed.internal.component.PackedComponentModifierSet;

/**
 * An immutable set of component modifiers.
 */
// Skal vi ikke bare extende AbstractSet??? Nah.. saa meget fluff.. maybe just a toSet??
public interface ComponentModifierSet extends Iterable<ComponentModifier> {

    /**
     * Returns whether or not this contains the specified modifier
     * 
     * @param modifier
     *            the modifier to test
     * @return true if this set contains the specified modifier, otherwise false
     */
    boolean contains(ComponentModifier modifier);

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINER} modifier.
     * 
     * @return true if this set contains the container modifier, otherwise false
     */
    default boolean isContainer() {
        return contains(ComponentModifier.CONTAINER);
    }

    /**
     * Returns whether or not this set contains any modifiers.
     * 
     * @return true if this set contains any modifiers, otherwise false
     */
    boolean isEmpty();

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#EXTENSION} modifier.
     * 
     * @return true if this set contains the extension modifier, otherwise false
     */
    default boolean isExtension() {
        return contains(ComponentModifier.EXTENSION);
    }

    // boolean containsAll(Collection<ComponentModifier> c);
    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINEROLD} modifier.
     * 
     * @return true if this set contains the guest modifier, otherwise false
     */
    default boolean isContainerOld() {
        return contains(ComponentModifier.CONTAINEROLD);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#IMAGE_ROOT} modifier.
     * 
     * @return true if this set contains the image modifier, otherwise false
     */
    default boolean isImage() {
        return contains(ComponentModifier.IMAGE_ROOT);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINEROLD} modifier.
     * 
     * @return true if this set contains the guest modifier, otherwise false
     */
    default boolean isSingleton() {
        return contains(ComponentModifier.CONSTANT);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#SOURCED} modifier.
     * 
     * @return true if this set contains the source modifier, otherwise false
     */
    default boolean isSource() {
        return contains(ComponentModifier.SOURCED);
    }

    /**
     * Returns whether or not this set contains the {@link ComponentModifier#CONTAINER} modifier.
     * 
     * @return true if this set contains the container modifier, otherwise false
     */
    default boolean isStateful() {
        return contains(ComponentModifier.STATEFUL);
    }

    /**
     * Returns the number of modifiers in this set.
     * 
     * @return the number of modifiers in this set
     */
    int size();

    /**
     * Returns a new array containing all of the modifiers in this set.
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this set. The caller is thus free to
     * modify the returned array.
     *
     * @return an array containing all the modifiers in this set
     */
    // toModifierArray? if we want to implement Set
    ComponentModifier[] toArray();

    /**
     * Returns a set that includes both the specified modifier and all modifiers in this set.
     * 
     * @param modifier
     *            the modifier to include in the new set
     * @return a modifier set with all modifiers in this set as well as the specified modifier
     * @implNote implementations should return this set if the specified modifier is already included in this set.
     */
    ComponentModifierSet with(ComponentModifier modifier);

    ComponentModifierSet withIf(boolean conditional, ComponentModifier modifier);

    /**
     * Returns a set that include all modifiers in this set except for the specified modifier.
     * 
     * @param modifier
     *            the modifier to exclude in the returned set
     * @return a modifier set with all modifiers in this set except for the specified modifier
     * @implNote implementations should return this set if the specified modifier is not in this set.
     */
    ComponentModifierSet without(ComponentModifier modifier);

    ComponentModifierSet withoutIf(boolean conditional, ComponentModifier modifier);

    /**
     * Returns an empty component modifier set.
     * 
     * @return an empty component modifier set
     */
    static ComponentModifierSet of() {
        return PackedComponentModifierSet.EMPTY;
    }

    /**
     * Returns an set containing the single specified modifier.
     * 
     * @return an set containing the single specified modifier
     * 
     * @see ComponentModifier#toSet()
     */
    static ComponentModifierSet of(ComponentModifier m) {
        return new PackedComponentModifierSet(m.bits());
    }

    static ComponentModifierSet of(ComponentModifier... modifiers) {
        requireNonNull(modifiers, "modifiers is null");
        if (modifiers.length == 0) {
            return of();
        }
        int b = 0;
        for (int i = 0; i < modifiers.length; i++) {
            b |= modifiers[i].bits();
        }
        return new PackedComponentModifierSet(b);
    }

    static ComponentModifierSet of(ComponentModifier m1, ComponentModifier m2) {
        return new PackedComponentModifierSet(m1.bits() | m2.bits());
    }

    static ComponentModifierSet of(ComponentModifier m1, ComponentModifier m2, ComponentModifier m3) {
        return new PackedComponentModifierSet(m1.bits() | m2.bits() | m3.bits());
    }
}

// @apiNote this interface does not have an isEmpty method as we have yet to find any components without modifiers
// Altsaa 
//boolean isEmpty();
