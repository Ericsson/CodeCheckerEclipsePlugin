package cc.codechecker.api.action.run.list;

import cc.codechecker.api.thrift.CodecheckerActionInitializer;
import cc.codechecker.api.action.bug.path.ProblemInfoAction;
import cc.codechecker.api.action.bug.path.ProblemInfoRequest;
import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.action.result.ResultFilter;
import cc.codechecker.api.action.result.count.SearchCountAction;
import cc.codechecker.api.action.result.count.SearchCountRequest;
import cc.codechecker.api.action.result.list.ResultList;
import cc.codechecker.api.action.result.list.SearchAction;
import cc.codechecker.api.job.report.list.SearchJob;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.api.job.report.list.SearchListener;
import cc.codechecker.api.job.report.list.SearchRequest;
import cc.ecl.action.*;
import cc.ecl.action.thrift.ThriftCommunicationInterface;
import cc.ecl.action.thrift.ThriftTransportFactory;
import cc.ecl.job.JobRunner;
import cc.ecl.job.SimpleJobRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;

public class ThriftTest {

    long reportId = 0;
    private PerServerActionRunner<ThriftCommunicationInterface> actionRunner;
    private JobRunner jobRunner;

    //@Before
    public void setupQueues() {
        actionRunner = new PerServerSimpleActionRunner<ThriftCommunicationInterface>(new
                ThriftTransportFactory(), (new CodecheckerActionInitializer()).initialize(new
                ActionImplementationRegistry()));
        jobRunner = new SimpleJobRunner(actionRunner);
    }

    //@Test
    public void runList() {
        ListRunsAction lra = new ListRunsAction(new ListRunsRequest("http://localhost:11444"));

        actionRunner.queueAction(lra, 0, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                System.out.println(action.getResult());
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    //@Test
    public void getLastResults() {
        SearchAction rla = new SearchAction(new cc.codechecker.api.action.result.list
                .SearchRequest("http://localhost:11444", Optional.<Long>absent(), 0, 20,
                ImmutableList.<ResultFilter>of()));

        actionRunner.queueAction(rla, 0, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                System.out.println(action.getResult());
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

    }

    //@Test
    public void getLastResultCount() {
        SearchCountAction rla = new SearchCountAction(new SearchCountRequest
                ("http://localhost:11444", Optional.<Long>absent(), ImmutableList
                        .<ResultFilter>of()));

        actionRunner.queueAction(rla, 0, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                System.out.println(action.getResult());
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

    }

    //@Test
    public void getLastResultCountWithFilter() {
        ResultFilter f1 = new ResultFilter(Optional.<String>absent(), Optional.<String>absent(),
                cc.codechecker.api.action.result.list.SearchRequest.Severity.ANY, Optional.of
                ("deadcode.DeadStores"), Optional.<String>absent(), false);
        SearchCountAction rla = new SearchCountAction(new SearchCountRequest
                ("http://localhost:11444", Optional.<Long>absent(), ImmutableList
                        .<ResultFilter>of(f1)));

        actionRunner.queueAction(rla, 0, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                System.out.println(action.getResult());
            }
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

    }

    //@Test
    public void testListJob() {
        SearchJob rlj = new SearchJob(1, Optional.of(new Instant().plus(1)), new SearchRequest
                ("http://localhost:11444", Optional.of(1L), ImmutableList.<ResultFilter>of()));

        rlj.addListener(new SearchListener() {
            @Override
            public void onTotalCountAvailable(SearchJob searchJob, SearchList result, int count) {
                System.out.println("Total: " + count + " " + result.getReportsFor("deadcode" + "" +
                        ".DeadStores"));
            }

            @Override
            public void onPartsArrived(SearchJob searchJob, SearchList result,
                                       ImmutableList<ReportInfo> runResultList) {
                System.out.println("Got parts size of " + runResultList.size());
            }

            @Override
            public void onJobStart(SearchJob j) {

            }

            @Override
            public void onJobComplete(SearchJob j) {
                System.out.println("Job completed" + j.getResult().getRecordCount());
            }

            @Override
            public void onJobTimeout(SearchJob j) {

            }

            @Override
            public void onJobInternalError(SearchJob j, RuntimeException e) {

            }
        });

        jobRunner.addJob(rlj);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SearchJob rlj2 = new SearchJob(5, Optional.of(new Instant().plus(1)), new SearchRequest
                ("http://localhost:11444", Optional.of(1L), ImmutableList.<ResultFilter>of()));
        jobRunner.addJob(rlj2);

        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    //@Test
    public void getDetails() {
        SearchAction rla = new SearchAction(new cc.codechecker.api.action.result.list
                .SearchRequest("http://localhost:11444", Optional.<Long>absent(), 0, 20,
                ImmutableList.<ResultFilter>of(new ResultFilter(Optional.of
                        ("/home/dutow/ericsson/tinyxml2-master/xmltest.cpp"), Optional
                        .<String>absent(), cc.codechecker.api.action.result.list.SearchRequest
                        .Severity.ANY, Optional.of("core.DivideZero"), Optional.<String>absent(),
                        false))));


        actionRunner.queueAction(rla, 0, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                reportId = ((ResultList) action.getResult().get()).getRunResultList().get(0)
                        .getReportId();
                System.out.println(action.getResult());
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        ProblemInfoAction bpa = new ProblemInfoAction(new ProblemInfoRequest
                ("http://localhost:11444", reportId));

        actionRunner.queueAction(bpa, 0, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                System.out.println(action.getResult());
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }
}
