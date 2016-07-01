package cc.codechecker.api.thrift.result.list;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.file.info.FileInfoAction;
import cc.codechecker.api.action.file.info.FileInfoRequest;
import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.action.result.ResultFilter;
import cc.codechecker.api.action.result.list.ResultList;
import cc.codechecker.api.action.result.list.SearchRequest;
import cc.codechecker.api.action.run.list.ListRunsRequest;
import cc.codechecker.api.action.run.list.ListRunsAction;
import cc.codechecker.service.thrift.gen.*;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.ActionStatus;
import cc.ecl.action.InnerRunner;
import cc.ecl.action.thrift.ThriftActionImpl;
import cc.ecl.action.thrift.ThriftCommunicationInterface;

import com.google.common.collect.ImmutableList;

import org.apache.thrift.TException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ResultListActionThriftImpl extends ThriftActionImpl<SearchRequest, ResultList,
        CodeCheckerDBAccess.Iface> {
    @Override
    protected String getProtocolUrlEnd(SearchRequest request) {
        return "codeCheckerDBAccess";
    }

    @Override
    protected ActionResult<ResultList> runThrift(CodeCheckerDBAccess.Iface client,
                                                 Action<SearchRequest, ResultList> action,
                                                 InnerRunner innerRunner) throws TException {

        SearchRequest req = action.getRequest();

        List<SortMode> sortType = Arrays.asList(new SortMode(SortType.CHECKER_NAME, Order.ASC),
                new SortMode(SortType.FILENAME, Order.ASC)/*, new SortMode(SortType.SEVERITY,
                Order.DESC)*/);

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

        List<ReportData> res;
        if (req.getId().isPresent()) {
            res = client.getRunResults(req.getId().get(), req.getEndId() - req.getBeginId(), req
                    .getBeginId(), sortType, filters);
        } else {
            ListRunsAction lra = new ListRunsAction(new ListRunsRequest(action.getRequest()
                    .getServer()));
            lra = innerRunner.requireResult(lra);
            res = client.getRunResults(lra.getResult().get().getLastRun().get().getRunId(), req
                    .getEndId() - req.getBeginId(), req.getBeginId(), sortType, filters);
        }

        ImmutableList.Builder<ReportInfo> builder = new ImmutableList.Builder<>();

        for (ReportData rd : res) {
            FileInfoAction fiaResult = new FileInfoAction(new FileInfoRequest(action.getRequest()
                    .getServer(), rd.getFileId()));
            fiaResult = innerRunner.requireResult(fiaResult);
            if (fiaResult.getStatus() != ActionStatus.SUCCEEDED) {
                throw new RuntimeException("Bad status for inner action: " + fiaResult);
            }

            FileInfoAction fiaBug = new FileInfoAction(new FileInfoRequest(action.getRequest()
                    .getServer(), rd.getLastBugPosition().getFileId()));
            fiaBug = innerRunner.requireResult(fiaBug);
            if (fiaBug.getStatus() != ActionStatus.SUCCEEDED) {
                throw new RuntimeException("Bad status for inner action: " + fiaBug);
            }

            BugPathItem lastBugItem = new BugPathItem(new BugPathItem.Position(rd
                    .getLastBugPosition().getStartLine(), rd.getLastBugPosition().getStartCol()),
                    new BugPathItem.Position(rd.getLastBugPosition().getEndLine(), rd
                            .getLastBugPosition().getEndCol()), rd.getLastBugPosition().getMsg(),
                    fiaBug.getResult().get().getFilePath());


            builder.add(new ReportInfo(rd.getCheckerId(), rd.getBugHash(), rd.getCheckedFile(),
                    rd.getCheckerMsg(), rd.getReportId(), rd.isSuppressed(), fiaResult.getResult
                    ().get().getFilePath(), lastBugItem));
        }

        return new ActionResult<>(new ResultList(builder.build()));
    }

    @Override
    public ActionCommImpl<SearchRequest, ResultList, ThriftCommunicationInterface> dup() {
        return new ResultListActionThriftImpl();
    }
}
