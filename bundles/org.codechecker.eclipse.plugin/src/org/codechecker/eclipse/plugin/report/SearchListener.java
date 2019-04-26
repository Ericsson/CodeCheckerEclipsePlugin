package org.codechecker.eclipse.plugin.report;

import org.codechecker.eclipse.plugin.report.ReportInfo;

import com.google.common.collect.ImmutableList;

public interface SearchListener extends Listener {
//TODO Refactor without jobs
//    void onTotalCountAvailable(SearchJob searchJob, SearchList result, int count);
    void onTotalCountAvailable(SearchList result, int count);
 
//    void onPartsArrived(SearchJob searchJob, SearchList result, ImmutableList<ReportInfo> runResultList);
    void onPartsArrived(SearchList result);
}
