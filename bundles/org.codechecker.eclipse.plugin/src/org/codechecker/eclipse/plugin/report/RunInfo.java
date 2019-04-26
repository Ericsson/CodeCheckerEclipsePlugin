package org.codechecker.eclipse.plugin.report;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

import java.util.Objects;

public class RunInfo {
    private final long runId;
    private final DateTime runDate;
    private final String name;
    private final long duration;
    private final long resultCount;

    public RunInfo(long runId, DateTime runDate, String name, long duration, long resultCount) {
        this.runId = runId;
        this.runDate = runDate;
        this.name = name;
        this.duration = duration;
        this.resultCount = resultCount;
    }

    public long getRunId() {
        return runId;
    }

    public DateTime getRunDate() {
        return runDate;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }

    public long getResultCount() {
        return resultCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("runID", runId).add("runDate", runDate).add
                ("name", name).add("duration", duration).add("count", resultCount).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(runId, runDate, name, duration, resultCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RunInfo) {
            RunInfo oth = (RunInfo) obj;

            return Objects.equals(runId, oth.runId) && Objects.equals(runDate, oth.runDate) &&
                    Objects.equals(name, oth.name) && Objects.equals(duration, oth.duration) &&
                    Objects.equals(resultCount, oth.resultCount);
        }

        return false;
    }
}
