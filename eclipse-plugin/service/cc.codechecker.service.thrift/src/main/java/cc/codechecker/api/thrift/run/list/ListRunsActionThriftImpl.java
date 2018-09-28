package cc.codechecker.api.thrift.run.list;

import cc.codechecker.api.action.run.RunInfo;
import cc.codechecker.api.action.run.list.ListRunsRequest;
import cc.codechecker.api.action.run.list.RunList;
import cc.codechecker.service.thrift.gen.codeCheckerDBAccess;
import cc.codechecker.service.thrift.gen.RunFilter;
import cc.codechecker.service.thrift.gen.RunData;
import cc.ecl.action.Action;
import cc.ecl.action.ActionCommImpl;
import cc.ecl.action.ActionResult;
import cc.ecl.action.InnerRunner;
import cc.ecl.action.thrift.ThriftActionImpl;
import cc.ecl.action.thrift.ThriftCommunicationInterface;

import com.google.common.collect.ImmutableList;

import org.apache.thrift.TException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ListRunsActionThriftImpl extends ThriftActionImpl<ListRunsRequest, RunList,
        codeCheckerDBAccess.Iface> {
    @Override
    protected String getProtocolUrlEnd(ListRunsRequest request) {
        return "CodeCheckerService";
    }

    @Override
    protected ActionResult<RunList> runThrift(codeCheckerDBAccess.Iface client,
                                              Action<ListRunsRequest, RunList> action,
                                              InnerRunner innerRunner) throws TException {

        ImmutableList.Builder<RunInfo> listBuilder = new ImmutableList.Builder<>();

        DateTimeFormatter datePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

	// construct run filter and pass it to getRunData()
	// null maybe sufficent
	
        //RunFilter rf = new RunFilter();

        client.getRunData(null);
        for (RunData d : client.getRunData(null)) {
            listBuilder.add(new RunInfo(d.getRunId(), DateTime.parse(d.getRunDate(), datePattern)
                    , d.getName(), d.getDuration(), d.getResultCount()));
        }

        return new ActionResult<>(new RunList(listBuilder.build()));
    }

    @Override
    public ActionCommImpl<ListRunsRequest, RunList, ThriftCommunicationInterface> dup() {
        return new ListRunsActionThriftImpl();
    }
}
