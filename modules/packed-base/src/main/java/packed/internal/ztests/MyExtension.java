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
package packed.internal.ztests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.HookApplicator;
import app.packed.hook.OnHook;
import app.packed.lang.reflect.MethodOperator;
import app.packed.lang.reflect.VarOperator;

/**
 *
 */
public class MyExtension extends Extension {

    public void foo(ComponentConfiguration<?> cc, AXA val) {
        for (HookApplicator<Supplier<Object>> ra : val.rars) {
            ra.onReady(cc, MySidecar.class, (s, o) -> s.foo(o));
        }

        for (HookApplicator<Object> ra : val.methods) {
            ra.onReady(cc, MySidecar.class, (s, o) -> s.foom(o));
        }
        System.out.println("Saa godt da");
    }

    public void foo(ComponentConfiguration<?> cc, AnnotatedMethodHook<MyA> h) {
        // ignore
    }

    // /** {@inheritDoc} */
    // @Override
    // public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
    // context().putIntoInstantiationContext(context, new MySidecar());
    // }

    public static class Agg implements Hook.Builder<AXA> {
        private int sum;
        private final ArrayList<HookApplicator<Supplier<Object>>> rar = new ArrayList<>();

        private final ArrayList<HookApplicator<Object>> methods = new ArrayList<>();

        @OnHook
        public void foo(AnnotatedMethodHook<MyA> h) {
            sum += h.annotation().value();
            methods.add(h.applicator(MethodOperator.invokeOnce()));

            if (h.method().isStatic()) {
                // System.out.println(h.applyStatic(MethodOperator.invokeOnce()));
                // Runnable val = h.applyStatic(MethodOperator.runnable());
                // val.run();
            }
        }

        @OnHook
        public void foo(AnnotatedFieldHook<MyA> h) throws Throwable {
            sum += h.annotation().value();
            if (h.field().isStatic()) {
                Supplier<Object> val = h.applyStatic(VarOperator.supplier());
                System.out.println("VAL = " + val.get());
                System.out.println("VAL = " + val.get());
            }

            HookApplicator<Supplier<Object>> ra = h.applicator(VarOperator.supplier());
            rar.add(ra);
        }

        /** {@inheritDoc} */
        @Override
        public AXA build() {
            return new AXA(this);
        }
    }

    static class AXA implements Hook {
        final int val;
        final List<HookApplicator<Supplier<Object>>> rars;
        final List<HookApplicator<Object>> methods;

        public AXA(Agg agg) {
            this.val = agg.sum;
            this.rars = agg.rar;
            this.methods = agg.methods;
        }
    }

    public static class MySidecar {
        public void foo(Supplier<Object> o) {
            System.out.println("Genius : " + o.get());
            System.out.println("Genius : " + o.get());
        }

        public void foom(Object o) {
            System.out.println("Genius : " + o);
        }
    }
}
