package org.codechecker.eclipse.plugin.config;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

/**
 * Classes for handling actual configuration entries, and logging.
 *
 */
public class Config {
    /**
     * Represents all available configuraton values.
     * Watch form marking comments, because specialized sets (Common, Project)
     * are returned using enumset range,
     *
     */
    public enum ConfigTypes {
        // Common configuration values
        CHECKER_PATH("codechecker_path"),
        RES_METHOD("PATH"),
        COMPILERS("gcc:g++:clang:clang++"),
        ANAL_THREADS("4"),
        CHECKER_LIST("enabled_checkers"),
        // Project configuration values
        IS_GLOBAL("true"),
        CHECKER_WORKSPACE("codechecker_workdir");

        public static Set<ConfigTypes> COMMON_TYPE = EnumSet.range(CHECKER_PATH, CHECKER_LIST);
        public static Set<ConfigTypes> PROJECT_TYPE = EnumSet.range(IS_GLOBAL, CHECKER_WORKSPACE);
        private String defaultValue;

        /**
         *
         * @param def utility method for setting the default value.
         */
        private ConfigTypes(String def) {
            this.defaultValue = def;
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
         * @param s The query string.
         * @return The matching ConfigType if exists null otherwise.
         */
        public static ConfigTypes getFromString(String s) {
            for (ConfigTypes c :ConfigTypes.values()) {
                if (s.equals(c.toString()))
                    return c;
            }
            return null;
        }

        /**
         *
         * @return Returns the default configuration values to the common configuration keys.
         */
        public static Map<ConfigTypes, String> getCommonDefault() {
            Map<ConfigTypes, String> map = new HashMap<ConfigTypes, String>();
            for (ConfigTypes ct : ConfigTypes.COMMON_TYPE)
               map.put(ct, ct.defaultValue);
            return map;
        }
    }
    
    /**
     * Utility class for easier config logging.
     *
     */
    public static class ConfigLogger {

        public static final String SEP = "=";

        private String header;
        private StringBuilder messageBuilder;

        /**
         * Initializes the header with a message.
         * @param header This will be the headline of the log.
         */
        public ConfigLogger(String header) {
            this(header, IStatus.INFO);
        }

        /**
         * Initializes the header with a message to be logged with, and
         * a log level if something other is desired than ISTATUS.INFO.
         * @param header This will be the headline of the log.
         * @param info The log level thats used for logging.
         */
        public ConfigLogger(String header, int info) {
            this.header = header + System.lineSeparator();
        }

        /**
         * Called on first append for flagging log need.
         */
        public void init() { if (messageBuilder == null) messageBuilder = new StringBuilder(); }

        /**
         * Appends a String to the message.
         * @param message Custom String.
         */
        public void append(String message){
            init();
            messageBuilder.append(message + System.lineSeparator());
        }

        /**
         * Appends a stringified entry to the message.
         * @param entry A {@link ConfigTypes} String pair.
         */
        public void append(Map.Entry<ConfigTypes, String> entry) {
            init();
            messageBuilder.append(entry.getKey() + SEP + entry.getValue() + System.lineSeparator());
        }

        /**
         * Only logs when there is a message added.
         */
        public void log(){
            if (messageBuilder != null)
                Logger.log(IStatus.WARNING, header + messageBuilder.toString());
        }
    }
}
