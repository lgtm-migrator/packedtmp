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
package app.packed.hooks.usage;

import app.packed.build.BuildWith;
import app.packed.hooks.ContextualProvide;
import app.packed.inject.sandbox.HookBootstrap;
import app.packed.inject.variable.InjectableVariable;
import app.packed.inject.variable.InjectableVariable.VariableInjector;
import app.packed.inject.variable.Variable;
import app.packed.lifecycle.OnInitialize;

/**
 *
 */
public class ConstantExpresUsage {

    abstract class Impl1 extends InjectableVariable {

        @BuildWith
        static void foo(Variable v, VariableInjector binder) {
            Plus p = v.getAnnotation(Plus.class); // getMetaAnnotation
            binder.injectConstant(p.arg1() + p.arg2());
        }
    }

    class Impl2 {

        @BuildWith // replace with @BootstrapWith...
        public void foo(Variable v, VariableInjector binder) {
            Plus p = v.getAnnotation(Plus.class);
            binder.injectConstant(p.arg1() + p.arg2());
        }
    }

    class Impl3 {

        @ContextualProvide
        public int foo(HookBootstrap<Const> cc) {
            return cc.val().val;
        }

        class Const {
            final int val;

            Const(Variable v) {
                Plus p = v.getAnnotation(Plus.class);
                val = p.arg1() + p.arg2();
            }
        }
    }

    class Impl4 {

        @ContextualProvide
        public final int val;

        Impl4(Variable v) {
            Plus p = v.getAnnotation(Plus.class);
            val = p.arg1() + p.arg2();
        }
    }

    class Fooo {

        @OnInitialize
        void foo(@Plus(arg1 = 123, arg2 = 4545) int valc) {

        }

    }

    @InjectableVariable.Hook(bootstrap = Impl1.class)
    @interface Plus {
        int arg1();

        int arg2();
    }
}

// Vi har leget lidt med tanken omkring variables...
//interface Impl00 {
//
//  @BuildWith
//  static void foo(Plus p, VariableInjector binder) {
//      binder.injectConstant(p.arg1() + p.arg2());
//  }
//}

// Saves one line of code but...
// Nej syntes, 1'eren er altsaa bedre
//abstract class Impl0 extends InjectableVariable {
//
//    @BuildWith
//    static void foo(Plus p, VariableInjector binder) {
//        binder.injectConstant(p.arg1() + p.arg2());
//    }
//}