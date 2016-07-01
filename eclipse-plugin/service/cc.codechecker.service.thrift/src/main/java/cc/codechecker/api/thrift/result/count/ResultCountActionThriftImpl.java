package cc.codechecker.api.thrift.result.count;

import cc.codechecker.api.action.result.ResultFilter;
import cc.codechecker.api.action.result.count.ResultCount;
import cc.codechecker.api.action.result.count.SearchCountRequest;
import cc.codechecker.api.action.run.list.ListRunsAction;
import cc.codechecker.api.action.run.list.ListRunsRequest;
import cc.codechecker.service.thrift.gen.CodeCheckerDBAccess;
import cc.codechecker.service.thrift.gen.ReportFilter;
import cc.codechecker.service.thrift.gen.Severity;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.InnerRunner;
import cc.ecl.action.thrift.ThriftActionImpl;
import cc.ecl.action.thrift.ThriftCommunicationInterface;

import org.apache.thrift.TException;

import java.util.LinkedList;
import java.util.List;

public class ResultCountActionThriftImpl extends ThriftActionImpl<SearchCountRequest,
        ResultCount, CodeCheckerDBAccess.Iface> {
    @Override
    protected String getProtocolUrlEnd(SearchCountRequest request) {
        return "codeCheckerDBAccess";
    }

    @Override
    protected ActionResult<ResultCount> runThrift(CodeCheckerDBAccess.Iface client,
                                                  Action<SearchCountRequest, ResultCount> action,
                                                  InnerRunner innerRunner) throws TException {

        SearchCountRequest req = action.getRequest();

        List<ReportFilter> filters = new LinkedList<>();
        for (ResultFilter f : req.getResultFilters()) {
            ReportFilter rf = new ReportFilter();
            rf.setFilepath(f.getFilepath().orNull());
            rf.setCheckerMsg(f.getCheckerMsg().orNull());
            rf.setSeverity(Severity.findByValue(f.getSeverity().getValue()));
            rf.setCheckerId(f.getCheckerId().orNull());
            rf.setSuppressed(f.isShowSuppressedErrors());
            filters.add(rf);
        }

        if (req.getId().isPresent()) {
            return new ActionResult<>(new ResultCount(client.getRunResultCount(req.getId().get(),
                    filters)));
        } else {
            ListRunsAction lra = new ListRunsAction(new ListRunsRequest(action.getRequest()
                    .getServer()));
            lra = innerRunner.requireResult(lra);
            return new ActionResult<>(new ResultCount(client.getRunResultCount(lra.getResult()
                    .get().getLastRun().get().getRunId(), filters)));
        }
    }

    @Override
    public ActionCommImpl<SearchCountRequest, ResultCount, ThriftCommunicationInterface> dup() {
        return new ResultCountActionThriftImpl();
    }
}
