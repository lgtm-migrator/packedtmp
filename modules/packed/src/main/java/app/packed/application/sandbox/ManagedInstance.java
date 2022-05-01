package app.packed.application.sandbox;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import app.packed.lifecycle.LifecycleApplicationController.StopOption;
import app.packed.lifecycle.OnStart;
import app.packed.lifecycle.RunState;

// Atomic State + Failure
public interface ManagedInstance {

    /**
     * Blocks until the underlying component has reached the specified state, or the current thread is interrupted,
     * whichever happens first.
     * <p>
     * If the component has already reached or passed the specified state this method returns immediately. For example, if
     * attempting to wait on the {@link RunState#RUNNING} state and the component has already been successfully
     * terminated. This method will return immediately.
     *
     * @param state
     *            the state to wait on
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await(RunState, long, TimeUnit)
     * @see #state()
     */
    void await(RunState state) throws InterruptedException;

    /**
     * Blocks until the component has reached the requested state, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     * <p>
     * If the component has already reached or passed the specified state this method returns immediately with. For example,
     * if attempting to wait on the {@link RunState#RUNNING} state and the object has already been stopped. This method
     * will return immediately with true.
     *
     * @param state
     *            the state to wait on
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if this component is in (or has already passed) the specified state and {@code false} if the
     *         timeout elapsed before reaching the state
     * @throws InterruptedException
     *             if interrupted while waiting
     * @see #await(RunState)
     * @see #state()
     */
    boolean await(RunState state, long timeout, TimeUnit unit) throws InterruptedException;

    Optional<Throwable> failure();

    boolean isFailed();

    Set<Mode> modes();

    /**
     * Starts and awaits the component if it has not already been started.
     * <p>
     * Normally, there is no need to call this methods since most methods on the component will lazily start the component
     * whenever it is needed. For example, invoking use will automatically start the component if it has not already been
     * started by another action.
     * 
     * @see #startAsync(Object)
     */
    void start();

    default CompletableFuture<Void> startAsync() {
        return startAsync(null);
    }

    <T> CompletableFuture<T> startAsync(T result);

    /**
     * Returns the current state of the component.
     * <p>
     * Calling this method will never block the current thread.
     * 
     * @return the current state of the component
     */
    RunState state();

    /**
     * Stops the component.
     * 
     * @param options
     *            optional stop options
     * @see #stopAsync(Object, StopOption...)
     */
    void stop();

    default CompletableFuture<?> stopAsync() {
        return stopAsync(null);
    }

    // @OnStop(mode = Mode.Failed) <-- only run if failed

    // @OnStop(mode = Mode.Restarting) <-- only run if failed

    // @OnStop(modeOn = {Mode.Restarting, Mode.Failed}, modeOff = {Mode.Upgrading}) <-- only run if failed and we are
    // restarting

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     * 
     * @param <T>
     *            the type of result in case of success
     * @param result
     *            the result the completable future
     * @param options
     *            optional guest stop options
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     * @see #stop(StopOption...)
     */
    // Does not take null. use StopAsync
    <T> CompletableFuture<T> stopAsync(T result);
    
    enum Mode {
        // IDK

        CLEAN, // Should not save fx ServerSocket

        PARENT, // HMMM, ideen er lidt at man kan se om det er en selv der har udloest det. Maaske kan man injecte en mere detaljeret grund 
        
        //

        
        PERSISTING,

        RESTARTING, // (FX i dev mode)

        UPGRADING, // PAUSING (on stop, RESUMING (on start),


    }
}

class ZFleshWound {

 //   @OnStop(mode = Mode.RESTARTING)
    public void dd(Object restartContext) {
        //rc.store(ServerSocket, 80); // Altsaa sporgsmaalet om man ikke ville have en AppServerApp, hvor vi deployer en enkelt app
    }
    
    @OnStart(async = true)
    public void restart(Object restartContext) {
        //rc.storeX();
    }
}
