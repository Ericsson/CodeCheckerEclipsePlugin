package cc.codechecker.plugin.config;

import java.io.File;
import java.util.Collections;
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
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Sets;

import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.Logger;
import cc.codechecker.plugin.config.Config.ConfigTypes;
import cc.codechecker.plugin.config.Config.ConfigTypesCommon;
import cc.codechecker.plugin.config.Config.ConfigTypesProject;
import cc.codechecker.plugin.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.plugin.runtime.CodecheckerServerThread;

/**
 * Stores and manages configurations related to projects and the plugin.
 */
public class CcConfiguration {

    private static Map<ConfigTypes, String> globalConfig;
    private static IEclipsePreferences globalPreferences;
    
    private static final String STR_EMPTY = "";
    private static final String TRUE = "";
    private static final String SEP = ":";
    
    private static final String CODECHECKER_WORKSPACE = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() 
            + "/.codechecker";

    private static final Map<ConfigTypes, String> DEFAULT_CONFIG = new HashMap<>();
    static {
        DEFAULT_CONFIG.put(ConfigTypesCommon.COMPILERS,"gcc:g++:clang");
        DEFAULT_CONFIG.put(ConfigTypesCommon.ANAL_THREADS,"4");
        DEFAULT_CONFIG.put(ConfigTypesCommon.CHECKER_PATH,STR_EMPTY);
        DEFAULT_CONFIG.put(ConfigTypesCommon.PYTHON_PATH,"");
        // TODO Somehow set a default checker List OR in gui
        // set a checkbox to use the default checkers.
        DEFAULT_CONFIG.put(ConfigTypesCommon.CHECKER_LIST,STR_EMPTY);
    }

    //Project related fields
    private IProject project;
    private boolean useGlobal = true;
    // This config has the same keys as global.
    private Map<ConfigTypesCommon,String> commonProjectConfig;
    // This config has no global meaning.
    private Map<ConfigTypesProject, String> projectOnlyConfig;
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
     * Validates preferences stored on dist for the project. At the time of the calling of this method,
     * the global pereferences must be initialized with this classes static methods.
     */
    private void validatePersistentProjectPreferences() {
        if (projectPreferences != null) {
            try {
                Set<String> storedProjPrefs = Sets.newHashSet(projectPreferences.keys());
                StringBuilder log = new StringBuilder();
                //Check the commons.
                for (Map.Entry<ConfigTypes, String> entry : globalConfig.entrySet()) {
                    //if the key doesn't exist in preferences, put the corresponding kv in.
                    if (!storedProjPrefs.contains(entry.getKey().toString())){
                        projectPreferences.put(entry.getKey().toString(), entry.getValue());
                        log.append(entry.getKey().toString() + SEP + entry.getValue() + System.lineSeparator());
                    }
                }
                for (ConfigTypesProject ctp : ConfigTypesProject.values()){
                    if (!storedProjPrefs.contains(ctp.toString())) {
                    	String value;
                        switch (ctp) {
                            case CHECKER_WORKSPACE:
                            	value = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() 
                                        + "/.codechecker/" + project.getName();
                                break;
                            case IS_GLOBAL:
                            	value = TRUE;
                                break;
                            default:
                            	value = STR_EMPTY;
                                break;
                        }
                        projectPreferences.put(ConfigTypesProject.IS_GLOBAL.toString(), value);
                        log.append(ctp.toString()  + SEP + value + System.lineSeparator());
                    }
                }
                Logger.log(IStatus.WARNING, "Missing keys in config " + project.getName() + System.lineSeparator()
                    + log.toString());
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
     * Stores Project level loaded preferences.
     */
    private void storeLoadedPreferences(){
        commonProjectConfig = new HashMap<>();
        projectOnlyConfig = new HashMap<>();
        try {
            for (String configKey : projectPreferences.keys()) {
                ConfigTypes ct = ConfigTypesCommon.GetFromString(configKey);
                if (ct != null)
                    commonProjectConfig.put((ConfigTypesCommon)ct, projectPreferences.get(configKey, STR_EMPTY));
                else {
                    ct = ConfigTypesProject.GetFromString(configKey);
                    if (ct != null) 
                    	projectOnlyConfig.put((ConfigTypesProject)ct, projectPreferences.get(configKey, STR_EMPTY));
                }
            }
            if (!projectOnlyConfig.containsKey(ConfigTypesProject.IS_GLOBAL)) {
                projectOnlyConfig.put(ConfigTypesProject.IS_GLOBAL, TRUE);
            }
            useGlobal = Boolean.parseBoolean(projectOnlyConfig.get(ConfigTypesProject.IS_GLOBAL));
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns preferences related to this project.
     * @param global If null the procets internal boolean decides which to return.
     * @return The config.
     */
    public Map<ConfigTypes, String> getProjectConfig(Boolean global) {
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
     * Returns the default config for the plugin. 
     * @return An Unmodifiable view of the default config Map.
     */
    public Map<ConfigTypes, String> getDefaultConfig() {
        return Collections.unmodifiableMap(DEFAULT_CONFIG);
    }


    /**
     * Logs the given configuration to Error Log.
     * @param config The config to be logged
     */
    public void logConfig(Map<ConfigTypes, String> config) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet()) {
            sb.append(entry.getKey() + SEP + entry.getValue() + System.lineSeparator());
        }
        Logger.log(IStatus.INFO, "Config: " + sb.toString());
    }

    /**
     * Returns if a project uses the global configuration.
     * @return True if global, false if not.
     */
    public boolean isGlobal() {
        return useGlobal;
    }

    /**
     * Updates the config.
     * @param config The new config to be saved.
     */
    public void updateProjectConfig(Map<ConfigTypes,String> config){
        StringBuilder log = new StringBuilder();
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet())
        {    
            log.append(entry.getKey().toString() + SEP + entry.getValue() + System.lineSeparator());
            projectPreferences.put(entry.getKey().toString(), entry.getValue());
        }
        Logger.log(IStatus.INFO, log.toString());
        try {
            projectPreferences.flush();
            storeLoadedPreferences();
            updateServer(CodeCheckerContext.getInstance().getServerObject(project));
        } catch (BackingStoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
            e.printStackTrace();
        }    	
    }

    /**
     * Return File location relative  to project location.
     * @param projectRelativeFile The file in question.
     * @return The location.
     */
    public String convertFilenameToServer(String projectRelativeFile) {
        return getLocationPrefix() + projectRelativeFile;
    }

    /**
     * Gets the projects containing folder on disk.
     * @return The projects location with an / appended.
     */
    public String getLocationPrefix() {
        return project.getLocation().toOSString() + "/";
    }

    /**
     * Strips path from file location.
     * @param serverFile Only the filename needed from this.
     * @return Filename stripped of it's path prefix.
     */
    public String convertFilenameFromServer(String serverFile) {
        if (getLocationPrefix().equals(STR_EMPTY)) {
            return serverFile;
        }
        return serverFile.replace(getLocationPrefix(), STR_EMPTY);
    }

    /**
     * Checks if the CodeChecker working directory exists, and creates it if not.
     */
    public void checkCodeCheckerReportDir() {
        File ccWorkDir = new File(CODECHECKER_WORKSPACE);
        if (!ccWorkDir.exists()) {
            Boolean b = ccWorkDir.mkdir();
            Logger.log(IStatus.INFO, "Making cc directory " + b);
        }
        File workDir = new File(projectOnlyConfig.get(ConfigTypesProject.CHECKER_WORKSPACE));
        if (!workDir.exists()) {
            Boolean b = workDir.mkdir();
            Logger.log(IStatus.INFO, "Making directory " + b);
        }
    }

    /**
     * TODO Will be deleted in next patch.
     * @param server .
     */
    public void updateServer(CodecheckerServerThread server) {
        if (project!=null){
            Logger.log(IStatus.INFO, "Updating Server" + project.getName());
            CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(getProjectConfig(null));
            server.setCodecheckerEnvironment(ccec);
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
            StringBuilder sb = new StringBuilder();
            for(String key : environmentAdd.keySet()) {
                sb.append(key + SEP + environmentAdd.get(key) + System.lineSeparator());
                if("PATH".equals(key)) {
                    ice.addVariable(key, environmentAdd.get(key),
                            IEnvironmentVariable.ENVVAR_PREPEND, STR_EMPTY, cfgd);
                } else {
                    ice.addVariable(key, environmentAdd.get(key),
                            IEnvironmentVariable.ENVVAR_REPLACE, STR_EMPTY, cfgd);
                }
            }
            Logger.log(IStatus.INFO, "Added Variables: " + sb.toString());
            try {
                CoreModel.getDefault().setProjectDescription(project, prjd);
            } catch (CoreException e) {
                Logger.log(IStatus.ERROR, e.getMessage());
                Logger.log(IStatus.INFO, e.getStackTrace().toString());
            }
        }
    }

    /**
     * @return The global config.
     */
    public static Map<ConfigTypes, String> getGlobalConfig() {
        return globalConfig;
    }

    /**
     * Initializes the plugins internal config, Call this method somewhere early.
     */
    public static void initGlobalConfig() {
    	Logger.log(IStatus.INFO, Collections.unmodifiableMap(DEFAULT_CONFIG).getClass().toString());
        globalPreferences = ConfigurationScope.INSTANCE.getNode(CodeCheckerNature.NATURE_ID);
        validatePersistentGlobalPreferences();
        globalConfig = new ConcurrentHashMap<>();
        try {
            StringBuilder log = new StringBuilder();
            for (String configKey : globalPreferences.keys()) {
                ConfigTypes ct = ConfigTypesCommon.GetFromString(configKey);
                if (ct != null) globalConfig.put(ct, globalPreferences.get(configKey, STR_EMPTY));
                log.append(configKey + SEP + globalPreferences.get(configKey, STR_EMPTY) + System.lineSeparator());
            }
            Logger.log(IStatus.INFO, System.lineSeparator() + log.toString());
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validates global preferences of the plugin.
     */
    private static void validatePersistentGlobalPreferences() {
        if (globalPreferences != null) {
            try {
                Set<String> storedGlobalPrefs = Sets.newHashSet(globalPreferences.keys());
                StringBuilder log = new StringBuilder();
                //These are the valid config keys.
                for (Map.Entry<ConfigTypes, String> entry : DEFAULT_CONFIG.entrySet()) {
                    //if the key doesn't exist in preferences, put the corresponding kv in.
                    if (!storedGlobalPrefs.contains(entry.getKey().toString())){
                        globalPreferences.put(entry.getKey().toString(), entry.getValue());
                        log.append(entry.getKey().toString() + SEP + entry.getValue() + System.lineSeparator());
                    }

                }
                Logger.log(IStatus.WARNING, "Missing keys in global config were:" + System.lineSeparator() 
                    + log.toString());
                globalPreferences.flush(); // flush only saves changes, so no need for a change flag.
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates both persistent and in memory representation of global config.
     * @param newConfig The new config to be flushed.
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
}
