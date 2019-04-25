package org.codechecker.eclipse.plugin.runtime;

import java.io.File;
import java.util.Map;

import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.config.global.CcGlobalConfiguration;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;


/**
 * This class checks for Environments used by CodeChecker.
 *
 */
public class CodeCheckEnvironmentChecker {

    public final Optional<String> pythonEnvironment;
    public final String checkerDir; // root directory of CodeChecker
    public final String codeCheckerCommand; // CodecCheker executable path   
    private Map<ConfigTypes,String> config;
    private CodeCheckerProject project;
    private String checkerList;

	//with specific python. This
    // can be used to run CodeChecker
    public final ImmutableMap<String, String> environmentBefore; 

    /**
     * 
     * @param project
     *            The project that will be used for constructing the Environment
     *            checker.
     */
    public CodeCheckEnvironmentChecker(CodeCheckerProject project) {
        this.project = project;
        if (project != null)
            config = project.getCurrentConfig().get();
        // This is bad design, but until further refactoring it should do.
        else
            config = CcGlobalConfiguration.getInstance().get();

        if (!config.containsKey(ConfigTypes.PYTHON_PATH)
                || (config.containsKey(ConfigTypes.PYTHON_PATH) && config.get(ConfigTypes.PYTHON_PATH).isEmpty())){
            pythonEnvironment=Optional.absent();
            SLogger.log(LogI.INFO, "pythonenv is not set");
        }
        else{
            SLogger.log(LogI.INFO, "pythonenv is set to:"+config.get("PYTHON_PATH"));
            pythonEnvironment=Optional.of(config.get(ConfigTypes.PYTHON_PATH));
        }

        //checkerList=getConfigValue(ConfigTypes.CHECKER_LIST);
        checkerDir=getConfigValue(ConfigTypes.CHECKER_PATH);
        environmentBefore = getInitialEnvironment(pythonEnvironment);
        codeCheckerCommand = checkerDir+"/bin/CodeChecker";
    }

    /**
     * @return The Config thats used in the CodeCheckEnvironmentChecker.
     */
    public Map<ConfigTypes, String> getConfig() {
        return config;
    }

    /**
     * @param key
     *            The config key for the interesting value.
     * @return The value for the key or an empty String if it can't be found.
     */
    private String getConfigValue(ConfigTypes key) {
        if (config.containsKey(key))
            return config.get(key);
        else
            return "";
    }

    /**
     * Checks if the given path to CodeChecker is valid.
     * @param config The Configuration to be used, 
     *              populated with {@link ConfigTypes}.
     * @param codeCheckerBinaryPath Path to CodeChecker.
     */
    public static void getCheckerEnvironment(
            Map<ConfigTypes, String> config, String codeCheckerBinaryPath) {

        ShellExecutorHelper she = new ShellExecutorHelper(
                getInitialEnvironment(Optional.of(config.get(ConfigTypes.PYTHON_PATH))));

        String cmd=codeCheckerBinaryPath + " -h";
        SLogger.log(LogI.INFO, "Testing " + cmd);
        Optional<String> ccEnvOutput = she.quickReturnOutput(cmd);
        double test = 0;
        // WTF
        while(!ccEnvOutput.isPresent() && test <= 2){
            ccEnvOutput = she.quickReturnOutput(cmd, Math.pow( 2.0 , test ) * 1000);
            ++test;
        } 
        if (!ccEnvOutput.isPresent()) {
            SLogger.log(LogI.ERROR, "Cannot run CodeChecker command:"+cmd);
            throw new IllegalArgumentException("Couldn't run the specified CodeChecker for " +
                    "environment testing!");
        }
    }

    /**
     * Returns new environment if using Python virtual environment or System env if not.
     * @param pythonEnvironment Path to Python virtual environment activator.
     * @return The environment to be used.
     */
    private static ImmutableMap<String, String> getInitialEnvironment(Optional<String> pythonEnvironment) {
        if (pythonEnvironment.isPresent()) {
            ShellExecutorHelper she = new ShellExecutorHelper(System.getenv());

            Optional<String> output = she.quickReturnOutput("source " + pythonEnvironment.get() + "/bin/activate" + 
                    " ; env");
            if (!output.isPresent()) {
                SLogger.log(LogI.INFO, "SERVER_GUI_MSG >> Couldn't check the given python environment!");
                throw new IllegalArgumentException("Couldn't check the given python environment!");
            } else {
                ImmutableMap<String, String> environment = (new EnvironmentParser()).parse(output
                        .get());
                return environment;
            }

        } else {
            SLogger.log(LogI.INFO, "Python Env not specified. Using original system env.");
            return ImmutableMap.copyOf(System.getenv());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) {
            return false;
        }
        CodeCheckEnvironmentChecker other = (CodeCheckEnvironmentChecker) obj;
        return this.config.equals(other.getConfig());
    }

    public void setCheckerList(String list) {
        this.checkerList = list;
    }

    /**
     * Creates a Codechecker analyze command.
     * @param buildLog Path to the compile commands file, which the analyze command uses.
     * @return The constructed analyze command.
     */
    public String createAnalyzeCommmand(String buildLog){
        return codeCheckerCommand + " analyze " + getConfigValue(ConfigTypes.CHECKER_LIST) +
             " -j "+ getConfigValue(ConfigTypes.ANAL_THREADS) + " -n javarunner" +
             " -o "+ project.getLogFileLocation().getParent().toString() +"/results/ " + buildLog;
    }

    /**
     * Executes CodeChecker check command
     * on the build log received in the fileName parameter.
     * @param fileName Build log in the http://clang.llvm.org/docs/JSONCompilationDatabase.html format.
     * @param logToConsole Flag for indicating console logging
     * @param monitor ProgressMonitor for to be able to increment progress bar.
     * @param taskCount How many analyze step to be taken.
     * @return CodeChecker check command output
     */
    public String processLog(String fileName, boolean logToConsole, IProgressMonitor monitor, int taskCount) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);
        String cmd = createAnalyzeCommmand(fileName);

        SLogger.log(LogI.INFO, "SERVER_SER_MSG >> processLog >> "+ cmd);
        //Optional<String> ccOutput = she.waitReturnOutput(cmd,logToConsole);
        Optional<String> ccOutput = she.progressableWaitReturnOutput(cmd,logToConsole, monitor, taskCount);
        if (ccOutput.isPresent()) {
            // assume it succeeded, and delete the log file...
            File f = new File(fileName);
            f.delete();
        }
        return ccOutput.or("");
    }

    public String getCheckerList() {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);
        String cmd = codeCheckerCommand + " checkers";
        Optional<String> ccOutput = she.waitReturnOutput(cmd,false);
        return ccOutput.or("");
    }
}
