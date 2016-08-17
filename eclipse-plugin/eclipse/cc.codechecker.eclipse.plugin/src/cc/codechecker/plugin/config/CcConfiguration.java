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

import com.google.common.base.Optional;

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

    //Logger
    private static final Logger logger = LogManager.getLogger(CcConfiguration.class);

    public String CODECHECKER_DIRECTORY_KEY = "server_url";
    public String PYTHON_ENV_KEY = "location_prefix";
    public String CHECKER_COMMAND = "checker_command";
    public String IS_GLOBAL = "is_global";
    public static String GLOBAL_CODECHECKER_DIRECTORY_KEY = "global_server_url";
    public static String GLOBAL_PYTHON_ENV_KEY = "global_location_prefix";
    public static String GLOBAL_CHECKER_COMMAND = "global_checker_command";
    //static CodecheckerServerThread ct = null;
    IProject project;
    IEclipsePreferences projectPreferences;
    static final IEclipsePreferences globalPreferences = ConfigurationScope.INSTANCE.getNode(CodeCheckerNature.NATURE_ID);

    public CcConfiguration(IProject project) {
        super();

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

    public void modifyProjectEnvironmentVariables(final IProject project, final File dir, final String location) {
        IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager()
                .getContributedEnvironment();
        ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, true);
        ICConfigurationDescription cfgd = prjd.getActiveConfiguration();
        boolean pythonEnvPresent;
        if(getGlobal()) {
            pythonEnvPresent = getGlobalPythonEnv().isPresent();
        } else {
            pythonEnvPresent = getProjectPythonEnv().isPresent();
        }
        Map<String, String> environmentAdd = new HashMap<String, String>(){{
            put("LD_LIBRARY_PATH", location + "/ld_logger/lib");
            put("_", location + "/bin/CodeChecker");
            put("CC_LOGGER_GCC_LIKE", "gcc:g++:clang:cc:c++");
            put("LD_PRELOAD","ldlogger.so");
            put("CC_LOGGER_FILE", dir.toString() + "/" + project.getName() + "/compilation_commands.json.javarunner");
            put("CC_LOGGER_BIN", location + "/bin/ldlogger");
        }};
        if(pythonEnvPresent) {
            String pythonEnvironment;
            if(getGlobal()) {
                pythonEnvironment = getGlobalPythonEnv().get();
            } else {
                pythonEnvironment = getProjectPythonEnv().get();
            }
            environmentAdd.put("PATH", pythonEnvironment + "/bin:");
            environmentAdd.put("VIRTUAL_ENV", pythonEnvironment);
        }
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

    public String getProjectCodecheckerDirectory() {
        return projectPreferences.get(CODECHECKER_DIRECTORY_KEY, "");
    }
    
    public static String getGlobalCodecheckerDirectory() {
        return globalPreferences.get(GLOBAL_CODECHECKER_DIRECTORY_KEY, "");
    }

    public Optional<String> getProjectPythonEnv() {
        String s = projectPreferences.get(PYTHON_ENV_KEY, "");
        if (s.isEmpty()) {
            return Optional.absent();
        } else {
            s = s.replaceAll("/bin/activate", "").replaceAll("/bin", "");
            return Optional.of(s);
        }
    }

    public static Optional<String> getGlobalPythonEnv() {
        String s = globalPreferences.get(GLOBAL_PYTHON_ENV_KEY, "");
        if (s.isEmpty()) {
            return Optional.absent();
        } else {
            s = s.replaceAll("/bin/activate", "").replaceAll("/bin", "");
            return Optional.of(s);
        }
    }

    public String getProjectCheckerCommand() {
        return projectPreferences.get(CHECKER_COMMAND, "");
    }

    public static String getGlobalCheckerCommand() {
        return globalPreferences.get(GLOBAL_CHECKER_COMMAND, "");
    }

    public boolean getGlobal() {
        return projectPreferences.getBoolean(IS_GLOBAL, true);
    }

    public static void updateGlobal(String serverUrl, String locationPrefix, String checkerCommand) {
        globalPreferences.put(GLOBAL_CODECHECKER_DIRECTORY_KEY, serverUrl);
        globalPreferences.put(GLOBAL_PYTHON_ENV_KEY, locationPrefix);
        globalPreferences.put(GLOBAL_CHECKER_COMMAND, checkerCommand);
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for(IProject project : projects) {
            try {
                if(project.hasNature(CodeCheckerNature.NATURE_ID)) {
                    CcConfiguration cc = new CcConfiguration(project);
                    if(cc.getGlobal()) {
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

    public void updateProject(String serverUrl, String locationPrefix, String checkerCommand, boolean isGlobal) {
        projectPreferences.put(CODECHECKER_DIRECTORY_KEY, serverUrl);
        projectPreferences.put(PYTHON_ENV_KEY, locationPrefix);
        projectPreferences.put(CHECKER_COMMAND, checkerCommand);
        projectPreferences.putBoolean(IS_GLOBAL, isGlobal);
        try {
            projectPreferences.flush();

            updateServer(project, CodeCheckerContext.getInstance().getServerObject(project));
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
            if(project.hasNature(CodeCheckerNature.NATURE_ID)) {
                CodeCheckEnvironmentChecker ccec = CodeCheckerContext.getInstance().getServerObject(project).getCodecheckerEnvironment();
                if(ccec != null) {
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

        final String location;
        if(getGlobal()) {
            location = getGlobalCodecheckerDirectory();
        } else {
            location = getProjectCodecheckerDirectory();
        }
        try {
            File dir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
                    + "/.codechecker/");
            if(!dir.exists()) {
                dir.mkdir();
            }
            logger.log(Level.INFO, "SERVER_GUI_MSG >> Workdir : " + dir);
            String workspaceName = dir + "/" + project.getName();
            CodeCheckEnvironmentChecker ccec;
            if(getGlobal()) {
                ccec = new CodeCheckEnvironmentChecker(getGlobalPythonEnv(),
                        location, workspaceName, getGlobalCheckerCommand());
            } else {
                ccec = new CodeCheckEnvironmentChecker(getProjectPythonEnv(),
                        location, workspaceName, getProjectCheckerCommand());
            }

            server.setCodecheckerEnvironment(ccec);

            modifyProjectEnvironmentVariables(project, dir, location);
            ConsoleFactory.consoleWrite(project.getName() + " complete to CodeChecker configuration and started server!");
        } catch (Exception e) {
            ConsoleFactory.consoleWrite(project.getName() + " failed to CodeChecker configuration and started server!");
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }

}
