package cc.ecl.action.mock;

import cc.ecl.action.AbstractActionImpl;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.InnerRunner;

/**
 * Created by Zsolt on 2015.02.20..
 */
public class AddOneIntegerActionImpl extends AbstractActionImpl<Integer, Integer, Object> {
    @Override
    protected ActionResult<Integer> doRealRun(Action<Integer, Integer> action, InnerRunner
            innerRunner, Object communicationInterface) {
        return new ActionResult<Integer>(action.getRequest() + 1);
    }

    @Override
    public ActionCommImpl<Integer, Integer, Object> dup() {
        return new AddOneIntegerActionImpl();
    }
}
