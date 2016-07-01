package cc.codechecker.api.action.bug.path;

import cc.codechecker.api.action.BugPathItem;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Contains every information about the problem
 */
public class ProblemInfo {

    private final List<BugPathItem> items;

    public ProblemInfo(List<BugPathItem> items) {
        List<BugPathItem> result = new ArrayList<>();
        for(BugPathItem bpi : items) {
            if(bpi.getMessage() != "") {
                result.add(bpi);
            }
        }
        this.items = result;
    }

    public List<BugPathItem> getItems() {
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
