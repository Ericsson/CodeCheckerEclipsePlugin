package cc.ecl.action;

import com.google.common.base.Optional;

/**
 * Stores the result of an action try.
 */
public class ActionResult<ResT> {

    private Optional<ResT> result;
    private ActionStatus status;

    /**
     * Default constructor, creates a pending action.
     */
    ActionResult() {
        result = Optional.absent();
        status = ActionStatus.PENDING;
    }

    /**
     * Creates an ActionResult without a real result, used by error and pending states.
     */
    public ActionResult(ActionStatus status) {

        if (status == ActionStatus.SUCCEEDED) {
            throw new IllegalArgumentException("Succeeded status requires a result object!");
        }

        result = Optional.absent();
        this.status = status;
    }

    /**
     * Creats an ActionResult with a successful status and a real result
     */
    public ActionResult(ResT result) {
        this.result = Optional.of(result);
        this.status = ActionStatus.SUCCEEDED;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public Optional<ResT> getResult() {
        return result;
    }

    /**
     * Tells if our result is better than the other - e.g. we should replace the other result with
     * our.
     */
    public boolean betterThan(ActionResult<ResT> other) {

        if (other == null) {
            throw new IllegalArgumentException("Null other");
        }

        // we are always have at least a pending status
        if (other.status == ActionStatus.PENDING) return true;

        // never replace a fixed result
        if (other.isConcreteResult()) return false;

        // other.status != PENDING => it's better than our
        if (status == ActionStatus.PENDING) return false;

        // other status is COMMUNICATION_ERROR, our's is not PENDING => same or better

        return true;
    }

    public boolean isConcreteResult() {
        return (status == ActionStatus.LOGIC_ERROR || status == ActionStatus.SUCCEEDED);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ActionResult)) {
            return false;
        }

        ActionResult other = (ActionResult) obj;

        return other.getStatus().equals(status) && other.getResult().equals(result);
    }
}
