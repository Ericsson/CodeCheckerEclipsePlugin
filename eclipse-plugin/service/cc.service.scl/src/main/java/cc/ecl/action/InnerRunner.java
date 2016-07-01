package cc.ecl.action;

/**
 * Simple interface for running an Action. Usually implemented by ActionRunners directly, but
 * bypasses the queue.
 */
public interface InnerRunner {

    /**
     * Called internally by actions if they require other in-action result.
     *
     * @param otherAction internal action request
     * @return concrete result
     */
    public <T extends Action> T requireResult(T otherAction);
}
