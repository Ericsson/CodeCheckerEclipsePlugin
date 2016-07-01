package cc.codechecker.api.runtime;

import com.google.common.collect.ImmutableMap;

/**
 * Parses the output of /usr/bin/env into a String map
 */
public class EnvironmentParser {

    public ImmutableMap<String, String> parse(String envOutput) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();

        for (String line : envOutput.split("\n")) {
            String[] entry = line.trim().split("=", 2);
            if (entry.length == 2) {
                builder.put(entry[0], entry[1]);
            }
        }

        return builder.build();
    }
}
