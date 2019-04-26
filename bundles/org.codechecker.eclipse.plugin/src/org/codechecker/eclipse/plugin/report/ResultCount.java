package org.codechecker.eclipse.plugin.report;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public class ResultCount {

    private final long count;

    public ResultCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("count", count).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(count);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResultCount) {
            ResultCount oth = (ResultCount) obj;
            return Objects.equals(count, oth.count);
        }

        return false;
    }
}
