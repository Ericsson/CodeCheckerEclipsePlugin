package cc.codechecker.api.runtime;

import cc.codechecker.api.config.Config.ConfigTypes;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.lang.Math;

public class CodeCheckEnvironmentChecker {

    private static final Logger logger = LogManager.getLogger(CodeCheckEnvironmentChecker.class.getName());

    public final Optional<String> pythonEnvironment;
    public final String checkerDir; // root directory of CodeChecker
    public final String codeCheckerCommand; // CodecCheker executable path   
    private Map<ConfigTypes,String> config;

    private String checkerList;

    public final ImmutableMap<String, String> environmentBefore; // with specific python. This
    // can be used to run CodeChecker
    //public final ImmutableMap<String, String> environmentDuringChecks; // this can be added to

    public Map<String, String> environmentAddList;

    public Map<String, String> getEnvironmentAddList() {
        return environmentAddList;
    }

    public  Map<ConfigTypes,String> getConfig(){
        return config;
    }

    private String getConfigValue(ConfigTypes key) {
        if (config.containsKey(key))
            return config.get(key);
        else
            return "";
    }

    public CodeCheckEnvironmentChecker(Map<ConfigTypes,String> config_m) {
        config=config_m;
        if (!config.containsKey(ConfigTypes.PYTHON_PATH) || (config.containsKey(ConfigTypes.PYTHON_PATH) && config.get(ConfigTypes.PYTHON_PATH).isEmpty())){
            pythonEnvironment=Optional.absent();
            logger.log(Level.DEBUG, "pythonenv is not set");
        }
        else{
            logger.log(Level.DEBUG, "pythonenv is set to:"+config.get("PYTHON_PATH"));
            pythonEnvironment=Optional.of(config.get(ConfigTypes.PYTHON_PATH));
        }

        checkerList=getConfigValue(ConfigTypes.CHECKER_LIST);
        checkerDir=getConfigValue(ConfigTypes.CHECKER_PATH);
        environmentBefore = getInitialEnvironment(pythonEnvironment);
        codeCheckerCommand = checkerDir+"/bin/CodeChecker";

        getCheckerEnvironment(environmentBefore,
                codeCheckerCommand);

        environmentAddList = new HashMap<String, String>(){{
            put("LD_LIBRARY_PATH", checkerDir + "/ld_logger/lib");
            put("_", checkerDir + "/bin/CodeChecker");
            put("CC_LOGGER_GCC_LIKE", getConfigValue(ConfigTypes.COMPILERS));
            put("LD_PRELOAD","ldlogger.so");
            put("CC_LOGGER_FILE", getConfigValue(ConfigTypes.CHECKER_WORKSPACE) + "/compilation_commands.json.javarunner");
            put("CC_LOGGER_BIN", checkerDir + "/bin/ldlogger");
        }};

        if(pythonEnvironment.isPresent()) {
            String pythonEnv = pythonEnvironment.get();
            environmentAddList.put("PATH", pythonEnv + "/bin:");
            environmentAddList.put("VIRTUAL_ENV", pythonEnv);
        }
    }


    private static void getCheckerEnvironment(
            ImmutableMap<String, String> environmentBefore, String codeCheckerCommand) {

        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);

        String cmd=codeCheckerCommand + " -h";
        logger.log(Level.DEBUG, "Testing " + cmd);
        Optional<String> ccEnvOutput = she.quickReturnOutput(cmd);
        double test = 0;
        while(!ccEnvOutput.isPresent() && test <= 2){
            ccEnvOutput = she.quickReturnOutput(cmd, Math.pow( 2.0 , test ) * 1000);
            ++test;
        } 
        if (!ccEnvOutput.isPresent()) {
            logger.log(Level.ERROR, "Cannot run CodeChecker command:"+cmd);
            throw new IllegalArgumentException("Couldn't run the specified CodeChecker for " +
                    "environment testing!");
        }
    }

    private static ImmutableMap<String, String> getInitialEnvironment(
            Optional<String> pythonEnvironment) {
        if (pythonEnvironment.isPresent()) {
            ShellExecutorHelper she = new ShellExecutorHelper(System.getenv());

            Optional<String> output = she.quickReturnOutput("source " + pythonEnvironment.get() + "/bin/activate" + 
                    " ; env");
            if (!output.isPresent()) {
                logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Couldn't check the given python environment!");
                throw new IllegalArgumentException("Couldn't check the given python environment!");
            } else {
                ImmutableMap<String, String> environment = (new EnvironmentParser()).parse(output
                        .get());
                return environment;
            }

        } else {
            logger.log(Level.DEBUG, "Python Env not specified. Using original system env.");
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

    public boolean isJavaRunner(int serverPort) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);

        String cmd = codeCheckerCommand + " cmd runs -p " + serverPort + " -o json";
        Optional<String> ccOutput = she.waitReturnOutput(cmd);
        if (ccOutput.isPresent()) {
            return ccOutput.get().contains("javarunner");
        }
        return false;
    }

    public void setCheckerList(String list) {
        this.checkerList = list;
    }

    public String getLogFileLocation() {
        return getConfigValue(ConfigTypes.CHECKER_WORKSPACE) + "/compilation_commands.json.javarunner";
    }

    // renames the logfile, to avoid concurrency issues
    public Optional<String> moveLogFile() {
        File f = new File(getLogFileLocation());
        if (f.exists()) {
            String newName = getLogFileLocation() + System.nanoTime();
            f.renameTo(new File(newName));

            return Optional.of(newName);
        }
        return Optional.absent();
    }

    public String createCheckCommmand(String buildLog){
        return codeCheckerCommand + " check " + getConfigValue(ConfigTypes.CHECKER_LIST) + "-j "+ getConfigValue(ConfigTypes.ANAL_THREADS) + " -n javarunner -w " + getConfigValue(ConfigTypes.CHECKER_WORKSPACE) + " -l " +
                buildLog;
    }

    public String createServerCommand(String port){
        return codeCheckerCommand + " server --not-host-only -w " + 
                getConfigValue(ConfigTypes.CHECKER_WORKSPACE) + " --view-port " + port;
    }


    /**
     * Executes CodeChecker check command
     * on the build log received in the fileName parameter.
     * @param fileName Build log in the http://clang.llvm.org/docs/JSONCompilationDatabase.html format.
     * @return CodeChecker check command output
     */
    public String processLog(String fileName) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);
        String cmd = createCheckCommmand(fileName);

        logger.log(Level.DEBUG, "SERVER_SER_MSG >> processLog >> "+ cmd);
        Optional<String> ccOutput = she.waitReturnOutput(cmd);

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
        Optional<String> ccOutput = she.waitReturnOutput(cmd);
        return ccOutput.or("");
    }


}
