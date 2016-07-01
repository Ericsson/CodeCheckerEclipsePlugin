package cc.ecl.job;

import cc.ecl.action.Action;
import cc.ecl.action.ActionCacheFilter;
import cc.ecl.action.ActionRunner;
import cc.ecl.action.ActionStatusNotifier;

import com.google.common.collect.HashMultimap;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of the JobRunner interface, uses an ActionRunner.
 */
public class SimpleJobRunner implements JobRunner, JobManager, ActionStatusNotifier {

    final ActionRunner actionRunner;

    HashMultimap<Action, Job> notifications;

    public SimpleJobRunner(ActionRunner actionRunner) {
        this.actionRunner = actionRunner;
        notifications = HashMultimap.create();
    }

    @Override
    public void run() {
        actionRunner.run();
    }

    @Override
    public void addJob(Job j) {
        j.startWithManager(this);
    }

    @Override
    public ActionCacheFilter getActionCacheFilter() {
        return actionRunner.getCacheFilter();
    }

    @Override
    public synchronized void queueAction(Job target, Action action, int priority) {
        if (Math.abs(priority) > 2) {
            throw new IllegalArgumentException("Priority must be between -2 and 2");
        }

        notifications.put(action, target);

        actionRunner.queueAction(action, target.getPriority() + priority, this);
    }

    @Override
    public void onActionCompleted(Action action) {

        for (Job j : getAndRemoveTargets(action)) {
            j.nextActions(action);
        }
    }

    protected synchronized Set<Job> getAndRemoveTargets(Action action) {
        Set<Job> targets = new HashSet<Job>(notifications.get(action));
        notifications.removeAll(action);

        return targets;
    }
}
