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

import java.util.Optional;

import app.packed.lifetime.LifetimeMirror;
import app.packed.lifetime.RunState;
import app.packed.operation.OperationMirror;

/**
 * An operation that is invoked doing lifecycle events on the bean.
 */
// Mit eneste problem er om vi fx har operationer der baade kan kalde paa flere tidspunkter??
// OnInitialize, OnStart (Hvor tit sker det... Laver vi ikke bare flere operationer saa)
// 
public class BeanLifecycleOperationMirror extends OperationMirror {

    /**
     * The lifetime the operation is run in.
     * 
     * @return
     */
    public LifetimeMirror lifetime() {
        throw new UnsupportedOperationException();
    }

    /** {@return the lifetime operation this operation is a part of.} */

    // IDK, supportere vi Lifecycle events there ikke har en Lifetime operation???
    // Saa er det ikke en lifetime. Fx restart
    public OperationMirror lifetimeOperation() {
        throw new UnsupportedOperationException();
    }

    // Previous on bean?
    // Previous on Lifetime
    // Maybe better to leave out
    Optional<BeanLifecycleOperationMirror> previous() {
        return Optional.empty();
    }

    public RunState runState() {
        return RunState.INITIALIZED;
    }

    public String stage() {
        // Maaske har vi en mere finindeling her end i runState()
        // Saa vi fx ogsaa kan sige <<instantiate>>
        return "<<instantiate>>";
    }

    /**
     * A mirror for an operation that creates a new instance of a bean.
     * <p>
     * The operator of this operation is always {@link BeanExtension}.
     */
    // IDK know if we want this
    public static class BeanInstantiationOperationMirror extends BeanLifecycleOperationMirror {}

    // Hvis jeg register en instance har min bean ikke en
    // Men factory og non-static class har altid en
    // En void eller static bean har aldrig en

    // Operatoren er vel altid operateren af lifetimen?
    // Hmm hvad med @Conf <--- Her er operatoren vel ConfigExtension
    // det betyder at operatoren maa vaere BeanExtension hvilket vel er aligned
    // med @OnInitialize
}
