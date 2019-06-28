package org.codechecker.eclipse.plugin.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.Config.ConfigLogger;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.config.global.CcGlobalConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Sets;

/**
 * Stores and manages configurations related to projects and the plug-in.
 */
public class CcConfiguration extends CcConfigurationBase {

    private IProject project;

    /**
     * Creates a project related configuration.
     * @param project The project which the configuration should be made.
     */
    public CcConfiguration(IProject project) {
        this.project = project;
        preferences = new ProjectScope(project).getNode(CodeCheckerNature.NATURE_ID);
        load();
    }

    /**
     * Loads project level preferences from disk.
     */
    @Override
    public void load() {
        validate();
        config = new HashMap<>();
        try {
            for (String configKey : preferences.keys()) {
                ConfigTypes ct = ConfigTypes.getFromString(configKey);
                if (ct != null) {
                    if (ConfigTypes.COMMON_TYPE.contains(ct)) {
                        config.put((ConfigTypes)ct, preferences.get(configKey, STR_EMPTY));
                    }
                }
            }
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates preferences stored on disc for the project. At the time of the calling of this method,

     */
    @Override
    protected void validate() {
        if (preferences != null) {
            try {
                Set<String> storedProjPrefs = Sets.newHashSet(preferences.keys());
                ConfigLogger configLogger = new ConfigLogger("Missing keys in config", IStatus.WARNING);
                for (ConfigTypes key : ConfigTypes.COMMON_TYPE) {
                    //if the key doesn't exist in preferences, put the corresponding kv in.
                    if (!storedProjPrefs.contains(key.toString())){
                        preferences.put(key.toString(), CcGlobalConfiguration.getInstance().get(key));
                        configLogger.append(key.toString() + " " + CcGlobalConfiguration.getInstance().get(key));
                    }
                }
                configLogger.log();
                preferences.flush(); // flush only saves changes, so no need for a change flag.
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns preferences related to this project.
     * @return The configuration which the objects internal boolean has chosen.
     */
    @Override
    public Map<ConfigTypes, String> get() {
        Map<ConfigTypes, String> retConfig = new HashMap<>();
        retConfig.putAll(config);
        return retConfig;
    }

    /**
     * Updates the persistent configuration.
     * @param config The new configuration to be saved.
     */
    @Override
    public void update(Map<ConfigTypes, String> config) {
        ConfigLogger configLogger = new ConfigLogger("Updated Project configuration with the following:");
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet())
        {    
            preferences.put(entry.getKey().toString(), entry.getValue());
            this.config.put(entry.getKey(), entry.getValue());
            configLogger.append(entry);
        }
        configLogger.log();
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
            e.printStackTrace();
        }
        notifyListeners(config);
    }

    /**
     * Logs the given configuration to Error Log.
     * @param config The configuration to be logged
     */
    public static void logConfig(Map<ConfigTypes, String> config) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet()) {
            sb.append(entry.getKey() + ConfigLogger.SEP + entry.getValue() + System.lineSeparator());
        }
        Logger.log(IStatus.INFO, "Config: " + System.lineSeparator() + sb.toString());
    }
}
