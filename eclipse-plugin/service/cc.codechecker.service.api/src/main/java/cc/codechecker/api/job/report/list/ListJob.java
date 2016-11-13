package cc.codechecker.api.job.report.list;

import cc.codechecker.api.action.result.count.ListCountAction;
import cc.codechecker.api.action.result.count.ListCountRequest;
import cc.codechecker.api.action.result.list.ListAction;
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
 * * ConfigProblemLimit * ListCountAction * ListAction
 */
public class ListJob extends AbstractJob<ListJob, ListListener> {

    final static int REQ_SIZE = 20;

    private final ListRequest request;

    private final ListList result;

    public ListJob(int priority, Optional<Instant> deadline, ListRequest request) {
        super(priority, deadline);
        this.request = request;
        this.result = new ListList(request);
    }

    @Override
    protected List<ActionRequest> startActions() {
        return Arrays.asList(new ActionRequest(new ListCountAction(new ListCountRequest(request
                .getServer(), request.getFilename())), 2), new ActionRequest(new ListAction(new
                        cc.codechecker.api.action.result.list.ListRequest(request.getServer(), 0,
                                REQ_SIZE, request.getFilename())), 1));
    }

    @Override
    protected List<ActionRequest> processActionResult(Action completedAction) {
        failIfError(completedAction);

        if (completedAction instanceof ListCountAction) {
            ListCountAction rca = (ListCountAction) completedAction;

            long requestsNeeded = (long) Math.ceil(rca.getResult().get().getCount() / (float)
                    REQ_SIZE) - 1;

            LinkedList<ActionRequest> requests = new LinkedList<>();

            for (int i = (int) requestsNeeded; i >= 1; i--) {
                requests.add(new ActionRequest(new ListAction(new cc.codechecker.api.action
                        .result.list.ListRequest(request.getServer(), i * REQ_SIZE, (i + 1) *
                                REQ_SIZE, request.getFilename()))));
            }

            // note: total count reported by the server is currently buggy :(
            result.setTotalReportCount((int) rca.getResult().get().getCount());


            for (ListListener listener : listeners) {
                listener.onTotalCountAvailable(this, result, (int) rca.getResult().get().getCount
                        ());
            }

            return requests;

        }

        if (completedAction instanceof ListAction) {
            ListAction rla = (ListAction) completedAction;
            result.addReports(rla.getResult().get().getRunResultList());

            for (ListListener listener : listeners) {
                listener.onPartsArrived(this, result, rla.getResult().get().getRunResultList());
            }
        }

        return Arrays.asList();
    }

    public ListList getResult() {
        return result;
    }
}
