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
package internal.app.packed.service;

import java.util.Collection;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanMirror;
import app.packed.container.BaseAssembly;
import app.packed.service.ExportService;
import app.packed.service.ExportedServiceMirror;
import app.packed.service.ProvideService;
import app.packed.service.ProvideableBeanConfiguration;
import app.packed.service.ProvidedServiceMirror;
import app.packed.service.ServiceExtensionMirror;

/**
 *
 */
public class CheckCycles extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        //bean().multiInstall(A.class).provide();
        ProvideableBeanConfiguration<A> aaa = bean().multiInstall(A.class);
        aaa.provide();
        aaa.provide();
        provide(A.class);
        provide(B.class);
    }

    public static void main(String[] args) {

        ApplicationMirror am = App.newMirror(new CheckCycles());
        for (var b : am.container().beans().toList()) {
            System.out.println(b.beanClass().getSimpleName() + " " + b.factoryOperation().get().target());
        }

        Collection<ProvidedServiceMirror> c = am.use(ServiceExtensionMirror.class).provisions().values();

        Collection<ExportedServiceMirror> ex = am.use(ServiceExtensionMirror.class).exports().values();

        BeanMirror b = am.container().beans().iterator().next();

        System.out.println(b.path() + " " + b.dependencies().extensions());

        c.forEach(e -> System.out.println(e.bean().path() + " provided by " + e.key()));

        ex.forEach(e -> System.out.println(e.bean().path() + " exported by " + e.key()));
    }

    public record D() {}

    public record C() {}

    public record A(B b, BeanMirror bean) {}

    public record B() {

        @ProvideService
        String dppro() {
            return null;
        }

        @ProvideService
        @ExportService
        C ppro() {
            return null;
        }

    }
}
