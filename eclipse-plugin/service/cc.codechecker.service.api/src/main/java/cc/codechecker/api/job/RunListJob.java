package cc.codechecker.api.job;


import cc.codechecker.api.action.run.list.ListRunsRequest;
import cc.codechecker.api.action.run.list.RunList;
import cc.ecl.job.SingleActionJob;

import com.google.common.base.Optional;

import org.joda.time.Instant;

public class RunListJob extends SingleActionJob<ListRunsRequest, RunList, RunListJob> {
    public RunListJob(ListRunsRequest request, int priority, Optional<Instant> deadline) {
        super(request, priority, deadline);
    }
}
