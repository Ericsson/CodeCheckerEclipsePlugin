package cc.ecl.action;

/**
 * Contains filtering methods for an action cache.
 */
public interface ActionCacheFilter {
    void remove(ActionParameterInfo actionType);

    <ReqT, ResT> void remove(ActionParameterInfo actionType, ActionCache.ActionFilter<ReqT, ResT>
            filter);

    void removeAll();
}
