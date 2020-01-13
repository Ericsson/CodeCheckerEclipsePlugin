package org.codechecker.eclipse.plugin.config.global;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.codechecker.CodeCheckerFactory;
import org.codechecker.eclipse.plugin.codechecker.ICodeChecker;
import org.codechecker.eclipse.plugin.codechecker.locator.CodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.EnvCodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.codechecker.locator.PreBuiltCodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.ResolutionMethodTypes;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.Config.ConfigLogger;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelperFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Sets;

/**
 * This Class represents the global configuration of the plugin.
 */
public class CcGlobalConfiguration extends CcConfigurationBase {
    private static final String STR_EMPTY = "";

    private static CcGlobalConfiguration instance;

    /**
     * Hidden due singleton pattern.
     */
    private CcGlobalConfiguration() {}

    /**
     * Calls {@link load} when first called.
     * @return The only Global Configuration instance.
     */
    public static CcGlobalConfiguration getInstance() {
        if (instance == null) {
            instance = new CcGlobalConfiguration();
            instance.load();

            CodeCheckerLocatorService serv = null;
            switch (ResolutionMethodTypes.valueOf(instance.config.get(ConfigTypes.RES_METHOD))) {
                case PATH:
                    serv = new EnvCodeCheckerLocatorService();
                    break;
                case PRE:
                    serv = new PreBuiltCodeCheckerLocatorService();
                    break;
                default:
                    break;
            }
            ICodeChecker cc = null;
            try {
                cc = serv.findCodeChecker(Paths.get(instance.config.get(ConfigTypes.CHECKER_PATH)),
                        new CodeCheckerFactory(),
                        new ShellExecutorHelperFactory());
            } catch (InvalidCodeCheckerException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                //TODO inform user to set a valid codeChecker.
            }
            instance.codeChecker = cc;
        }
        return instance;

    }

    /**
     * Initializes the plug-ins internal configuration, Call this method somewhere early.
     */
    @Override
    public void load() {
        preferences = ConfigurationScope.INSTANCE.getNode(CodeCheckerNature.NATURE_ID);
        validate();
        config = new ConcurrentHashMap<>();

        try {
            ConfigLogger configlogger = new ConfigLogger("Initialized GlobalConfig with:");
            for (String configKey : preferences.keys()) {
                ConfigTypes ct = ConfigTypes.getFromString(configKey);
                if (ct != null) config.put(ct, preferences.get(configKey, STR_EMPTY));
                configlogger.append(configKey + ConfigLogger.SEP + preferences.get(configKey, STR_EMPTY));
            }
            configlogger.log();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates global preferences of the plug-in.
     */
    @Override
    protected void validate() {
        if (preferences != null) {
            try {
                Set<String> storedGlobalPrefs = Sets.newHashSet(preferences.keys());
                ConfigLogger configlogger = new ConfigLogger("Missing keys in global config were:", IStatus.WARNING);
                //These are the valid configuration keys.
                for (Map.Entry<ConfigTypes, String> entry : ConfigTypes.getCommonDefault().entrySet()) {
                    //if the key doesn't exist in preferences, put the corresponding key-value in.
                    if (!storedGlobalPrefs.contains(entry.getKey().toString())){
                        preferences.put(entry.getKey().toString(), entry.getValue());
                        configlogger.append(entry);
                    }

                }
                configlogger.log();
                preferences.flush(); // flush only saves changes, so no need for a change flag.
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates both persistent and in memory representation of global configuration.
     * @param newConfig The new configuration to be flushed.
     */
    @Override
    public void update(Map<ConfigTypes, String> newConfig) {
        for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet()) {
            preferences.put(entry.getKey().toString(), entry.getValue());
            config.put(entry.getKey(), entry.getValue());
        }
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
        }
        notifyListeners(newConfig);
    }
}
