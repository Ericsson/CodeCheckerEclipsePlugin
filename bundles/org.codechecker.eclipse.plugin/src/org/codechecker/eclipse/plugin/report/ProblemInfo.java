package org.codechecker.eclipse.plugin.report;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

//TODO Refactor this to be called BugPathList.
/**
 * Contains a list of BugPathItems.
 */
public class ProblemInfo {

    private final ImmutableList<BugPathItem> items;

    public ProblemInfo(ImmutableList<BugPathItem> items) {
        this.items = items;
    }

    public ImmutableList<BugPathItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("items", items).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProblemInfo) {
            ProblemInfo oth = (ProblemInfo) obj;

            return Objects.equals(items, oth.items);
        }

        return false;
    }
}
