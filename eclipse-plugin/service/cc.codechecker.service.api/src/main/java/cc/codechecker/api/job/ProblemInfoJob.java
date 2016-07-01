package cc.codechecker.api.job;

import cc.codechecker.api.action.bug.path.ProblemInfo;
import cc.codechecker.api.action.bug.path.ProblemInfoRequest;
import cc.ecl.job.SingleActionJob;

import com.google.common.base.Optional;

import org.joda.time.Instant;

/**
 * Created by Zsolt on 2015.02.25..
 */
public class ProblemInfoJob extends SingleActionJob<ProblemInfoRequest, ProblemInfo,
        ProblemInfoJob> {

    public ProblemInfoJob(ProblemInfoRequest request, int priority, Optional<Instant> deadline) {
        super(request, priority, deadline);
    }
}
