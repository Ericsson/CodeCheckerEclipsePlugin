package org.codechecker.eclipse.plugin.runtime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.config.global.CcGlobalConfiguration;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * This class checks for Environments used by CodeChecker.
 *
 */
public class CodeCheckEnvironmentChecker {

    private static final String CC_BINARY = "CC_BINARY";

    private static final String HELP_ARGUMENT = "-h";
    private static final int WAIT_TIME_MULTIPLYER = 1000; // in milliseconds
    
    public final Optional<String> pythonEnvironment;
    public final String checkerDir; // root directory of CodeChecker
    public final String codeCheckerCommand; // CodecCheker executable path   

    public final ImmutableMap<String, String> environmentBefore;

    public Map<String, File> commandSubstitutionMap;

    private Map<ConfigTypes,String> config;
    private CodeCheckerProject project;
    private String checkerList;

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
            SLogger.log(LogI.INFO, "pythonenv is set to:" + config.get(ConfigTypes.PYTHON_PATH));
            pythonEnvironment=Optional.of(config.get(ConfigTypes.PYTHON_PATH));
        }

        //checkerList=getConfigValue(ConfigTypes.CHECKER_LIST);
        checkerDir=getConfigValue(ConfigTypes.CHECKER_PATH);
        environmentBefore = getInitialEnvironment(pythonEnvironment);
        codeCheckerCommand = checkerDir+"/bin/CodeChecker";


        commandSubstitutionMap = new HashMap<String, File>() {{
                put("CC_BIN", new File(codeCheckerCommand));
        }};
        if (project != null)
            commandSubstitutionMap.put("RESULTS",
                    new File(project.getLogFileLocation().getParent().toString() + "/results/"));

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
     * TODO This method doesn't need codeCheckerBinaryPath in its arguments as it's a field in this class.
     */
    public static void getCheckerEnvironment(
            Map<ConfigTypes, String> config, String codeCheckerBinaryPath) {

        ShellExecutorHelper she = new ShellExecutorHelper(
                getInitialEnvironment(Optional.of(config.get(ConfigTypes.PYTHON_PATH))));

        String cmd = "'${CC_BINARY}' " + HELP_ARGUMENT;
        @SuppressWarnings("serial")
        Map<String, File> substitutinMap = new HashMap<String, File>() {{
                put(CC_BINARY, new File(codeCheckerBinaryPath));
        }};

        SLogger.log(LogI.INFO, "Testing " + substitutinMap.get(CC_BINARY).getAbsolutePath() + " -h");
        Optional<String> ccEnvOutput = she.quickReturnOutput(cmd, substitutinMap);
        double test = 0;
        // TODO WTF -- check the twisted logic behind this, and simplify.
        while(!ccEnvOutput.isPresent() && test <= 2){
            ccEnvOutput = she.quickReturnOutput(cmd, substitutinMap, Math.pow( 2.0 , test ) * WAIT_TIME_MULTIPLYER);
            ++test;
        }
        if (!ccEnvOutput.isPresent()) {
            SLogger.log(LogI.ERROR, "Cannot run CodeChecker command:" +
                    substitutinMap.get(CC_BINARY).getAbsolutePath() + " " + HELP_ARGUMENT);
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
            File pyEnv = new File(pythonEnvironment.get() + "/bin/activate");
            String cmd = "source '${PY_ENV}' ; env";
            @SuppressWarnings("serial")
            Map<String, File> substitutionMap = new HashMap<String, File>() {{ put("PY_ENV", pyEnv); }};
            Optional<String> output = she.quickReturnOutput(cmd, substitutionMap);
            if (!output.isPresent()) {
                Logger.log(IStatus.ERROR, "Couldn't check the python environment!");
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
    public String createAnalyzeCommmand(String buildLog) {
        commandSubstitutionMap.put("LOG", new File(buildLog));
        return "'${CC_BIN}' analyze " + getConfigValue(ConfigTypes.CHECKER_LIST) +
                " -j "+ getConfigValue(ConfigTypes.ANAL_THREADS) + " -n javarunner" +
                " -o " + "'${RESULTS}' " + "'${LOG}'";
    }

    /**
     * Executes CodeChecker check command
     * on the build log received in the fileName parameter.
     * @param buildLog Build log in the http://clang.llvm.org/docs/JSONCompilationDatabase.html format.
     * @param logToConsole Flag for indicating console logging
     * @param monitor ProgressMonitor for to be able to increment progress bar.
     * @param taskCount How many analyze step to be taken.
     * @return CodeChecker check command output
     */
    public String processLog(String buildLog, boolean logToConsole, IProgressMonitor monitor, int taskCount) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);
        String cmd = createAnalyzeCommmand(buildLog);
        SLogger.log(LogI.INFO, "SERVER_SER_MSG >> processLog >> "+ cmd);
        Optional<String> ccOutput = she.progressableWaitReturnOutput(cmd, commandSubstitutionMap, logToConsole, monitor, taskCount);
        if (ccOutput.isPresent()) {
            // assume it succeeded, and delete the log file...
            File f = new File(buildLog);
            f.delete();
        }
        return ccOutput.or("");
    }

    /**
     * Returns the list of available checkers.
     * Add the following for checker enability checking.
     * CodeChecker checkers --details | cut -d " " -f "1-3"
     * @return A String containing the checkers. One at a line.
     */
    public String getCheckerList() {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);
        String cmd = "'${CC_BIN}' checkers";
        Optional<String> ccOutput = she.waitReturnOutput(cmd, commandSubstitutionMap, false);
        return ccOutput.or("");
    }

}
