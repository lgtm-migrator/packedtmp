package app.packed.inject;

import java.util.Set;

import app.packed.base.Key;
import app.packed.component.ComponentMirror;
import app.packed.mirror.Mirror;

// extends ComponentFeature???
public interface ServiceMirror extends Mirror {

    /** {@return the component the service belongs to.} */
    ComponentMirror component();

    Object configSite(); //or ConfigMirror...

    Set<ServiceMirror> dependencies();

    /** {@return the key of the service.} */
    Key<?> key();
    
    // Taenker 
    Set<InjectionSiteMirror> usedBy();
    
    Set<ComponentMirror> usedByComponents();
    
    // export()
    int id(); // Ideen var at kunne sammenligne services, der blot var exporteret...
}
// Skal vi have et id???


// Noget med injection
// Noget om den er cached
// Noget om hvor den kommer fra
// Altsaa der kan jo vare en chain af exports
// Dependencies som ikke er for samme container
// Depth +1 -> "imports"