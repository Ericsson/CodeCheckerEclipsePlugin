package org.codechecker.eclipse.plugin.report;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

public class ResultList {

    private final ImmutableList<ReportInfo> runResultList;

    public ResultList(ImmutableList<ReportInfo> runResultList) {
        this.runResultList = runResultList;
    }

    public ImmutableList<ReportInfo> getRunResultList() {
        return runResultList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("result", runResultList).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(runResultList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResultList) {
            ResultList oth = (ResultList) obj;
            return Objects.equals(runResultList, oth.runResultList);
        }

        return false;
    }
}
