package org.codechecker.eclipse.plugin.report;


import org.codechecker.eclipse.plugin.report.ReportInfo;
import org.codechecker.eclipse.plugin.runtime.LogI;
import org.codechecker.eclipse.plugin.runtime.SLogger;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;


import java.util.Collection;
import java.util.Set;

/**
 * Stores all report information for a project.
 *
 */
public class SearchList {

    private final Multimap<String, ReportInfo> reports;

    private Optional<Integer> totalReportCount;

    public SearchList() {
        this.reports = TreeMultimap.create();
        totalReportCount = Optional.absent();
    }

    public void addReports(ImmutableList<ReportInfo> reports) {
        for (ReportInfo report : reports) {
            if (this.reports.containsValue(report)) {
                //SLogger.log(LogI.WARNING, "Duplicate report in the result list: " + report);
            }
            this.reports.put(report.getCheckerId(), report);
        }
    }

    public Optional<Integer> getTotalReportCount() {
        return totalReportCount;
    }

    public void setTotalReportCount(Integer totalReportCount) {
        this.totalReportCount = Optional.of(totalReportCount);
    }

    public Integer getRecordCount() {
        return reports.size();
    }

    public Set<String> getCheckers() {
        return reports.keySet();
    }

    public Collection<ReportInfo> getReportsFor(String checker) {
        return reports.get(checker);
    }

    /*public ProblemInfoJob getBugPathJobFor(ReportInfo report, int priority, Optional<Instant>
            deadline) {
        return new ProblemInfoJob(new ProblemInfoRequest(this.request.getServer(), report
                .getReportId()), priority, deadline);
    }*/
}
