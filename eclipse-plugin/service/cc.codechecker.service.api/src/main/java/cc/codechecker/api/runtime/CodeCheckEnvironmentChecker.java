package cc.codechecker.api.runtime;

import cc.codechecker.api.config.Config.ConfigTypes;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import cc.codechecker.api.runtime.SLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.lang.Math;

public class CodeCheckEnvironmentChecker {


    public final Optional<String> pythonEnvironment;
    public final String checkerDir; // root directory of CodeChecker
    public final String codeCheckerCommand; // CodecCheker executable path   
    private Map<ConfigTypes,String> config;
    private String checkerList;
    
    //with specific python. This
    // can be used to run CodeChecker
    public final ImmutableMap<String, String> environmentBefore; 
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
            SLogger.log(LogI.INFO, "pythonenv is not set");
        }
        else{
            SLogger.log(LogI.INFO, "pythonenv is set to:"+config.get("PYTHON_PATH"));
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
        SLogger.log(LogI.INFO, "Testing " + cmd);
        Optional<String> ccEnvOutput = she.quickReturnOutput(cmd);
        double test = 0;
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

    private static ImmutableMap<String, String> getInitialEnvironment(
            Optional<String> pythonEnvironment) {
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

    public boolean isJavaRunner(int serverPort) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);

        String cmd = codeCheckerCommand + " cmd runs -p " + serverPort + " -o json";
        Optional<String> ccOutput = she.waitReturnOutput(cmd,true);
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
    

/*
 *     
 *     drops codechecker Database
 *     
 */
    public void dropDB(){        
        String dbPath=getConfigValue(ConfigTypes.CHECKER_WORKSPACE)+"/codechecker.sqlite";        
        SLogger.log(LogI.INFO,"Dropping database:"+dbPath);
        File f = new File(dbPath);
        if (f.isFile()){
            if (!f.delete())
                SLogger.log(LogI.ERROR,"Cannot delte CodeChecker DB. "+dbPath);
        }
        else
            SLogger.log(LogI.ERROR,"Cannot delte CodeChecker DB. File not exists:"+dbPath);
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
    public String processLog(String fileName, boolean logToConsole) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);
        String cmd = createCheckCommmand(fileName);

        SLogger.log(LogI.INFO, "SERVER_SER_MSG >> processLog >> "+ cmd);
        Optional<String> ccOutput = she.waitReturnOutput(cmd,logToConsole);

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
