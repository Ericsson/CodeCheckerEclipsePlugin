package cc.ecl.action;


/**
 * Implementation of an action.
 *
 * @see AbstractActionImpl
 */
public interface ActionImpl<ReqT, ResT> {

    ActionResult<ResT> doRun(Action<ReqT, ResT> action, InnerRunner innerRunner);

    ActionParameterInfo getParameterInfo();
}
