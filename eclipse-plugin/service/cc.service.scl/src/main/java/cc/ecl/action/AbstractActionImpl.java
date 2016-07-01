package cc.ecl.action;

import com.google.common.reflect.TypeToken;

/**
 * Implements generic functionality for Actions Implementations, expected by Action runners.
 *
 * Most Action Implementations should inherit from this class, and only implement the doRealRun
 * function.
 */
public abstract class AbstractActionImpl<ReqT, ResT, CommT> implements ActionCommImpl<ReqT, ResT,
        CommT> {

    // generic parameter hack
    private final TypeToken<ReqT> requestType = new TypeToken<ReqT>(getClass()) {
    };
    private final TypeToken<ResT> resultType = new TypeToken<ResT>(getClass()) {
    };
    private CommT communicationInterface;

    public AbstractActionImpl() {

    }

    @Override
    public ActionParameterInfo getParameterInfo() {
        return new ActionParameterInfo(requestType, resultType);
    }

    @Override
    public void setCommunicationInterface(CommT communicationInterface) {
        if (this.communicationInterface != null) {
            throw new RuntimeException("setCommunicationInterface called twice!");
        }
        this.communicationInterface = communicationInterface;
    }

    @Override
    public ActionResult<ResT> doRun(Action<ReqT, ResT> action, InnerRunner innerRunner) {
        if (this.communicationInterface == null) {
            throw new RuntimeException("setCommunicationInterface not called");
        }

        return doRealRun(action, innerRunner, communicationInterface);
    }

    /**
     * Runs the action.
     *
     * @param action                 request information, an action with a non-concrete result
     * @param innerRunner            internal action runner, for extreme use cases.
     * @param communicationInterface Communication interface with the implementation.
     */
    protected abstract ActionResult<ResT> doRealRun(Action<ReqT, ResT> action, InnerRunner
            innerRunner, CommT communicationInterface);
}
