package cc.codechecker.plugin.config;

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
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import cc.codechecker.api.config.Config.ConfigTypes;
import cc.codechecker.api.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.api.runtime.CodecheckerServerThread;
import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.views.console.ConsoleFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class CcConfiguration {

    // Logger
    private static final Logger logger = LogManager.getLogger(CcConfiguration.class);
    IProject project;
    IEclipsePreferences projectPreferences;
    static final IEclipsePreferences globalPreferences = ConfigurationScope.INSTANCE
            .getNode(CodeCheckerNature.NATURE_ID);

    private static Map<ConfigTypes, String> defaults;// default config values
    private static Map<ConfigTypes, String> configKeys;// textual values of
    // config variables

    public CcConfiguration(IProject project) {
        super();

        defaults = new HashMap<ConfigTypes, String>();
        defaults.put(ConfigTypes.COMPILERS, "gcc:g++:clang");
        defaults.put(ConfigTypes.ANAL_THREADS, "4");
        defaults.put(ConfigTypes.IS_GLOBAL, "true");

        configKeys = new HashMap<ConfigTypes, String>();
        configKeys.put(ConfigTypes.CHECKER_PATH, "global_server_url");
        configKeys.put(ConfigTypes.PYTHON_PATH, "location_prefix");
        configKeys.put(ConfigTypes.COMPILERS, "compilers");
        configKeys.put(ConfigTypes.ANAL_THREADS, "analthreads");
        configKeys.put(ConfigTypes.CHECKER_LIST, "global_checker_command");
        configKeys.put(ConfigTypes.IS_GLOBAL, "is_global");
        configKeys.put(ConfigTypes.CHECKER_WORKSPACE, "checker_workspace");

        this.project = project;

        try {
            if (project.hasNature(CodeCheckerNature.NATURE_ID)) {
                IScopeContext context = new ProjectScope(project);
                projectPreferences = context.getNode(CodeCheckerNature.NATURE_ID);
            }
        } catch (CoreException e) {
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }

    public void modifyProjectEnvironmentVariables(final IProject project, final Map<String, String> environmentAdd) {
        IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment();
        // we assume that the project is CDT
        ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, true);
        ICConfigurationDescription cfgd = prjd.getActiveConfiguration();
        for (String key : environmentAdd.keySet()) {
            if (key.equals("PATH")) {
                ice.addVariable(key, environmentAdd.get(key), IEnvironmentVariable.ENVVAR_PREPEND, "", cfgd);
            } else {
                ice.addVariable(key, environmentAdd.get(key), IEnvironmentVariable.ENVVAR_REPLACE, "", cfgd);
            }
        }
        try {
            CoreModel.getDefault().setProjectDescription(project, prjd);
        } catch (CoreException e) {
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }

    public String getServerUrl() {
        try {
            return CodeCheckerContext.getInstance().getServerObject(project).getServerUrl();
        } catch (Exception e) {
            return "";
        }
    }

    public IEclipsePreferences getProjectPreferences() {
        return projectPreferences;
    }

    public static IEclipsePreferences getGlobalPreferences() {
        return globalPreferences;
    }

    public IEclipsePreferences getActivePreferences() {
        if (getGlobal())
            return globalPreferences;
        else
            return projectPreferences;
    }

    public Map<ConfigTypes, String> getProjectConfig() {
        logger.log(Level.DEBUG, "returning project config");
        return getConfig(projectPreferences);
    }

    public static Map<ConfigTypes, String> getGlobalConfig() {
        logger.log(Level.DEBUG, "returning global config");
        return getConfig(globalPreferences);
    }

    public static Map<ConfigTypes, String> getDefaultConfig() {
        Map<ConfigTypes, String> ret = new HashMap<ConfigTypes, String>();
        for (ConfigTypes k : ConfigTypes.values()) {
            if (defaults.containsKey(k))
                ret.put(k, defaults.get(k));
            else
                ret.put(k, "");
        }
        return ret;
    }

    public static Map<ConfigTypes, String> getConfig(IEclipsePreferences pref) {
        Map<ConfigTypes, String> ret = new HashMap<ConfigTypes, String>();
        for (ConfigTypes k : ConfigTypes.values()) {
            if (defaults.containsKey(k))
                ret.put(k, defaults.get(k));
            else
                ret.put(k, "");
        }

        try {
            for (String configKey : pref.keys()) {
                for (ConfigTypes c : configKeys.keySet()) {
                    if (configKey.equals(configKeys.get(c))) {
                        ret.put(c, pref.get(configKey, ""));
                    }
                }
            }
        } catch (BackingStoreException e) {
        }


        for (ConfigTypes c : configKeys.keySet()) {
            logger.log(Level.DEBUG, "Config " + configKeys.get(c) + ":" + pref.get(configKeys.get(c), ""));
        }

        return ret;
    }

    public Map<ConfigTypes, String> getActiveConfig() {
        if (getGlobal())
            return getGlobalConfig();
        else
            return getProjectConfig();
    }

    public void dumpConfig(Map<ConfigTypes, String> config) {
        logger.log(Level.DEBUG, "Config:");
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet()) {
            logger.log(Level.DEBUG, configKeys.get(entry.getKey()) + ":" + entry.getValue());
        }
    }

    public boolean getGlobal() {
        return (projectPreferences.get(configKeys.get(ConfigTypes.IS_GLOBAL), "true").equals("true"));
    }

    public void updateProject(Map<ConfigTypes,String> newConfig){
        for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet())
        {            
            logger.log(Level.DEBUG, "updateProject(): " + configKeys.get(entry.getKey()) +":"+entry.getValue());
            projectPreferences.put(configKeys.get(entry.getKey()), entry.getValue());
        }
        try {
            projectPreferences.flush();
            updateServer(project, CodeCheckerContext.getInstance().getServerObject(project));
        } catch (BackingStoreException e) {
        }
    }

    // adds invariable configuration
    public  void addConstatConfig(){
        String workDir=ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+ "/.codechecker/"+
                project.getName();
    }

    public static boolean isCDTProject(IProject p) {
        return CoreModel.getDefault().getProjectDescription(p, true) != null;
    }

    public static void updateGlobal(Map<ConfigTypes, String> newConfig) {
        for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet()) {            
            logger.log(Level.DEBUG, "updateGlobal(): " + configKeys.get(entry.getKey()) + ":" + entry.getValue());
            globalPreferences.put(configKeys.get(entry.getKey()), entry.getValue());
        }

        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects) {
            try {
                if (project.hasNature(CodeCheckerNature.NATURE_ID) && isCDTProject(project)) {
                    CcConfiguration cc = new CcConfiguration(project);
                    if (cc.getGlobal()) {
                        cc.updateServer(project, CodeCheckerContext.getInstance().getServerObject(project));
                    }
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
            }
        }
        try {
            globalPreferences.flush();
        } catch (BackingStoreException e) {
        }
    }

    public String convertFilenameToServer(String projectRelativeFile) {
        return getLocationPrefix() + projectRelativeFile;
    }

    public String getLocationPrefix() {
        // TODO Auto-generated method stub
        return project.getLocation().toOSString() + "/";
    }

    public String convertFilenameFromServer(String serverFile) {
        if (getLocationPrefix().equals("")) {
            return serverFile;
        }
        return serverFile.replace(getLocationPrefix(), "");
    }

    public boolean isConfigured() {
        try {
            if (project.hasNature(CodeCheckerNature.NATURE_ID) && isCDTProject(project)) {
                CodeCheckEnvironmentChecker ccec = CodeCheckerContext.getInstance().getServerObject(project)
                        .getCodecheckerEnvironment();
                if (ccec != null) {
                    return ccec.isJavaRunner(CodeCheckerContext.getInstance().getServerObject(project).serverPort);
                }
                return false;
            }
            return false;
        } catch (CoreException e) {
            return false;
        }
    }

    public void updateServer(IProject project, CodecheckerServerThread server) {
        logger.log(Level.DEBUG, "Updating Server" + project.getName());
        Map<ConfigTypes, String> config = getActiveConfig();
        config.put(ConfigTypes.CHECKER_WORKSPACE, ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
                + "/.codechecker/" + project.getName());// codechecker
        // workspace
        dumpConfig(config);
        try {
            File workDir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/.codechecker/"
                    + project.getName());
            if (!workDir.exists()) {
                workDir.mkdir();
            }
            logger.log(Level.INFO, "Restarting server : " + project.getName());
            CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(config);
            modifyProjectEnvironmentVariables(project, ccec.getEnvironmentAddList());
            server.setCodecheckerEnvironment(ccec);
            ConsoleFactory.consoleWrite(
                    project.getName() + ":  CodeChecker server listening on port: " + server.serverPort);

        } catch (Exception e) {
            ConsoleFactory.consoleWrite(project.getName() + ": Failed to start server " + e.getStackTrace().toString());            
            e.printStackTrace();
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }

}
