package app.packed.container;

import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;

/**
 * A configuration object for an {@link Extensor}.
 * <p>
 * An instance of this class is returned when installing an Extensor via
 * 
 * ExtensionContext or
 * 
 * Extension.
 * 
 */
// .ServiceExtension
// .ServiceExtension
// .ServiceExtension
// .ServiceExtension

public final class ExtensorConfiguration extends ComponentConfiguration {

    /** {@inheritDoc} */
    @Override
    protected ExtensorConfiguration named(String name) {
        super.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return super.path();
    }

    // Nu skal de sgu nok hedde .ServiceExtension alligevel paa runtime
}
///// Application Level...
/// Maaske er det bare en abstract klasse man extender
/// Det eneste man kan bruge der er application level context
/// Men den maa kunne faa parents injected...

// Alternativ installere den via en static something...
// Eneste problem er de der hooks