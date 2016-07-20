package cc.ecl.action.mock;

import cc.ecl.action.*;
import cc.ecl.action.InnerRunner;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

/**
 * Sample action implementation which fails with COMM_ERR once.
 */
public class FailFirstCharActionImpl extends AbstractActionImpl<Character, Character, Object> {
	
	//Logger
	private final static Logger logger = LogManager.getLogger(FailFirstCharActionImpl.class.getName());	

    @Override
    protected ActionResult<Character> doRealRun(Action<Character, Character> action, InnerRunner
            innerRunner, Object communicationInterface) {

        // some waiting to help test timeouts
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
        	logger.log(Level.ERROR, "SERVER_SER_MSG >> " + e);
        }

        if (action.getStatus() == ActionStatus.COMMUNICATION_ERROR) {
            return new ActionResult<Character>('c');
        } else {
            return new ActionResult<Character>(ActionStatus.COMMUNICATION_ERROR);
        }
    }

    @Override
    public ActionCommImpl<Character, Character, Object> dup() {
        return new FailFirstCharActionImpl();
    }
}
