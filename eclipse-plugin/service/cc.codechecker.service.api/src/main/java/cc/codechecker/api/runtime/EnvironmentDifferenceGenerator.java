package cc.codechecker.api.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;

public class EnvironmentDifferenceGenerator {

    private HashMap<String, EnvironmentDifference.ModificationAction> specificStrategies;
    private HashMap<String, String> specificStrategyParameters;

    public EnvironmentDifferenceGenerator() {
        specificStrategies = new HashMap<>();
        specificStrategyParameters = new HashMap<>();
    }

    public void addStrategyParameter(String variableName, EnvironmentDifference
            .ModificationAction strategy, String parameter) {
        if (strategy == EnvironmentDifference.ModificationAction.REMOVE || strategy ==
                EnvironmentDifference.ModificationAction.ADD) {
            throw new IllegalArgumentException("Only APPEND, PREPEND or REPLACE strategies can " +
                    "be" + " specified!");
        }
        specificStrategies.put(variableName, strategy);
        specificStrategyParameters.put(variableName, parameter);
    }

    public ImmutableList<EnvironmentDifference> difference(ImmutableMap<String, String>
                                                                   sourceEnv,
                                                           ImmutableMap<String, String>
                                                                   destinationEnv) {
        ImmutableList.Builder<EnvironmentDifference> builder = new ImmutableList.Builder<>();
        for (String sourceKey : sourceEnv.keySet()) {
            if (!destinationEnv.containsKey(sourceKey)) {
                builder.add(new EnvironmentDifference(EnvironmentDifference.ModificationAction
                        .REMOVE, sourceKey, ""));
            }
        }
        for (String destinationKey : destinationEnv.keySet()) {
            if (!sourceEnv.containsKey(destinationKey)) {
                builder.add(new EnvironmentDifference(EnvironmentDifference.ModificationAction
                        .ADD, destinationKey, destinationEnv.get(destinationKey)));
            } else {
                EnvironmentDifference.ModificationAction strategy;
                String sourceValue = sourceEnv.get(destinationKey);
                String destinationValue = destinationEnv.get(destinationKey);
                if (specificStrategies.containsKey(destinationKey)) {
                    strategy = specificStrategies.get(destinationKey);
                    if (strategy == EnvironmentDifference.ModificationAction.REPLACE) {
                        destinationValue = specificStrategyParameters.get(destinationKey);
                    }
                } else {
                    strategy = deduceStrategy(destinationKey, sourceValue, destinationValue);
                }
                applyStrategy(builder, strategy, destinationKey, sourceValue, destinationValue);
            }
        }

        return builder.build();
    }

    private void applyStrategy(ImmutableList.Builder<EnvironmentDifference> builder,
                               EnvironmentDifference.ModificationAction strategy, String
                                       destinationKey, String sourceValue, String
                                       destinationValue) {
        switch (strategy) {
            case REPLACE:
                builder.add(new EnvironmentDifference(strategy, destinationKey, destinationValue));
                break;
            // TODO: not perfect, but works for now
            case APPEND:
                builder.add(new EnvironmentDifference(strategy, destinationKey, destinationValue
                        .replace(sourceValue, "")));
                break;
            case PREPEND:
                builder.add(new EnvironmentDifference(strategy, destinationKey, destinationValue
                        .replace(sourceValue, "")));
                break;
            // nop for ADD / REMOVE
        }
    }

    private EnvironmentDifference.ModificationAction deduceStrategy(String destinationKey, String
            sourceValue, String destinationValue) {

        if (sourceValue.equals(destinationValue)) {
            return EnvironmentDifference.ModificationAction.NONE;
        }

        // common patterns ... for path or other lists
        if (destinationValue.startsWith(sourceValue + ":")) {
            return EnvironmentDifference.ModificationAction.APPEND;
        }
        if (destinationValue.endsWith(":" + sourceValue)) {
            return EnvironmentDifference.ModificationAction.PREPEND;
        }
        if (destinationValue.startsWith(sourceValue + ";")) {
            return EnvironmentDifference.ModificationAction.APPEND;
        }
        if (destinationValue.endsWith(";" + sourceValue)) {
            return EnvironmentDifference.ModificationAction.PREPEND;
        }

        return EnvironmentDifference.ModificationAction.REPLACE;
    }
}
