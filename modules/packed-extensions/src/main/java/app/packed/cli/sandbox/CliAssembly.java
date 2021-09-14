package app.packed.cli.sandbox;

import java.util.function.Consumer;

import app.packed.application.programs.SomeApp;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;

// Skal vi returner int???
public abstract class CliAssembly extends BaseBundle {

    // Provides a result... men det goer main jo ikke...
    @SuppressWarnings("unused")
    private final void main(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    protected final void onMain(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    protected final void onMain(Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    public static void main(Bundle<?> assembly, String[] args, Wirelet... wirelets) {
        SomeApp.run(assembly, wirelets);
    }

    public static void main(Bundle<?> assembly, Wirelet... wirelets) {
        SomeApp.run(assembly, wirelets);
    }

    public static abstract class Op<T> {

        public Op(Consumer<T> consumer) {

        }
    }
}
