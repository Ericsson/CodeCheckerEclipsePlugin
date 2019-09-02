package org.codechecker.eclipse.plugin.config;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;

/**
 * Environment variables to added to the C/C++ project environment variables.
 */
public enum EnvironmentVariables {
    LD_LIBRARY_PATH("/ld_logger/lib", IEnvironmentVariable.ENVVAR_REPLACE),
    _("/bin/CodeChecker", IEnvironmentVariable.ENVVAR_REPLACE),
    CC_LOGGER_GCC_LIKE("clang", IEnvironmentVariable.ENVVAR_REPLACE),
    LD_PRELOAD("ldlogger.so", IEnvironmentVariable.ENVVAR_REPLACE),
    CC_LOGGER_FILE("logfile", IEnvironmentVariable.ENVVAR_REPLACE),
    CC_LOGGER_BIN("/bin/ldlogger", IEnvironmentVariable.ENVVAR_REPLACE);

    private String defaultValue;
    private int method;

    /**
     *
     * @param def
     *            utility method for setting the default value.
     * @param method
     *            default addition method.
     */
    EnvironmentVariables(String def, int method) {
        this.defaultValue = def;
        this.method = method;
    }

    /**
     *
     * @return Returns the default value associated with the enum.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * 
     * @return Returns Environment variable addition method associated with the key.
     */
    public int getMethod( ) {
        return method;
    }

    /**
     *
     * @param s The query string.
     * @return The matching ConfigType if exists null otherwise.
     */
    public static EnvironmentVariables getFromString(String s) {
        for (EnvironmentVariables ev :EnvironmentVariables.values()) {
            if (s.equals(ev.toString()))
                return ev;
        }
        return null;
    }
}