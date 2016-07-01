package cc.ecl.job;

import cc.ecl.action.Action;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;

import org.joda.time.Instant;

import java.util.Arrays;
import java.util.List;

/**
 * Helper class, implements a job which consist of a single action.
 *
 * Subclasses basically don't have to do anything.
 */
public class SingleActionJob<ReqT, ResT, SubType extends SingleActionJob> extends
        AbstractJob<SubType, JobListener<SubType>> {

    protected final ReqT request;
    private final TypeToken<ReqT> requestType = new TypeToken<ReqT>(getClass()) {
    };
    private final TypeToken<ResT> resultType = new TypeToken<ResT>(getClass()) {
    };
    protected Optional<ResT> result;

    public SingleActionJob(ReqT request, int priority, Optional<Instant> deadline) {
        super(priority, deadline);
        this.request = request;
    }

    @Override
    protected List<ActionRequest> startActions() {
        System.out.println(requestType);
        // TODO: why is this needed? Seems like a crude hack to me
        return Arrays.asList(new ActionRequest(new Action<ReqT, ResT>(request, requestType,
                resultType), 0));
    }

    @Override
    protected List<ActionRequest> processActionResult(Action completedAction) {

        if (failIfError(completedAction)) {
            return Arrays.asList();
        }

        result = completedAction.getResult();

        return Arrays.asList();
    }

    public Optional<ResT> getResult() {
        return result;
    }
}
