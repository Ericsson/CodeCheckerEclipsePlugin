package cc.ecl.action;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a simple action cache, only removing items when requested by a filter
 *
 * @todo size checks, remove rarely needed elements
 */
public class SimpleActionCache implements ActionCache {

    Map<Action, Action> cache;

    public SimpleActionCache() {
        cache = new HashMap<Action, Action>();
    }

    @Override
    public Optional<Action> get(Action a) {
        Action aa = cache.get(a);
        if (aa == null) {
            return Optional.absent();
        } else {
            return Optional.of(aa);
        }
    }

    @Override
    public void add(Action action) {
        if (!action.isConcreteResult()) {
            throw new IllegalArgumentException("Can only cache concrete result");
        }

        cache.put(action, action);
    }

    @Override
    public void remove(ActionParameterInfo actionType) {
        ActionSimplePredicate f = new ActionSimplePredicate(actionType);
        HashMap<Action, Action> newCache = new HashMap<>();
        for (Action a : cache.values()) {
            if (!f.apply(a)) {
                newCache.put(a, a);
            }
        }
        cache = newCache;
    }

    @Override
    public <ReqT, ResT> void remove(ActionParameterInfo actionType, ActionFilter<ReqT, ResT>
            filter) {
        ActionFilteredPredicate f = new ActionFilteredPredicate(actionType, filter);
        HashMap<Action, Action> newCache = new HashMap<>();
        for (Action a : cache.values()) {
            if (!f.apply(a)) {
                newCache.put(a, a);
            }
        }
        cache = newCache;
    }

    @Override
    public void removeAll() {
        cache.clear();
    }

    /**
     * Handles removal of a given action type
     */
    protected static class ActionSimplePredicate implements Predicate<Action> {

        private ActionParameterInfo filter;

        public ActionSimplePredicate(ActionParameterInfo filter) {
            this.filter = filter;
        }

        @Override
        public boolean apply(Action action) {
            return action.getParameterInfo().equals(filter);
        }
    }

    /**
     * Handles removal of a given action type with a predicate object.
     */
    protected static class ActionFilteredPredicate implements Predicate<Action> {

        private ActionParameterInfo parameterFilter;
        private ActionFilter filter;

        public ActionFilteredPredicate(ActionParameterInfo parameterFilter, ActionFilter filter) {
            this.parameterFilter = parameterFilter;
            this.filter = filter;
        }

        @Override
        public boolean apply(Action action) {
            //noinspection unchecked
            return action.getParameterInfo().equals(parameterFilter) && filter.removeAction(action);
        }

    }
}
