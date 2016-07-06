package cc.codechecker.api.thrift.bug.path;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.bug.path.ProblemInfo;
import cc.codechecker.api.action.bug.path.ProblemInfoRequest;
import cc.codechecker.api.action.file.info.FileInfoAction;
import cc.codechecker.api.action.file.info.FileInfoRequest;
import cc.codechecker.service.thrift.gen.BugPathEvent;
import cc.codechecker.service.thrift.gen.BugPathPos;
import cc.codechecker.service.thrift.gen.CodeCheckerDBAccess;
import cc.codechecker.service.thrift.gen.ReportDetails;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.ActionStatus;
import cc.ecl.action.InnerRunner;
import cc.ecl.action.thrift.ThriftActionImpl;
import cc.ecl.action.thrift.ThriftCommunicationInterface;
import cc.ecl.action.thrift.ThriftTransportFactory;

import com.google.common.collect.ImmutableList;

import org.apache.thrift.TException;

import java.util.Collections;
import java.util.LinkedList;

public class BugPathActionThriftImpl extends ThriftActionImpl<ProblemInfoRequest, ProblemInfo,
        CodeCheckerDBAccess.Iface> {
    @Override
    protected String getProtocolUrlEnd(ProblemInfoRequest request) {
        return "codeCheckerDBAccess";
    }

    @Override
    protected ActionResult<ProblemInfo> runThrift(CodeCheckerDBAccess.Iface client,
                                                  Action<ProblemInfoRequest, ProblemInfo> action,
                                                  InnerRunner innerRunner) throws TException {

        ReportDetails res = client.getReportDetails(action.getRequest().getReportId());

        LinkedList<BugPathItem> listBuilder = new LinkedList<BugPathItem>();

        for (BugPathPos bpp : res.getExecutionPath()) {

            FileInfoAction fiaBug = new FileInfoAction(new FileInfoRequest(action.getRequest()
                    .getServer(), bpp.getFileId()));
            fiaBug = innerRunner.requireResult(fiaBug);
            if (fiaBug.getStatus() != ActionStatus.SUCCEEDED) {
                throw new RuntimeException("Bad status for inner action: " + fiaBug);
            }

            System.out.println(" - " + bpp.getStartLine() + " - " + bpp.getStartCol());
            listBuilder.add(new BugPathItem(new BugPathItem.Position(bpp.getStartLine(), bpp
                    .getStartCol()), new BugPathItem.Position(bpp.getEndLine(), bpp.getEndCol()),
                    "", fiaBug.getResult().get().getFilePath()));
        }

        for (BugPathEvent bpe : res.getPathEvents()) {

            FileInfoAction fiaBug = new FileInfoAction(new FileInfoRequest(action.getRequest()
                    .getServer(), bpe.getFileId()));
            fiaBug = innerRunner.requireResult(fiaBug);
            if (fiaBug.getStatus() != ActionStatus.SUCCEEDED) {
                throw new RuntimeException("Bad status for inner action: " + fiaBug);
            }

            listBuilder.add(new BugPathItem(new BugPathItem.Position(bpe.getStartLine(), bpe
                    .getStartCol()), new BugPathItem.Position(bpe.getEndLine(), bpe.getEndCol()),
                    bpe.getMsg(), fiaBug.getResult().get().getFilePath()));

            System.out.println("Event: " + bpe.getStartLine() + " - " + bpe.getStartCol() + " - "
                    + bpe.getMsg());
        }
        ImmutableList.Builder<BugPathItem> builder = new ImmutableList.Builder<>();
        builder.addAll(listBuilder);
        return new ActionResult<>(new ProblemInfo(builder.build()));
    }

    @Override
    public ActionCommImpl<ProblemInfoRequest, ProblemInfo, ThriftCommunicationInterface> dup() {
        return new BugPathActionThriftImpl();
    }
}
