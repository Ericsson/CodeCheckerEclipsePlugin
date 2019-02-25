package org.codechecker.eclipse.plugin.report;

/**
 * Basic listener for job events.
 */
public interface Listener {

    /**
     * Called when the job is started.
     *
     * Always called, unless an internal error happens during initialization.
     */
    void onStart(Object obj);

    /**
     * Called after the job is finished (no actions remain, every processing is done)
     */
    void onComplete();

    /**
     * Called after the job stops with a timeout.
     *
     * More result may come in after this, because timeout only means it won't start new ones.
     */
    void onTimeout();

    /**
     * Called when an internal error happens.
     *
     * Might be called before (without) start.
     */
    void onJobInternalError(RuntimeException e);

}