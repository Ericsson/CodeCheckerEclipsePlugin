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
    static CodecheckerServerThread ct = null;
    IProject project;
    IEclipsePreferences projectPreferences;
    public String CHECKER_COMMAND = "checker_command";

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

    public void modifyProjectEnvironmentVariables(final IProject project, final File dir) {
        IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager()
                .getContributedEnvironment();
        ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, true);
        ICConfigurationDescription cfgd = prjd.getActiveConfiguration();
        final String location = getCodecheckerDirectory();
        Map<String, String> environmentAdd = new HashMap<String, String>(){{
            put("LD_LIBRARY_PATH", location + "/ld_logger/lib");
            put("_", location + "/bin/CodeChecker");
            put("CC_LOGGER_GCC_LIKE", "gcc:g++:clang:cc:c++");
            put("LD_PRELOAD","ldlogger.so");
            put("CC_LOGGER_FILE", dir.toString() + "/" + project.getName() + "/compilation_commands.json.javarunner");
            put("CC_LOGGER_BIN", location + "/bin/ldlogger");
        }};
        if(getPythonEnv().isPresent()) {
            String pythonEnvironment = getPythonEnv().get();
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
            return "http://localhost:11444/";
        }
    }

    public String getCodecheckerDirectory() {
        return projectPreferences.get(CODECHECKER_DIRECTORY_KEY, "");
    }

    public Optional<String> getPythonEnv() {
        String s = projectPreferences.get(PYTHON_ENV_KEY, "");
        if (s.isEmpty()) {
            return Optional.absent();
        } else {
            s = s.replaceAll("/bin/activate", "").replaceAll("/bin", "");
            return Optional.of(s);
        }
    }

    public String getCheckerCommand() {
        return projectPreferences.get(CHECKER_COMMAND, "");
    }

    public void update(String serverUrl, String locationPrefix, String checkerCommand) {
        projectPreferences.put(CODECHECKER_DIRECTORY_KEY, serverUrl);
        projectPreferences.put(PYTHON_ENV_KEY, locationPrefix);
        projectPreferences.put(CHECKER_COMMAND, checkerCommand);

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
        return !getServerUrl().equals("");
    }

    public void updateServer(IProject project, CodecheckerServerThread server) {

        String location = getCodecheckerDirectory();
        try {
            File dir = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()
                    + "/.codechecker/");
            if(!dir.exists()) {
                dir.mkdir();
            }
            logger.log(Level.INFO, "SERVER_GUI_MSG >> Workdir : " + dir);
            String workspaceName = dir + "/" + project.getName();
            System.out.println(getCheckerCommand());
            CodeCheckEnvironmentChecker ccec = new CodeCheckEnvironmentChecker(getPythonEnv(),
                    location, workspaceName, getCheckerCommand());

            server.setCodecheckerEnvironment(ccec);

            modifyProjectEnvironmentVariables(project, dir);
            ConsoleFactory.consoleWrite(project.getName() + " complete to CodeChecker configuration and started server!");
        } catch (Exception e) {
            ConsoleFactory.consoleWrite(project.getName() + " failed to CodeChecker configuration and started server!");
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }

}
