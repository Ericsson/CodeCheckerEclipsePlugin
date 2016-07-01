package cc.codechecker.api.job.analyze;

import cc.codechecker.api.action.analyze.AnalyzeAction;
import cc.codechecker.api.action.analyze.AnalyzeRequest;
import cc.codechecker.api.action.result.count.SearchCountAction;
import cc.codechecker.api.action.result.list.SearchAction;
import cc.codechecker.api.action.run.list.ListRunsAction;
import cc.ecl.action.Action;
import cc.ecl.job.AbstractJob;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import java.util.Arrays;
import java.util.List;

/**
 * Requires AnalyzeAction
 */
public class AnalyzeJob extends AbstractJob<AnalyzeJob, AnalyzeJobListener> {

    private final AnalyzeRequest request;

    public AnalyzeJob(int priority, Optional<Instant> deadline, AnalyzeRequest request) {
        super(priority, deadline);
        this.request = request;
    }

    @Override
    protected List<ActionRequest> startActions() {

        if (request.getFileList().isEmpty()) {
            getActionCacheFilter().remove((ListRunsAction.getStaticParameterInfo()));
            getActionCacheFilter().remove((SearchCountAction.getStaticParameterInfo()));
            getActionCacheFilter().remove((SearchAction.getStaticParameterInfo()));
        } else {
            // TODO: a more gentle solution not removing everything ...
            getActionCacheFilter().remove((ListRunsAction.getStaticParameterInfo()));
            getActionCacheFilter().remove((SearchCountAction.getStaticParameterInfo()));
            getActionCacheFilter().remove((SearchAction.getStaticParameterInfo()));
        }

        return Arrays.asList(new ActionRequest(new AnalyzeAction(request), getPriority()));
    }

    @Override
    protected List<ActionRequest> processActionResult(Action completedAction) {
        return Arrays.asList();
    }
}
