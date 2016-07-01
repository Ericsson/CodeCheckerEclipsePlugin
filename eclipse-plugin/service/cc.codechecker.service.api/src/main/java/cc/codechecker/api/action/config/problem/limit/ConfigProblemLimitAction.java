package cc.codechecker.api.action.config.problem.limit;

import cc.ecl.action.Action;

public class ConfigProblemLimitAction extends Action<ConfigProblemLimitRequest,
        ConfigProblemLimitResult> {
    public ConfigProblemLimitAction(ConfigProblemLimitRequest request) {
        super(request);
    }
}
