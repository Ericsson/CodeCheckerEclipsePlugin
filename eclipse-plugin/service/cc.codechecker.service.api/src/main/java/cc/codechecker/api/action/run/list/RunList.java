package cc.codechecker.api.action.run.list;

import cc.codechecker.api.action.run.RunInfo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

public class RunList {

    ImmutableList<RunInfo> runList;

    public RunList(ImmutableList<RunInfo> runList) {
        this.runList = runList;
    }

    public ImmutableList<RunInfo> getRunList() {
        return runList;
    }

    public Optional<RunInfo> getLastRun() {
        if (runList.size() == 0) {
            return Optional.absent();
        }
        return Optional.of(runList.get(runList.size() - 1));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("list", runList).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(runList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RunList) {
            RunList oth = (RunList) obj;

            return Objects.equals(runList, oth.runList);
        }

        return false;
    }
}
