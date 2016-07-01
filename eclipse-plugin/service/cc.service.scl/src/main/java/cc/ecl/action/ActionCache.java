package cc.ecl.action;

import com.google.common.base.Optional;

/**
 * Implements minimal method required for a working cache. Allows to put and get cache elements, and
 * to invalidate them using filters.
 */
public interface ActionCache extends ActionCacheFilter {
    Optional<Action> get(Action a);

    void add(Action action);

    /**
     * Filter interface: requires a simple boolean method.
     *
     * @todo Move to ActionCacheFilter
     */
    public static interface ActionFilter<ReqT, ResT> {
        boolean removeAction(Action<ReqT, ResT> act);
    }
}
