package cc.codechecker.api.action.bug.path;


import cc.ecl.action.AbstractServerRequest;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class ProblemInfoRequest extends AbstractServerRequest {

    private final long reportId;

    public ProblemInfoRequest(String server, long reportId) {
        super(server);
        this.reportId = reportId;
    }

    public long getReportId() {
        return reportId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("server", server).add("reportId", reportId)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, reportId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProblemInfoRequest && super.equals(obj)) {
            ProblemInfoRequest oth = (ProblemInfoRequest) obj;

            return Objects.equals(reportId, oth.reportId);
        }

        return false;
    }
}
