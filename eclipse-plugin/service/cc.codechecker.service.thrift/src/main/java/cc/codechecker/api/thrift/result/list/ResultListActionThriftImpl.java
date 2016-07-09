package cc.codechecker.api.thrift.result.list;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.bug.path.ProblemInfo;
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

import com.google.common.base.Optional;
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
            
            // BugPathItems
            ReportDetails reportdetails = client.getReportDetails(rd.getReportId());
            LinkedList<BugPathItem> listBuilder = new LinkedList<BugPathItem>();
            for (BugPathPos bpp : reportdetails.getExecutionPath()) {

                FileInfoAction fileinfoaction = new FileInfoAction(new FileInfoRequest(action.getRequest()
                        .getServer(), bpp.getFileId()));
                fileinfoaction = innerRunner.requireResult(fileinfoaction);
                if (fileinfoaction.getStatus() != ActionStatus.SUCCEEDED) {
                    throw new RuntimeException("Bad status for inner action: " + fileinfoaction);
                }

                System.out.println(" - " + bpp.getStartLine() + " - " + bpp.getStartCol());
                listBuilder.add(new BugPathItem(new BugPathItem.Position(bpp.getStartLine(), bpp
                        .getStartCol()), new BugPathItem.Position(bpp.getEndLine(), bpp.getEndCol()),
                        "", fileinfoaction.getResult().get().getFilePath()));
            }

            for (BugPathEvent bpe : reportdetails.getPathEvents()) {

                FileInfoAction fileinfoaction = new FileInfoAction(new FileInfoRequest(action.getRequest()
                        .getServer(), bpe.getFileId()));
                fileinfoaction = innerRunner.requireResult(fileinfoaction);
                if (fileinfoaction.getStatus() != ActionStatus.SUCCEEDED) {
                    throw new RuntimeException("Bad status for inner action: " + fileinfoaction);
                }

                listBuilder.add(new BugPathItem(new BugPathItem.Position(bpe.getStartLine(), bpe
                        .getStartCol()), new BugPathItem.Position(bpe.getEndLine(), bpe.getEndCol()),
                        bpe.getMsg(), fileinfoaction.getResult().get().getFilePath()));

                System.out.println("Event: " + bpe.getStartLine() + " - " + bpe.getStartCol() + " - "
                        + bpe.getMsg());
            }
            ImmutableList.Builder<BugPathItem> bugpathitems = new ImmutableList.Builder<>();
            bugpathitems.addAll(listBuilder);
            //Bug path items!!
            Optional<ProblemInfo> probleminfo = new ActionResult<>(
            		new ProblemInfo(bugpathitems.build())).getResult();
            
            builder.add(new ReportInfo(rd.getCheckerId(), rd.getBugHash(), rd.getCheckedFile(),
                    rd.getCheckerMsg(), rd.getReportId(), rd.isSuppressed(), fiaResult.getResult
                    ().get().getFilePath(), lastBugItem, probleminfo));
        }

        return new ActionResult<>(new ResultList(builder.build()));
    }

    @Override
    public ActionCommImpl<SearchRequest, ResultList, ThriftCommunicationInterface> dup() {
        return new ResultListActionThriftImpl();
    }
}
