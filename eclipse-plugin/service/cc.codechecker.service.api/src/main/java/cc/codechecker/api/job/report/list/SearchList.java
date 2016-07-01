package cc.codechecker.api.job.report.list;

import cc.codechecker.api.action.bug.path.ProblemInfoRequest;
import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.job.ProblemInfoJob;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import org.joda.time.Instant;

import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

public class SearchList {

    private final static Logger LOGGER = Logger.getLogger(SearchList.class.getName());

    private final SearchRequest request;

    private final Multimap<String, ReportInfo> reports;

    private Optional<Integer> totalReportCount;

    public SearchList(SearchRequest request) {
        this.request = request;
        this.reports = TreeMultimap.create();
        totalReportCount = Optional.absent();
    }

    void addReports(ImmutableList<ReportInfo> reports) {
        for (ReportInfo report : reports) {
            if (this.reports.containsValue(report)) {
                LOGGER.warning("Duplicate report in the result list: " + report);
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

    public ProblemInfoJob getBugPathJobFor(ReportInfo report, int priority, Optional<Instant>
            deadline) {
        return new ProblemInfoJob(new ProblemInfoRequest(this.request.getServer(), report
                .getReportId()), priority, deadline);
    }
}
