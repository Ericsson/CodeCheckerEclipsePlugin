package cc.ecl.action;

/**
 * Simple interface for running actions.
 */
public interface ActionRunner<CommT> extends Runnable {
    void queueAction(Action a, int priority, ActionStatusNotifier notifier);

    /**
     * @todo Not really part of the public interface, it's only here for ActionRegitry
     */
    CommT getCommunicationInterface();

    /**
     * Allows the modification of the cache for higher layers (Jobs)
     *
     * @see Job
     */
    public ActionCacheFilter getCacheFilter();
}
