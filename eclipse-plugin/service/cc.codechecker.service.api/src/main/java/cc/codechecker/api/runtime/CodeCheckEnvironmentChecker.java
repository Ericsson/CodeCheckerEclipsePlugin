package cc.codechecker.api.runtime;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.lang.Math;

public class CodeCheckEnvironmentChecker {

    private static final Logger logger = LogManager.getLogger(CodeCheckEnvironmentChecker.class.getName());

    public final Optional<String> pythonEnvironment;
    public final String codeCheckerParameter; // as specified by the user
    public final String codeCheckerCommand; // used by us
    public final String workspaceName;
    public String checkerCommand;

    public final ImmutableMap<String, String> environmentBefore; // with specific python. This
    // can be used to run CodeChecker
    public final ImmutableMap<String, String> environmentDuringChecks; // this can be added to
    // the build processes
    public final ImmutableList<EnvironmentDifference> environmentDifference;

    public CodeCheckEnvironmentChecker(Optional<String> pythonEnvironment,
            String codeCheckerParameter, String workspaceName, String checkerCommand) {
        this.pythonEnvironment = pythonEnvironment;
        this.codeCheckerParameter = codeCheckerParameter;
        this.workspaceName = workspaceName;
        this.checkerCommand = checkerCommand;

        environmentBefore = getInitialEnvironment(pythonEnvironment);

        codeCheckerCommand = getCodeCheckerCommand(environmentBefore, codeCheckerParameter);

        environmentDuringChecks = modifyLogfileName(getCheckerEnvironment(environmentBefore,
                codeCheckerCommand, workspaceName));
        EnvironmentDifferenceGenerator gen = new EnvironmentDifferenceGenerator();
        environmentDifference = (gen).difference(getInitialEnvironment(Optional.<String>absent())
                , environmentDuringChecks);
    }

    private static ImmutableMap<String, String> getCheckerEnvironment(
            ImmutableMap<String, String> environmentBefore, String codeCheckerCommand,
            String workspaceName) {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);

        logger.log(Level.DEBUG, "SERVER_SER_MSG >> " + codeCheckerCommand + " check -w " +
                workspaceName + " -n dummy -b env | grep =");
        Optional<String> ccEnvOutput = she.quickReturnOutput(codeCheckerCommand + " check -w " +
                workspaceName + " -n dummy -b env | grep =");
        double test = 1;
        do {
            ccEnvOutput = she.quickReturnOutput(codeCheckerCommand + " check -w " +
                    workspaceName + " -n dummy -b env | grep =", Math.pow( 2.0 , test ) * 1000);
            ++test;
        } while(!ccEnvOutput.isPresent() && test <= 2);
        if (!ccEnvOutput.isPresent() && test > 2) {
            logger.log(Level.ERROR, "Couldn't run the specified CodeChecker for " +
                    "environment testing!");
            throw new IllegalArgumentException("Couldn't run the specified CodeChecker for " +
                    "environment testing!");
        }

        return (new EnvironmentParser()).parse(ccEnvOutput.get());
    }

    private static ImmutableMap<String, String> getInitialEnvironment(
            Optional<String> pythonEnvironment) {
        if (pythonEnvironment.isPresent()) {
            ShellExecutorHelper she = new ShellExecutorHelper(System.getenv());

            if (!pythonEnvironment.get().endsWith("/bin/activate")) {
                pythonEnvironment = Optional.of(pythonEnvironment.get() + "/bin/activate");
            }
            Optional<String> output = she.quickReturnOutput("source " + pythonEnvironment.get() +
                    " ; env");

            if (!output.isPresent()) {
                logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Couldn't check the given python environment!");
                throw new IllegalArgumentException("Couldn't check the given python environment!");
            } else {
                ImmutableMap<String, String> environment = (new EnvironmentParser()).parse(output
                        .get());

                if (pythonEnvironment.isPresent()) {
                    // sanity check
                    ShellExecutorHelper originalEnv = new ShellExecutorHelper(System.getenv());
                    Optional<String> originalOutput = she.quickReturnOutput("source " +
                            pythonEnvironment.get() + " ; env");
                    ImmutableMap<String, String> originalEnvironment = (new EnvironmentParser())
                            .parse(originalOutput.get());

                    ImmutableList<EnvironmentDifference> diff = (new
                            EnvironmentDifferenceGenerator()).difference(originalEnvironment,
                                    environment);
                    if (diff.isEmpty()) {
                        //throw new RuntimeException("Python environment changes nothing:" +
                        // pythonEnvironment.get());
                    }
                }

                return environment;
            }

        } else {
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
        return Objects.equals(pythonEnvironment, other.pythonEnvironment) && Objects.equals
                (codeCheckerCommand, other.codeCheckerCommand) && Objects.equals(workspaceName,
                        other.workspaceName);
    }
    
    public void setCheckerCommand(String checkerCommand) {
        this.checkerCommand = checkerCommand;
    }

    public String getLogFileLocation() {
        return environmentDuringChecks.get("CC_LOGGER_FILE"); // hackis ...
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

    public String processLog(String fileName) { // returns the log
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);

        logger.log(Level.DEBUG, "SERVER_SER_MSG >> processLog >> "+ codeCheckerCommand + " check " + this.checkerCommand + " -n javarunner -w " + workspaceName + " -l " +
                fileName);
        String cmd = codeCheckerCommand + " check " + this.checkerCommand + " -n javarunner -w " + workspaceName + " -l " +
                fileName;

        Optional<String> ccOutput = she.waitReturnOutput(cmd);

        if (ccOutput.isPresent()) {
            // assume it succeeded, and delete the log file...
            File f = new File(fileName);
            f.delete();
        }

        return ccOutput.or("");
    }

    public String checkerList() {
        ShellExecutorHelper she = new ShellExecutorHelper(environmentBefore);

        String cmd = codeCheckerCommand + " checkers";

        Optional<String> ccOutput = she.waitReturnOutput(cmd);

        return ccOutput.or("");
    }

    private ImmutableMap<String, String> modifyLogfileName(
            ImmutableMap<String, String> checkerEnvironment) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        for (String key : checkerEnvironment.keySet()) {
            if (key.equals("CC_LOGGER_FILE")) {
            	logger.log(Level.DEBUG, "SERVER_SER_MSG >> modifyLogfileName >> " + key + " " + 
            				checkerEnvironment.get(key) + ".javarunner");
                builder.put(key, checkerEnvironment.get(key) + ".javarunner");
            } else {
                builder.put(key, checkerEnvironment.get(key));
            }
        }
        return builder.build();
    }

    private String getCodeCheckerCommand(ImmutableMap<String, String> pythonEnvironment,
            String codeCheckerParameter) {
        ShellExecutorHelper she = new ShellExecutorHelper(pythonEnvironment);

        logger.log(Level.DEBUG, "SERVER_SER_MSG >> getCodeCheckerCommand >> " + codeCheckerParameter + "/bin/CodeChecker");
        String codeCheckerPath = codeCheckerParameter + "/bin/CodeChecker";
        CodeCheckerLocator locator = null;

        try {
            locator = new CodeCheckerLocator(she, Optional.of(codeCheckerPath));
        } catch (IOException e) {
            logger.log(Level.ERROR, "SERVER_SER_MSG >> Error wile locating CodeChecker! " + e);
            throw new RuntimeException("Error while locating CodeChecker!");
        }

        if (!locator.getRunnerCommand().isPresent()) {
            logger.log(Level.ERROR, "SERVER_SER_MSG >> CodeChecker not found: " + codeCheckerParameter);
            throw new IllegalArgumentException("CodeChecker not found: " + codeCheckerParameter);
        }

        return locator.getRunnerCommand().get();
    }

}
