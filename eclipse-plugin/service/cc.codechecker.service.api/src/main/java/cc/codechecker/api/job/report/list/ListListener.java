package cc.codechecker.api.job.report.list;

import cc.codechecker.api.action.result.ReportInfo;
import cc.ecl.job.JobListener;

import com.google.common.collect.ImmutableList;

public interface ListListener extends JobListener<ListJob> {

    void onTotalCountAvailable(ListJob searchJob, ListList result, int count);

    void onPartsArrived(ListJob searchJob, ListList result, ImmutableList<ReportInfo>
            runResultList);
}
