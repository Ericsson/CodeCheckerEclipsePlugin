package cc.ecl.action.mock;

import cc.ecl.action.AbstractActionImpl;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.InnerRunner;

public class ServerAddOneActionImpl extends AbstractActionImpl<ServerIntegerRequest, Integer,
        Object> {
    @Override
    protected ActionResult<Integer> doRealRun(Action<ServerIntegerRequest, Integer> action,
                                              InnerRunner innerRunner, Object
                                                          communicationInterface) {
        return new ActionResult<>(action.getRequest().getQuery() + 1);
    }

    @Override
    public ActionCommImpl<ServerIntegerRequest, Integer, Object> dup() {
        return new ServerAddOneActionImpl();
    }
}
