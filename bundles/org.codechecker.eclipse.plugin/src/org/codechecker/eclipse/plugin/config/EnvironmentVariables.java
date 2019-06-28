package org.codechecker.eclipse.plugin.config;

import java.util.EnumSet;
import java.util.Set;

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
    CC_LOGGER_BIN("/bin/ldlogger", IEnvironmentVariable.ENVVAR_REPLACE),
    //python variables comes after this;
    PATH("/bin:", IEnvironmentVariable.ENVVAR_PREPEND),
    VIRTUAL_ENV("pythonEnv", IEnvironmentVariable.ENVVAR_REPLACE);

    public static Set<EnvironmentVariables> BASE_TYPE = EnumSet.range(LD_LIBRARY_PATH, CC_LOGGER_BIN);
    public static Set<EnvironmentVariables> PYTHON_TYPE = EnumSet.range(PATH, VIRTUAL_ENV);
    public static Set<EnvironmentVariables> ALL = EnumSet.range(LD_LIBRARY_PATH, VIRTUAL_ENV);

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