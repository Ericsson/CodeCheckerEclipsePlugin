package cc.ecl.job;

/**
 * Basic listener for job events.
 */
public interface JobListener<JobT extends Job> {

    /**
     * Called when the job is started.
     *
     * Always called, unless an internal error happens during initialization.
     */
    void onJobStart(JobT j);

    /**
     * Called after the job is finished (no actions remain, every processing is done)
     */
    void onJobComplete(JobT j);

    /**
     * Called after the job stops with a timeout.
     *
     * More result may come in after this, because timeout only means it won't start new ones.
     */
    void onJobTimeout(JobT j);

    /**
     * Called when an internal error happens.
     *
     * Might be called before (without) start.
     */
    void onJobInternalError(JobT j, RuntimeException e);

}
