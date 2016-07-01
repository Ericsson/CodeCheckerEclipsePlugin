package cc.codechecker.api.runtime;

import com.google.common.base.MoreObjects;

public class EnvironmentDifference {

    public final ModificationAction action;
    public final String variableName;
    public final String parameter;

    public EnvironmentDifference(ModificationAction action, String variableName, String parameter) {
        this.action = action;
        this.variableName = variableName;
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", action).add("variableName",
                variableName).add("parameter", parameter).toString();
    }


    // IGNORE is same as NONE, but intended to be used from specific strategies
    public enum ModificationAction {
        NONE, IGNORE, ADD, REMOVE, REPLACE, PREPEND, APPEND
    }
}
