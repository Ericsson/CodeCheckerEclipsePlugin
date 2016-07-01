package cc.ecl.job;

import cc.ecl.action.Action;
import cc.ecl.action.ActionStatus;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import java.util.Arrays;
import java.util.List;

/**
 * Implements
 *
 * @see Job
 */
public abstract class AbstractJob<SubType extends AbstractJob, ListenerType extends
        JobListener<SubType>> extends Job<SubType, ListenerType> {


    public AbstractJob(int priority, Optional<Instant> deadline) {
        super(priority, deadline);
    }

    protected boolean failIfError(Action completedAction) {

        // TODO: error methods maybe?

        if (completedAction.getStatus() == ActionStatus.LOGIC_ERROR) {
            return true;
        }

        return false;
    }

    @Override
    protected List<ActionRequest> preemptiveActions() {
        return Arrays.asList();
    }
}
