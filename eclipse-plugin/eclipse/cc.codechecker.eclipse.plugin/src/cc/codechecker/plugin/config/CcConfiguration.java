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

import cc.codechecker.plugin.config.Config.ConfigTypes;
import cc.codechecker.plugin.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.plugin.runtime.CodecheckerServerThread;
import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.views.console.ConsoleFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cc.codechecker.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class CcConfiguration {

    //Logger
    IProject project=null;
    IEclipsePreferences projectPreferences=null;
    IEclipsePreferences globalPreferences = null;

    private  Map<ConfigTypes,String> defaults;//default config values
    private  Map<ConfigTypes,String> configKeys;//textual values of config variables

    public void init() {        
        globalPreferences = ConfigurationScope.INSTANCE.getNode(CodeCheckerNature.NATURE_ID);
        defaults=new HashMap<ConfigTypes,String>();
        defaults.put(ConfigTypes.COMPILERS,"gcc:g++:clang");
        defaults.put(ConfigTypes.ANAL_THREADS,"4");
        defaults.put(ConfigTypes.IS_GLOBAL,"true");

        configKeys=new HashMap<ConfigTypes,String>();
        configKeys.put(ConfigTypes.CHECKER_PATH,"global_server_url");
        configKeys.put(ConfigTypes.PYTHON_PATH,"location_prefix");
        configKeys.put(ConfigTypes.COMPILERS,"compilers");
        configKeys.put(ConfigTypes.ANAL_THREADS,"analthreads");
        configKeys.put(ConfigTypes.CHECKER_LIST,"global_checker_command");
        configKeys.put(ConfigTypes.IS_GLOBAL,"is_global");
        configKeys.put(ConfigTypes.CHECKER_WORKSPACE,"checker_workspace");        
    }

    public CcConfiguration() {
        init();        
    }
    public CcConfiguration(IProject project) {
        super();
        init();
        this.project = project;

        try {
            if (project.hasNature(CodeCheckerNature.NATURE_ID)) {
                IScopeContext context = new ProjectScope(project);
                projectPreferences = context.getNode(CodeCheckerNature.NATURE_ID);
            }
        } catch (CoreException e) {
            Logger.log(IStatus.ERROR, "" + e);
            Logger.log(IStatus.INFO, "" + e.getStackTrace());
        }
    }

    public void modifyProjectEnvironmentVariables(final Map<String, String> environmentAdd) {
        if (project!=null){
            IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager()
                    .getContributedEnvironment();
            //we assume that the project is CDT
            ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, true); 
            ICConfigurationDescription cfgd = prjd.getActiveConfiguration();
            for(String key : environmentAdd.keySet()) {
                if(key.equals("PATH")) {
                    ice.addVariable(key, environmentAdd.get(key), IEnvironmentVariable.ENVVAR_PREPEND,"", cfgd);
                } else {
                    ice.addVariable(key, environmentAdd.get(key), IEnvironmentVariable.ENVVAR_REPLACE,"", cfgd);
                }
            }
            try {
                CoreModel.getDefault().setProjectDescription(project, prjd);
            } catch (CoreException e) {
                Logger.log(IStatus.ERROR, "" + e);
                Logger.log(IStatus.INFO, "" + e.getStackTrace());
            }
        }
    }

    public String getServerUrl() {
        try {
            return CodeCheckerContext.getInstance().getServerObject(project).getServerUrl();
        } catch (Exception e) {
            return "";
        }
    }

    public IEclipsePreferences getActivePreferences(){
        if (project!=null && !isGlobal() )
            return 
                    projectPreferences;
        else
            return
                    globalPreferences;
    }

    public IEclipsePreferences getGlobalPreferences(){
        return globalPreferences;
    }


    public Map<ConfigTypes, String> getConfig() {
        if (project!=null && !isGlobal()){
            Logger.log(IStatus.INFO, "returning project config");
            return getConfigEclipse(projectPreferences);
        }
        else{
            Logger.log(IStatus.INFO, "returning global config");
            return getConfigEclipse(globalPreferences);            
        }

    }

    public Map<ConfigTypes, String> getDefaultConfig() {
        Map<ConfigTypes, String> ret = new HashMap<ConfigTypes, String>();
        for (ConfigTypes k:ConfigTypes.values()){
            if (defaults.containsKey(k))
                ret.put(k,defaults.get(k));
            else
                ret.put(k,"");
        }
        return ret;
    }

    private  Map<ConfigTypes, String> getConfigEclipse(IEclipsePreferences pref ) {
        Map<ConfigTypes, String> ret = new HashMap<ConfigTypes, String>();
        for (ConfigTypes k:ConfigTypes.values()){
            if (defaults.containsKey(k))
                ret.put(k,defaults.get(k));
            else
                ret.put(k,"");
        }

        try {
            for (String configKey : pref.keys()) {
                for (ConfigTypes c : configKeys.keySet()) {
                    if (configKey.equals(configKeys.get(c))){
                        ret.put(c, pref.get(configKey, ""));
                    }
                }
            }
        } catch (BackingStoreException e) {
        }


        return ret;
    }


    public void dumpConfig(Map<ConfigTypes, String> config) {
        Logger.log(IStatus.INFO, "Config:");
        for (Map.Entry<ConfigTypes, String> entry : config.entrySet()) {
            Logger.log(IStatus.INFO, configKeys.get(entry.getKey()) + ":" + entry.getValue());
        }
    }

    public boolean isGlobal() {
        if (project!=null)
            return (projectPreferences.get(configKeys.get(ConfigTypes.IS_GLOBAL), "true").equals("true"));
        else
            return true;
    }

    public void updateProject(Map<ConfigTypes,String> newConfig){
        for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet())
        {            
            Logger.log(IStatus.INFO, "updateProject(): " + configKeys.get(entry.getKey()) +":"+entry.getValue());
            projectPreferences.put(configKeys.get(entry.getKey()), entry.getValue());
        }
        try {
            projectPreferences.flush();
            updateServer(CodeCheckerContext.getInstance().getServerObject(project));
        } catch (BackingStoreException e) {
        }    	
    }


    //updates configuration in eclipse
    //and restarts the corresponding CodeChecker server(s)
    public void updateConfig(Map<ConfigTypes, String> newConfig) {        
        if (project==null){//updating global configuration and all running servers
            for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet()) {            
                Logger.log(IStatus.INFO, "updateGlobal(): " + configKeys.get(entry.getKey()) + ":" + entry.getValue());
                globalPreferences.put(configKeys.get(entry.getKey()), entry.getValue());
            }

            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            for (IProject project : projects) {
                try {
                    if (project.hasNature(CodeCheckerNature.NATURE_ID) && isCDTProject(project) ) {
                        CcConfiguration cc = new CcConfiguration(project);
                        if (cc.isGlobal()) {
                            cc.updateServer(CodeCheckerContext.getInstance().getServerObject(project));
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
        }else{//updating the config of the current project only
            for (Map.Entry<ConfigTypes, String> entry : newConfig.entrySet())
            {            
                Logger.log(IStatus.INFO, "updateProject(): " + configKeys.get(entry.getKey()) +":"+entry.getValue());
                projectPreferences.put(configKeys.get(entry.getKey()), entry.getValue());
            }
            try {
                projectPreferences.flush();
                updateServer(CodeCheckerContext.getInstance().getServerObject(project));
            } catch (BackingStoreException e) {
            }       
        }
    }

    public static boolean isCDTProject(IProject p){
        return CoreModel.getDefault().getProjectDescription(p, true)!=null;
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
            if(project!=null && project.hasNature(CodeCheckerNature.NATURE_ID)&& isCDTProject(project) ) {
            	return true;
                /*CodeCheckEnvironmentChecker ccec = CodeCheckerContext.getInstance().getServerObject(project)
                        .getCodecheckerEnvironment();
                if (ccec != null) {
                    int port=CodeCheckerContext.getInstance().getServerObject(project).serverPort;
                    Logger.log(IStatus.INFO, "checking codechecker on port"+port);
                    return ccec.isJavaRunner(port);
                }else
                    Logger.log(IStatus.INFO, "CodeCheckerContext is null!");
                return false;*/
            }
            return false;
        } catch (CoreException e) {
            return false;
        }
    }

    public void updateServer(CodecheckerServerThread server) {
        if (project!=null){
            Logger.log(IStatus.INFO, "Updating Server" + project.getName());
            Map<ConfigTypes, String> config = getConfig();		
            config.put(ConfigTypes.CHECKER_WORKSPACE, ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
                    + "/.codechecker/" + project.getName());// codechecker workspace
            dumpConfig(config);
            try {
                File workDir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/.codechecker/"
                        + project.getName());
                if (!workDir.exists()) {
                    workDir.mkdir();
                }
                Logger.log(IStatus.INFO, "Restarting server : " + project.getName());
                CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(config);
                ccec.setServerPort(server.serverPort);
                modifyProjectEnvironmentVariables(ccec.getEnvironmentAddList());
                server.setCodecheckerEnvironment(ccec);
                ConsoleFactory.consoleWrite(
                        project.getName() + ":  CodeChecker server listening on port: " + server.serverPort);

            } catch (Exception e) {
                ConsoleFactory.consoleWrite(project.getName() + ": Failed to start server " + e.getStackTrace().toString());            
                e.printStackTrace();
                Logger.log(IStatus.ERROR, "" + e);
                Logger.log(IStatus.INFO, "" + e.getStackTrace());
            }
        }
    }
}
