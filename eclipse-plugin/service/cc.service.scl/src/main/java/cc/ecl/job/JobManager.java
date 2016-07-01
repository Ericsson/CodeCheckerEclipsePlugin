package cc.ecl.job;

import cc.ecl.action.Action;
import cc.ecl.action.ActionCacheFilter;

/**
 * Used by jobs to queue actions and modify the cache.
 *
 * @see Job
 */
public interface JobManager {

    /**
     * Queues a new action.
     */
    public void queueAction(Job target, Action action, int priority);

    /**
     * Allows cache filtering.
     */
    public ActionCacheFilter getActionCacheFilter();
}
