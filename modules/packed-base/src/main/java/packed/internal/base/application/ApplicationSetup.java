package packed.internal.base.application;

import static java.util.Objects.requireNonNull;

public final class ApplicationSetup {

    public final PackedApplicationDriver<?> driver;

    ApplicationSetup(PackedApplicationDriver<?> driver) {
        this.driver = requireNonNull(driver, "driver is null");
    }
}
