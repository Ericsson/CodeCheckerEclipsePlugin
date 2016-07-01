package cc.codechecker.api.action.config.problem.limit;

public class ConfigProblemLimitResult {
    private final int maximumSize;

    public ConfigProblemLimitResult(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    public int getMaximumSize() {
        return maximumSize;
    }
}
