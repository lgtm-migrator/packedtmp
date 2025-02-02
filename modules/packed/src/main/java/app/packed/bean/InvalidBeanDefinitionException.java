package app.packed.bean;

/**
 * A generic exception that is thrown when An exception that is thrown if
 */
// Hmmm, a generic extension why not BeanInstallationException then. That is pretty generic
//
public class InvalidBeanDefinitionException extends BeanInstallationException {

    /** <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the specified detailed message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link Throwable#initCause}.
     *
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public InvalidBeanDefinitionException(String message) {
        super(message);

    }

    /**
     * Creates a new exception with the specified detailed message and cause.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()}method). (A{@code null} value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public InvalidBeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
