package cc.ecl.action;

/**
 * Implementation of an action which requires a communication interface.
 *
 * @see cc.ecl.action.AbstractActionImpl
 */
public interface ActionCommImpl<ReqT, ResT, CommT> extends ActionImpl<ReqT, ResT> {
    public void setCommunicationInterface(CommT communicationInterface);

    public ActionCommImpl<ReqT, ResT, CommT> dup();
}
