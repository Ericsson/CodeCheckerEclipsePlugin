package org.codechecker.eclipse.plugin.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Sets;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.Config.ConfigLogger;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.runtime.CodeCheckEnvironmentChecker;

/**
 * Stores and manages configurations related to projects and the plug-in.
 */
public class CcConfiguration {

    private static Map<ConfigTypes, String> globalConfig;
    private static IEclipsePreferences globalPreferences;
    
    private static final String STR_EMPTY = "";
    
    private static final Path CODECHECKER_WORKSPACE = Paths.get(
            ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(), ".codechecker");

    //Project related fields
    private IProject project;
    private boolean useGlobal = true;
    // This configuration has the same keys as global.
    private Map<ConfigTypes,String> commonProjectConfig;
    // This is a project local configuration not available globally.
    private Map<ConfigTypes, String> projectOnlyConfig;
    private IEclipsePreferences projectPreferences;

    /**
     * Creates a project related configuration.
     * @param project The project which the configuration should be made.
     */
    public CcConfiguration(IProject project) {
        this.project = project;

        loadProjectConfig(project);
        validatePersistentProjectPreferences();
        storeLoadedPreferences();
        checkCodeCheckerReportDir();
    }

    /**
     * Validates preferences stored on disc for the project. At the time of the calling of this method,
     * the global preferences must be initialized with this classes static methods.
     */
    private void validatePersistentProjectPreferences() {
        if (projectPreferences != null) {
            try {
                Set<String> storedProjPrefs = Sets.newHashSet(projectPreferences.keys());
                ConfigLogger configLogger = new ConfigLogger("Missing keys in config " + project.getName(),
                        IStatus.WARNING);
                //Check the commons.
                for (Map.Entry<ConfigTypes, String> entry : globalConfig.entrySet()) {
                    //if the key doesn't exist in preferences, put the corresponding kv in.
                    if (!storedProjPrefs.contains(entry.getKey().toString())){
                        projectPreferences.put(entry.getKey().toString(), entry.getValue());
                        configLogger.append(entry);
                    }
                }
                for (ConfigTypes ctp : ConfigTypes.PROJECT_TYPE){
                    if (!storedProjPrefs.contains(ctp.toString())) {
                    	String value;
                        switch (ctp) {
                            case CHECKER_WORKSPACE:
                                value = Paths.get(CODECHECKER_WORKSPACE.toString(), project.getName()).toString();
                                break;
                            case IS_GLOBAL:
                                value = Boolean.TRUE.toString();
                                break;
                            default:
                            	value = STR_EMPTY;
                                break;
                        }
                        projectPreferences.put(ctp.toString(), value);
                        configLogger.append(ctp.toString()  + ConfigLogger.SEP + value);
                    }
                }
                configLogger.log();
                projectPreferences.flush(); // flush only saves changes, so no need for a change flag.
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads project level preferences from disk.
     * @param project The project in question.
     */
    public void loadProjectConfig(IProject project) {
        try {
            // If it's an "old project" open saved context
            if (project.hasNature(CodeCheckerNature.NATURE_ID)) {
                IScopeContext context = new ProjectScope(project);
                projectPreferences = context.getNode(CodeCheckerNature.NATURE_ID);
            } 
        } catch (CoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
            Logger.log(IStatus.INFO, e.getStackTrace().toString());
            return;
        }
    }

    /**
     * Stores previously loaded Project level preferences.
     */
    private void storeLoadedPreferences(){
        commonProjectConfig = new HashMap<>();
        projectOnlyConfig = new HashMap<>();
        try {
            for (String configKey : projectPreferences.keys()) {
                ConfigTypes ct = ConfigTypes.getFromString(configKey);
                if (ct != null) {
                    if (ConfigTypes.COMMON_TYPE.contains(ct)) {
                        commonProjectConfig.put((ConfigTypes)ct, projectPreferences.get(configKey, STR_EMPTY));
                    }
                    if (ConfigTypes.PROJECT_TYPE.contains(ct)) {
                        projectOnlyConfig.put((ConfigTypes)ct, projectPreferences.get(configKey, STR_EMPTY));
                    }
                }
            }
            useGlobal = Boolean.parseBoolean(projectOnlyConfig.get(ConfigTypes.IS_GLOBAL));
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns preferences related to this project.
     * @param global If null the projects internal boolean decides which to return.
     * @return The configuration which has chosen.
     */
    public Map<ConfigTypes, String> getProjectConfig(@Nullable Boolean global) {
        Map<ConfigTypes, String> retConfig = new HashMap<>();
        retConfig.putAll(projectOnlyConfig);
        if (global == null) {
            if (useGlobal){
                retConfig.putAll(globalConfig);
            } else {
                retConfig.putAll(commonProjectConfig);
            }
            return retConfig;
        } else { // if we want the projects choice
            if (global){
                retConfig.putAll(globalConfig);
            } else {
                retConfig.putAll(commonProjectConfig);
            }
            return retConfig;
        }
    }

    /**
     * Returns the default configuration for the plug-in.
     * @return An Unmodifiable view of the default configuration Map.
     */
    public Map<ConfigTypes, String> getDefaultConfig() {
        return ConfigTypes.getCommonDefault();
    }

    /**
     * Returns if a project uses the global configuration.
     * @return True if global, false if not.
     */
    public boolean isGlobal() {
        return useGlobal;
    }

    /**
     * Updates the persistent configuration.
     * @param config The new configuration to be saved.
     */
    public void updateProjectConfig(Map<ConfigTypes,String> config){
        ConfigLogger configLogger = new ConfigLogger("Updated Project configuration with the following:");
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet())
        {    
            projectPreferences.put(entry.getKey().toString(), entry.getValue());
            configLogger.append(entry);
        }
        configLogger.log();
        try {
            projectPreferences.flush();
            storeLoadedPreferences();
        } catch (BackingStoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
            e.printStackTrace();
        }    	
    }

    /**
     * Return File location relative to project location.
     * @param projectRelativeFile The file in question.
     * @return The location.
     */
    public String getAsProjectRelativePath(String projectRelativeFile) {
        return getLocationPrefix() + projectRelativeFile;
    }

    /**
     * Gets the projects containing folder on disk.
     * @return The projects location with system dependent name-separator appended to it.
     */
    public String getLocationPrefix() {
        return project.getLocation().toOSString() + File.separator;
    }

    /**
     * Strips path from file location.
     * @param path Only the filename needed from this.
     * @return Filename stripped of it's path prefix.
     */
    public String stripProjectPathFromFilePath(String path) {
        if (getLocationPrefix().equals(STR_EMPTY)) {
            return path;
        }
        return path.replace(getLocationPrefix(), STR_EMPTY);
    }

    /**
     * Checks if the CodeChecker working directory exists, and creates it if not.
     */
    public void checkCodeCheckerReportDir() {
        File ccWorkDir = CODECHECKER_WORKSPACE.toFile();
        if (!ccWorkDir.exists()) {
            Boolean b = ccWorkDir.mkdir();
            Logger.log(IStatus.INFO, "Making cc directory " + b);
        }
        File workDir = new File(projectOnlyConfig.get(ConfigTypes.CHECKER_WORKSPACE));
        if (!workDir.exists()) {
            Boolean b = workDir.mkdir();
            Logger.log(IStatus.INFO, "Making directory " + b);
        }
    }

    /**
     * Adds to build environment variables, to be able to log the compilation commands with lldb.
     * @param environmentAdd
     */
    public void modifyProjectEnvironmentVariables() {
        CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(getProjectConfig(null));
        final Map<String, String> environmentAdd = ccec.getEnvironmentAddList();
        if (project!=null){
            IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager()
                .getContributedEnvironment();
            //we assume that the project is CDT
            ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, true); 
            ICConfigurationDescription cfgd = prjd.getActiveConfiguration();
            ConfigLogger configLogger = new ConfigLogger("Modified the build environment variables with:");
            for(String key : environmentAdd.keySet()) {
                if("PATH".equals(key)) {
                    ice.addVariable(key, environmentAdd.get(key),
                            IEnvironmentVariable.ENVVAR_PREPEND, STR_EMPTY, cfgd);
                } else {
                    ice.addVariable(key, environmentAdd.get(key),
                            IEnvironmentVariable.ENVVAR_REPLACE, STR_EMPTY, cfgd);
                }
                configLogger.append(key + ConfigLogger.SEP + environmentAdd.get(key));
            }
            configLogger.log();
            try {
                CoreModel.getDefault().setProjectDescription(project, prjd);
            } catch (CoreException e) {
                Logger.log(IStatus.ERROR, e.getMessage());
                Logger.log(IStatus.INFO, e.getStackTrace().toString());
            }
        }
    }

    /**
     *
     * @return Returns the newly generated analyze log location.
     */
    public String getLogFileLocation() {
        CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(getProjectConfig(null));
        String logFile = ccec.getLogFileLocation();
        Logger.log(IStatus.INFO,"Logfile being used is: " + logFile);
        return logFile;
    }

    /**
     * @return The global configuration.
     */
    public static Map<ConfigTypes, String> getGlobalConfig() {
        return globalConfig;
    }

    /**
     * Initializes the plug-ins internal configuration, Call this method somewhere early.
     */
    public static void initGlobalConfig() {
        globalPreferences = ConfigurationScope.INSTANCE.getNode(CodeCheckerNature.NATURE_ID);
        validatePersistentGlobalPreferences();
        globalConfig = new ConcurrentHashMap<>();

        try {
            ConfigLogger configlogger = new ConfigLogger("Initialized GlobalConfig with:");
            for (String configKey : globalPreferences.keys()) {
                ConfigTypes ct = ConfigTypes.getFromString(configKey);
                if (ct != null) globalConfig.put(ct, globalPreferences.get(configKey, STR_EMPTY));
                configlogger.append(configKey + ConfigLogger.SEP + globalPreferences.get(configKey, STR_EMPTY));
            }
            configlogger.log();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates global preferences of the plug-in.
     */
    private static void validatePersistentGlobalPreferences() {
        if (globalPreferences != null) {
            try {
                Set<String> storedGlobalPrefs = Sets.newHashSet(globalPreferences.keys());
                ConfigLogger configlogger = new ConfigLogger("Missing keys in global config were:", IStatus.WARNING);
                //These are the valid configuration keys.
                for (Map.Entry<ConfigTypes, String> entry : ConfigTypes.getCommonDefault().entrySet()) {
                    //if the key doesn't exist in preferences, put the corresponding key-value in.
                    if (!storedGlobalPrefs.contains(entry.getKey().toString())){
                        globalPreferences.put(entry.getKey().toString(), entry.getValue());
                        configlogger.append(entry);
                    }

                }
                configlogger.log();
                globalPreferences.flush(); // flush only saves changes, so no need for a change flag.
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates both persistent and in memory representation of global configuration.
     * @param newConfig The new configuration to be flushed.
     */
    public static void updateGlobalConfig(Map<ConfigTypes, String> newConfig) {
        globalConfig = newConfig;
        for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet()) {
            globalPreferences.put(entry.getKey().toString(), entry.getValue());
        }
        try {
            globalPreferences.flush();
        } catch (BackingStoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
        }
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
