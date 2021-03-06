package org.codechecker.eclipse.plugin.config.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.CcConfiguration;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase.ConfigurationChangedListener;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.config.EnvironmentVariables;
import org.codechecker.eclipse.plugin.config.global.CcGlobalConfiguration;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The CodeChecker project encapsulates the project level configuration,
 * (and reports in the future), for one Eclipse project.
 */
public class CodeCheckerProject implements ConfigurationChangedListener {
    public static final String COMPILATION_COMMANDS = "compilation_commands.json";
    public static final String MASTER_COMPILATION_COMMANDS = "master_compilation_commands.json";

    protected static final String STR_EMPTY = "";

    /**
     * The CodeChecker workspace is where the results folder will be created after an analyze.
     * Also the compilation commands is created here after the build.
     */
    private Path codeCheckerWorkspace;

    private boolean isGlobal = true;

    private final IProject project;

    private CcConfigurationBase current;
    // There is a local configuration for saving the config, when it is set to global.
    private final CcConfigurationBase local;

    private Map<EnvironmentVariables, String> environmentVariables = new HashMap<>();

    // These can't be final, because a project can be moved.
    private Path origCompCmd;
    private Path masterCompCmd;
    private Path tempCompCmd;

    //TODO Reports logically belongs to the projects. should add them here from the CodeCheckerContext.
    //private Map<IProject, SearchList> reports = new HashMap<>();

    /**
     * @param project The project thats the new instance is related to.
     */
    public CodeCheckerProject(IProject project) {
        this.project = project;
        codeCheckerWorkspace = Paths.get(
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(), ".codechecker", project.getName());

        origCompCmd = codeCheckerWorkspace.resolve(COMPILATION_COMMANDS);
        masterCompCmd = codeCheckerWorkspace.resolve(MASTER_COMPILATION_COMMANDS);
        
        local = new CcConfiguration(project);
        local.registerChangeListener(this);

        initProjectRelated();

        useGlobal(isGlobal);

        setEnvironment();
        updateProjectRelated();
        checkCodeCheckerReportDir();
    }

    /**
     * Deregisters from the Configurations.
     */
    public void cleanUp() {
        local.deRegisterChangeListener(this);
        getGlobal().deRegisterChangeListener(this);
    }

    /**
     * Initializes the CDT projects environment variables.
     */
    private void setEnvironment() {
        //Initialize to defaults.
        for (EnvironmentVariables ev : EnvironmentVariables.values()) {
            environmentVariables.put(ev, ev.getDefaultValue());
        }
        String checkerDir = current.get(ConfigTypes.CHECKER_PATH);
        String checkerRootDir = "";
        try {
            checkerRootDir = Paths.get(checkerDir).getParent().getParent().toAbsolutePath().toString();
        } catch (NullPointerException e) {
            checkerRootDir = checkerDir;
        }

        environmentVariables.put(EnvironmentVariables.LD_LIBRARY_PATH,
                checkerRootDir + EnvironmentVariables.LD_LIBRARY_PATH.getDefaultValue());
        environmentVariables.put(EnvironmentVariables._,
                checkerDir + EnvironmentVariables._.getDefaultValue());
        environmentVariables.put(EnvironmentVariables.CC_LOGGER_BIN,
                checkerRootDir + EnvironmentVariables.CC_LOGGER_BIN.getDefaultValue());
        environmentVariables.put(EnvironmentVariables.CC_LOGGER_GCC_LIKE,
                current.get(ConfigTypes.COMPILERS));
        // The current path to workspace is always handled by this project.
        environmentVariables.put(EnvironmentVariables.CC_LOGGER_FILE,
                Paths.get(codeCheckerWorkspace.toString(),
                        COMPILATION_COMMANDS).toString());

        modifyProjectEnvironmentVariables();
    }

    /**
     * Updates project related fields, and saves to the preferences.
     */
    private void updateProjectRelated() {
        IScopeContext context = new ProjectScope(project);
        IEclipsePreferences preferences = context.getNode(CodeCheckerNature.NATURE_ID);

        for (ConfigTypes ctp : ConfigTypes.PROJECT_TYPE){
            String value = null;
            switch (ctp) {
                case CHECKER_WORKSPACE:
                    value = codeCheckerWorkspace.toString();
                    break;
                case IS_GLOBAL:
                    value = Boolean.toString(isGlobal);
                    break;
                default:
                    break;
            }
            preferences.put(ctp.toString(), value);
        }
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            Logger.log(IStatus.ERROR, "Preferences cannot be saved!");
            e.printStackTrace();
        }
    }

    /**
     * Merges a build log to an other.
     */
    public void mergeCompilationLog() {
        if (!Files.exists(masterCompCmd, LinkOption.NOFOLLOW_LINKS))
            try {
                Files.copy(tempCompCmd, masterCompCmd);
                return;
            } catch (IOException e1) {
                Logger.log(IStatus.ERROR, "Couldn't copy master log file.");
            }

        Set<CompilationCommand> ccmds = null;
        Set<CompilationCommand> masteCcmds = null;

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try (JsonReader reader = new JsonReader(Files.newBufferedReader(tempCompCmd))) {
            ccmds = gson.fromJson(reader, new TypeToken<Set<CompilationCommand>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (JsonReader reader = new JsonReader(Files.newBufferedReader(masterCompCmd))) {
            masteCcmds = gson.fromJson(reader, new TypeToken<Set<CompilationCommand>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ccmds != null)
            masteCcmds.addAll(ccmds);

        try (JsonWriter writer = new JsonWriter(
                Files.newBufferedWriter(masterCompCmd, StandardOpenOption.TRUNCATE_EXISTING))) {
            writer.setIndent("\t");
            gson.toJson(masteCcmds, new TypeToken<Set<CompilationCommand>>() {
            }.getType(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        deleteTemporaryLogFile();
    }

    /**
     * Delete the specified logfile after the analysis.
     */
    public void deleteTemporaryLogFile() {
        try {
            Files.delete(tempCompCmd);
        } catch (IOException e) {
            Logger.log(IStatus.ERROR, "Couldn't delete the Temporary log file!" + tempCompCmd.toString());
        }
    }

    /**
     * Delete the specified logfile after the analysis.
     */
    public void deleteMasterLogFile() {
        try {
            Files.delete(masterCompCmd);
        } catch (IOException e) {
            Logger.log(IStatus.ERROR, "Couldn't delete the Master log file!" + masterCompCmd.toString());
        }
    }

    /**
     * Creates a copy of the log file created by ld logger, to avoid concurrency
     * issues.
     * 
     * @throws IOException
     *             Thrown when the copying fails.
     * @return The temporary compilation command.
     */
    public Path copyLogFile() throws IOException {
        if (Files.exists(origCompCmd, LinkOption.NOFOLLOW_LINKS))
            tempCompCmd = Files.move(origCompCmd,
                    origCompCmd.resolveSibling(origCompCmd.getFileName().toString() + System.nanoTime()),
                    StandardCopyOption.REPLACE_EXISTING);
        return tempCompCmd;
    }


    /**
     * Initializes project related fields.
     *  - Is the last used configuration was local or global.
     */
    private void initProjectRelated() {
        IScopeContext context = new ProjectScope(project);
        IEclipsePreferences preferences = context.getNode(CodeCheckerNature.NATURE_ID);

        isGlobal = preferences.getBoolean(ConfigTypes.IS_GLOBAL.toString(), true);
    }

    /**
     * @return The current config being used.
     */
    public CcConfigurationBase getCurrentConfig() { return current; }

    /**
     * Sets the configuration being used.
     * @param b If true global configuration ill be used, else the local.
     */
    public void useGlobal(boolean b) {
        if(b){
            current = CcGlobalConfiguration.getInstance();
            //register to global ConfigurationChanged
            if(current != null)
                current.registerChangeListener(this);
            isGlobal = b;
        } else {
            // Before changing to local deregister from global, but check because it can be null if
            // its a newly created object with a configuration initialized to local.
            if(current != null)
                current.deRegisterChangeListener(this);
            current = local;
            isGlobal = b;
        }
        setEnvironment();
        modifyProjectEnvironmentVariables();
    }

    /**
     * @return The global configuration.
     */
    public CcConfigurationBase getGlobal() {
        return CcGlobalConfiguration.getInstance();
    }

    /**
     * @return The local configuration.
     */
    public CcConfigurationBase getLocal() {
        return local;
    }

    /**
     * @return Returns the related project.
     */
    public IProject getRelatedProject() { return project; }

    /**
     * Adds to build environment variables, to be able to log the compilation commands with lldb.
     * @param environmentAdd
     */
    public void modifyProjectEnvironmentVariables() {
        IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager()
            .getContributedEnvironment();
        //we assume that the project is CDT
        ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(project, true);
        ICConfigurationDescription cfgd = prjd.getActiveConfiguration();

        //TODO Configlogger is for config types.
        //ConfigLogger configLogger = new ConfigLogger("Modified the build environment variables with:");
        environmentVariables.forEach((key, value) -> {
            ice.addVariable(key.toString(), value, key.getMethod(), STR_EMPTY, cfgd);
            //configLogger.append(key + ConfigLogger.SEP + environmentAdd.get(key));
        });
        //configLogger.log();

        try {
            CoreModel.getDefault().setProjectDescription(project, prjd);
        } catch (CoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
            Logger.log(IStatus.ERROR, e.getStackTrace().toString());
        }
    }

    /**
    *
    * @return Returns the newly generated analyze log location.
    */
    public Path getLogFileLocation() {
        return masterCompCmd;
        // return Paths.get(codeCheckerWorkspace.toString(),
        // COMPILATION_COMMANDS);
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
     * @return The projects location with platform independent pathSeparator appended to it.
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
        // TODO the root directory checking should be on plugin startup or when the workspace changes
        try {
            Files.createDirectory(codeCheckerWorkspace.getParent());
            Logger.log(IStatus.INFO, "CodeChecker root directory not found. Making root Codechecker directory ");
        } catch (IOException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
        }
        try {
            Files.createDirectory(codeCheckerWorkspace);
            Logger.log(IStatus.INFO, "Project directory for results not found. Making project directory");
        } catch (IOException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
        }
    }

    @Override
    public void configUpdated(Map<ConfigTypes, String> config) {
        updateProjectRelated();
        setEnvironment();
        modifyProjectEnvironmentVariables();
    }

    /**
     * Returns if a project uses the global configuration.
     * @return True if global, false if not.
     */
    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * Pojo for compilation command records. For the time being one
     * {@link ComilationCommand} equals the other if their file members equals.
     */
    private static class CompilationCommand {
        private String directory;
        private String command;
        private String file;

        /**
         * For debug purposes.
         */
        @Override
        public String toString() {
            return directory + ' ' + command + ' ' + file;
        }

        /**
         * Compilation command uniqueing.
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof CompilationCommand))
                return false;
            CompilationCommand cc = (CompilationCommand) o;
            if (Paths.get(file).isAbsolute())
                return file.equals(cc.file);
            else
                return (Paths.get(directory, file).toAbsolutePath().toString()
                        .equals(Paths.get(cc.directory, cc.file).toAbsolutePath().toString()));
        }

        /**
         * Because the equals method is overridden.
         */
        @Override
        public int hashCode() {
            if (Paths.get(file).isAbsolute())
                return file.hashCode();
            else
                return Paths.get(directory, file).toAbsolutePath().toString().hashCode();
        }
    }
}
