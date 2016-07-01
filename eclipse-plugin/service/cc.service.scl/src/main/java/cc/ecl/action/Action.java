package cc.ecl.action;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for storing action related data.
 *
 * This usually means a request, a status, and an optional result.
 *
 * ReqT must implement a sane equals and hashCode, otherwise other components will fail or provide
 * poor performance.
 */
public class Action<ReqT, ResT> {

    private final static Logger LOGGER = Logger.getLogger(Action.class.getName());

    final private ReqT request;
    // generic parameter hack
    private final TypeToken<ReqT> requestType;
    private final TypeToken<ResT> resultType;
    private ActionResult<ResT> result;

    public Action(ReqT request) {
        this.request = request;
        this.result = new ActionResult<ResT>();
        resultType = new TypeToken<ResT>(getClass()) {
        };
        requestType = new TypeToken<ReqT>(getClass()) {
        };
    }

    public Action(ReqT request, TypeToken<ReqT> requestType, TypeToken<ResT> resultType) {
        this.request = request;
        this.result = new ActionResult<ResT>();
        this.requestType = requestType;
        this.resultType = resultType;
    }

    public ReqT getRequest() {
        return request;
    }

    public ActionStatus getStatus() {
        return result.getStatus();
    }

    public Optional<ResT> getResult() {
        return result.getResult();
    }

    /**
     * Tries to run itself with the given implementation and runner.
     */
    public void runWithImpl(ActionImpl<ReqT, ResT> impl, InnerRunner innerRunner) {
        if (result.isConcreteResult()) {
            throw new RuntimeException("Can't rerun a finished action");
        }
        try {
            ActionResult<ResT> tmp = impl.doRun(this, innerRunner);

            if (tmp.betterThan(result)) {
                result = tmp;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception while running action, treating as " +
                    "COMMUNICATION_ERROR", e);
            result = new ActionResult<ResT>(ActionStatus.COMMUNICATION_ERROR);
        }

    }

    public ActionParameterInfo getParameterInfo() {
        return new ActionParameterInfo(requestType, resultType);
    }

    /**
     * @see ActionResult::isConcreteResult
     */
    public boolean isConcreteResult() {
        return result.isConcreteResult();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) {
            return false;
        }
        Action oth = (Action) obj;
        return request.equals(oth.getRequest());
    }

    @Override
    public int hashCode() {
        return request.hashCode();
    }

    @Override
    public String toString() {
        return "<<ACTION: " + request.toString() + ">>[" + getStatus() + "]";
    }
}
