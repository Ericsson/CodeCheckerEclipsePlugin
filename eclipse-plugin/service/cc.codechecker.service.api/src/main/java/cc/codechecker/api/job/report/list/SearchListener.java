package cc.codechecker.api.job.report.list;

import cc.codechecker.api.action.result.ReportInfo;
import cc.ecl.job.JobListener;

import com.google.common.collect.ImmutableList;

public interface SearchListener extends JobListener<SearchJob> {

    void onTotalCountAvailable(SearchJob searchJob, SearchList result, int count);

    void onPartsArrived(SearchJob searchJob, SearchList result, ImmutableList<ReportInfo>
            runResultList);
}
