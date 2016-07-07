package cc.codechecker.api.action.result;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.bug.path.ProblemInfo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;

import java.util.Objects;

/**
 * Created by Zsolt on 2015.02.25..
 */
public class ReportInfo implements Comparable<ReportInfo> {

    private final String checkerId;
    private final String bugHash;
    private final String checkedFile;
    private final String checkerMsg;
    private final long reportId;
    private final boolean suppressed;
    private final String file;
    private final BugPathItem lastBugPathItem;
    private Optional<ProblemInfo> bugPath;
    
    public ReportInfo(String checkerId, String bugHash, String checkedFile, String checkerMsg,
                      long reportId, boolean suppressed, String file, BugPathItem lastBugPathItem) {
        this.checkerId = checkerId;
        this.bugHash = bugHash;
        this.checkedFile = checkedFile;
        this.checkerMsg = checkerMsg;
        this.reportId = reportId;
        this.suppressed = suppressed;
        this.file = file;
        this.lastBugPathItem = lastBugPathItem;
    }
    
    public void addBugPath(Optional<ProblemInfo> optional) {
    	this.bugPath = optional;
    }
    
    public Optional<ProblemInfo> getBugPath() {
    	return this.bugPath;
    }
    
    public String getCheckerId() {
        return checkerId;
    }

    public String getBugHash() {
        return bugHash;
    }

    public String getCheckedFile() {
        return checkedFile;
    }

    public String getCheckerMsg() {
        return checkerMsg;
    }

    public long getReportId() {
        return reportId;
    }

    public boolean isSuppressed() {
        return suppressed;
    }

    public String getFile() {
        return file;
    }

    public BugPathItem getLastBugPathItem() {
        return lastBugPathItem;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("checkerId", checkerId).add("checkedFile",
                checkedFile).add("checkerMsg", checkerMsg).add("reportId", reportId).add
                ("suppressed", suppressed).add("file", file).add("lastBugPathItem",
                lastBugPathItem).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(checkerId, checkedFile, checkerMsg, reportId, suppressed, file,
                lastBugPathItem);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReportInfo) {
            ReportInfo oth = (ReportInfo) obj;
            return Objects.equals(checkerId, oth.checkerId) && Objects.equals(checkedFile, oth
                    .checkedFile) && Objects.equals(checkerMsg, oth.checkerMsg) && Objects.equals
                    (reportId, oth.reportId) && Objects.equals(suppressed, oth.suppressed) &&
                    Objects.equals(file, oth.file) && Objects.equals(lastBugPathItem, oth
                    .lastBugPathItem);
        }

        return false;
    }

    @Override
    public int compareTo(ReportInfo o) {
        return ComparisonChain.start().compare(file, o.file).compare(suppressed, o.suppressed)
                .compare(checkerMsg, o.checkerMsg).compare(lastBugPathItem.getStartPosition(), o
                        .lastBugPathItem.getStartPosition()).compare(reportId, o.reportId).result();
    }
}
