package cc.ecl.job;

import cc.ecl.action.Action;
import cc.ecl.action.ActionCache;
import cc.ecl.action.ActionCacheFilter;
import cc.ecl.action.ActionParameterInfo;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Simple representation of a job: something consisting of actions, with a possible deadline, no
 * other requirement.
 *
 * Deadline is a hard time limit: it means there's no more need for the job, so it fails.
 *
 * Subclasses can provide their own listener interface. Note that methods in the basic JobListener
 * are handled by this class.
 */
public abstract class Job<SubType extends Job, ListenerType extends JobListener<SubType>> {

    protected final Optional<Instant> deadline;
    protected final Set<ListenerType> listeners;
    private final int priority;
    ActionCacheFilterStore innerCacheFilter;
    private JobManager jobManager;
    private Status status;
    private volatile int ongoingActions;

    public Job(int priority, Optional<Instant> deadline) {
        this.priority = priority;
        this.deadline = deadline;
        this.status = Status.PENDING;
        listeners = new HashSet<>();
    }

    /**
     * Handles timeout / other errors / completion, and queues new actions in the queue.
     *
     * @see Job::processActionResult
     */
    public void nextActions(Action completedAction) {

        if (status == Status.INTERNAL_ERROR) { // already failed
            return;
        }

        ongoingActions--;

        try {
            List<ActionRequest> req = processActionResult(completedAction);


            if (deadline.isPresent() && deadline.get().isBefore(new Instant())) {
                // we are after the deadline, the job got cancelled, doing nothing more
                if (ongoingActions == 0 && req.isEmpty()) {
                    completeJob();
                } else {
                    timeoutJob();
                }
                return;
            }

            for (ActionRequest rq : req) {
                jobManager.queueAction(this, rq.action, rq.priority);
                ongoingActions++;
            }

            if (ongoingActions == 0) {
                completeJob();
            }

        } catch (RuntimeException e) {
            internalError(e);
        }
    }

    /**
     * @todo Not really public, used internally!
     */
    public void startWithManager(JobManager manager) {
        if (jobManager != null) {
            throw new RuntimeException("Job already started");
        }
        jobManager = manager;

        if (innerCacheFilter != null) {
            if (innerCacheFilter.removeAll) {
                manager.getActionCacheFilter().removeAll();
            } else {
                for (ActionCacheFilterStore.Item i : innerCacheFilter.items) {
                    if (i.filter != null) {
                        manager.getActionCacheFilter().remove(i.actionType, i.filter);
                    } else {
                        manager.getActionCacheFilter().remove(i.actionType);
                    }
                }
            }
        }

        try {
            for (ActionRequest rq : startActions()) {
                System.out.println(rq.action.getParameterInfo());
                jobManager.queueAction(this, rq.action, rq.priority);
                ongoingActions++;
            }
        } catch (RuntimeException e) {
            internalError(e);

            return;
        }

        for (JobListener listener : listeners) {
            listener.onJobStart(this);
        }
    }

    public Status getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * Returns the list of actions required when the job is startd.
     */
    protected abstract List<ActionRequest> startActions();

    /**
     * Method to process a finished action and request more.
     */
    protected abstract List<ActionRequest> processActionResult(Action completedAction);

    // called after job is completed. Doesn't count toward completing, but can help likely next
    // actions
    protected abstract List<ActionRequest> preemptiveActions();

    private void completeJob() {
        status = Status.COMPLETED;

        for (JobListener listener : listeners) {
            listener.onJobComplete(this);
        }

        for (ActionRequest rq : preemptiveActions()) {
            jobManager.queueAction(this, rq.action, rq.priority);
            ongoingActions++;
        }
    }

    private void timeoutJob() {
        status = Status.TIMEOUT;

        for (JobListener listener : listeners) {
            listener.onJobTimeout(this);
        }
    }

    private void internalError(RuntimeException e) {
        status = Status.INTERNAL_ERROR;

        for (JobListener listener : listeners) {
            listener.onJobInternalError(this, e);
        }
    }

    /**
     * Adds a new listener to be notified by job events.
     */
    public void addListener(ListenerType listener) {
        listeners.add(listener);
    }

    /**
     * For filtering the cache.
     */
    protected ActionCacheFilter getActionCacheFilter() {
        if (innerCacheFilter == null) {
            innerCacheFilter = new ActionCacheFilterStore();
        }

        return innerCacheFilter;
    }

    /**
     * Job status options
     */
    enum Status {
        PENDING, COMPLETED, TIMEOUT, INTERNAL_ERROR
    }

    /**
     * Wrapper class for filters.
     *
     * CommunicationInterface (required for getting the real ActionCacheFilter) is only set by the
     * JobRunner, but cache filtering can, and usually is requested before that momemt. This class
     * stores the filters until the real object is available.
     */
    static class ActionCacheFilterStore implements ActionCacheFilter {

        boolean removeAll;
        LinkedList<Item> items;

        public ActionCacheFilterStore() {
            items = new LinkedList<>();
            removeAll = false;
        }

        @Override
        public void remove(ActionParameterInfo actionType) {
            items.add(new Item(actionType, null));
        }

        @Override
        public <ReqT, ResT> void remove(ActionParameterInfo actionType, ActionCache
                .ActionFilter<ReqT, ResT> filter) {
            items.add(new Item(actionType, filter));
        }

        @Override
        public void removeAll() {
            removeAll = true;
        }

        static class Item {
            ActionParameterInfo actionType;
            ActionCache.ActionFilter filter;

            public Item(ActionParameterInfo actionType, ActionCache.ActionFilter filter) {
                this.actionType = actionType;
                this.filter = filter;
            }
        }
    }

    /**
     * Jobs can require prioritized actions, optionally with a disabled cache.
     *
     * @todo Disabled cache isn't used by SimpleActionRunner!
     */
    public static class ActionRequest {
        Action action;
        int priority; // +-2
        boolean disableCache;

        public ActionRequest(Action action) {
            this.action = action;
            this.priority = 0;
        }

        public ActionRequest(Action action, int priority) {
            this.action = action;
            this.priority = priority;
            this.disableCache = false;
        }

        public ActionRequest(Action action, int priority, boolean disableCache) {
            this.action = action;
            this.priority = priority;
            this.disableCache = disableCache;
        }
    }
}
