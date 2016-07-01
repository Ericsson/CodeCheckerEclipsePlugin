package cc.ecl.job;

import cc.ecl.action.ActionCacheFilter;

/**
 * Interface available for clients to run jobs
 */
public interface JobRunner extends Runnable {

    /**
     * Adds and starts new job
     */
    void addJob(Job j);

    /**
     * Allows cache filtering.
     *
     * This method is intended for user interactions (e.g. forced list reload, clear all cache), not
     * for automatic functions!
     */
    public ActionCacheFilter getActionCacheFilter();

}
