package app.packed.bean;

// Maybe BeanKind instead
public enum BeanKind {

    EXTENSION,
    /** Lives and dies with the application. */
    APPLICATION,
    
    // Instantiated by an extensions that
    // A single ideally operates within it
    REQUEST,

    // Instantiated and deconstructed by an extension and some point
    MANAGED,

    /** Once an instance of the bean has been initialized, Packed (or the extension) maintains no reference to it. */
    UNMANAGED;
}
// Scoped vs unscoped