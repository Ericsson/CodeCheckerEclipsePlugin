package cc.codechecker.api.job.report.list;

import cc.codechecker.api.action.result.count.SearchCountAction;
import cc.codechecker.api.action.result.count.SearchCountRequest;
import cc.codechecker.api.action.result.list.SearchAction;
import cc.ecl.action.Action;
import cc.ecl.job.AbstractJob;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Requires actions:
 *
 * * ConfigProblemLimit * SearchCountAction * SearchAction
 */
public class SearchJob extends AbstractJob<SearchJob, SearchListener> {

    final static int REQ_SIZE = 20;

    private final SearchRequest request;

    private final SearchList result;

    public SearchJob(int priority, Optional<Instant> deadline, SearchRequest request) {
        super(priority, deadline);
        this.request = request;
        this.result = new SearchList(request);
    }

    @Override
    protected List<ActionRequest> startActions() {
        return Arrays.asList(new ActionRequest(new SearchCountAction(new SearchCountRequest
                (request.getServer(), request.getId(), request.getFilters())), 2), new
                ActionRequest(new SearchAction(new cc.codechecker.api.action.result.list
                .SearchRequest(request.getServer(), request.getId(), 0, REQ_SIZE, request
                .getFilters())), 1));
    }

    @Override
    protected List<ActionRequest> processActionResult(Action completedAction) {
        failIfError(completedAction);

        if (completedAction instanceof SearchCountAction) {
            SearchCountAction rca = (SearchCountAction) completedAction;

            long requestsNeeded = (long) Math.ceil(rca.getResult().get().getCount() / (float)
                    REQ_SIZE) - 1;

            LinkedList<ActionRequest> requests = new LinkedList<>();

            for (int i = (int) requestsNeeded; i >= 1; i--) {
                requests.add(new ActionRequest(new SearchAction(new cc.codechecker.api.action
                        .result.list.SearchRequest(request.getServer(), request.getId(), i *
                        REQ_SIZE, (i + 1) * REQ_SIZE, request.getFilters()))));
            }

            // note: total count reported by the server is currently buggy :(
            result.setTotalReportCount((int) rca.getResult().get().getCount());


            for (SearchListener listener : listeners) {
                listener.onTotalCountAvailable(this, result, (int) rca.getResult().get().getCount
                        ());
            }

            return requests;

        }

        if (completedAction instanceof SearchAction) {
            SearchAction rla = (SearchAction) completedAction;
            result.addReports(rla.getResult().get().getRunResultList());

            for (SearchListener listener : listeners) {
                listener.onPartsArrived(this, result, rla.getResult().get().getRunResultList());
            }
        }

        return Arrays.asList();
    }

    public SearchList getResult() {
        return result;
    }
}
