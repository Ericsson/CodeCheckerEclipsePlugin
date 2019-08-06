package org.codechecker.eclipse.plugin.runtime;

import com.google.common.base.Optional;

import org.apache.commons.exec.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.Nullable;

import java.io.*;
import java.util.Map;
import java.util.Vector;

public class ShellExecutorHelper {

    private static final int DEFAULT_TIMEOUT = 1000; // in milliseconds
    private static final String WRAP_CHARACTER = "\"";

    final Map<String, String> environment;

    public ShellExecutorHelper(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * Executes the given bash script with a one sec time limit and returns it's first output line
     * from STDOUT.
     * @param cmd Bash script, executed with bash -c "{script}".
     * @param substitutionMap A <String, File> map for substituting the paths in the commands. use '${FILE}' for full
     * compatibility.
     * @return The first line of the script's output in an Optional wrapper.
     */
    public Optional<String> quickReturnFirstLine(String cmd, @Nullable Map<String, File> substitutionMap) {
        Executor ec = build(DEFAULT_TIMEOUT);
        try {
            OneLineReader olr = new OneLineReader();
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(cmd, substitutionMap), environment);
            return olr.getLine();
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Returns the full output.
     * @param cmd Bash script, executed with bash -c "{script}".
     * @param substitutionMap A <String, File> map for substituting the paths in the commands. use '${FILE}' for full
     * compatibility.
     * @return The script's output in an Optional wrapper.
     */
    public Optional<String> quickReturnOutput(String cmd, @Nullable Map<String, File> substitutionMap) {
        return this.quickReturnOutput(cmd, substitutionMap, DEFAULT_TIMEOUT);
    }
    
    /**
     * Returns the full output.
     * @param cmd Bash script, executed with bash -c "{script}".
     * @param substitutionMap A <String, File> map for substituting the paths in the commands. use '${FILE}' for full
     * compatibility.
     * @param timeOut The timeOut in Milliseconds.
     * @return The script's output in an Optional wrapper.
     */
    public Optional<String> quickReturnOutput(String cmd, @Nullable Map<String, File> substitutionMap, double timeOut) {
        Executor ec = build(new Double(timeOut).longValue());
        try {
            AllLineReader olr = new AllLineReader();
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(cmd, substitutionMap), environment);
            return Optional.of(olr.getOutput());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Returns the full output.
     * @param cmd Bash script, executed with bash -c "{script}".
     * @param substitutionMap A <String, File> map for substituting the paths in the commands. use '${FILE}' for full
     * compatibility.
     * @param logToConsole If true the execution log will appear on console log.
     * @return The script's output in an Optional wrapper.
     */
    public Optional<String> waitReturnOutput(String cmd, @Nullable Map<String, File> substitutionMap, boolean logToConsole) {
        Executor ec = build();
        try {
            AllLineReader olr = new AllLineReader(logToConsole);
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(cmd, substitutionMap), environment);
            return Optional.of(olr.getOutput());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Job {@link IProgressMonitor} compatible version of the standard
     * {@link #waitReturnOutput(String, Map, boolean) waitReturnOutput} method.
     * @param cmd Bash script, executed with bash -c "{script}".
     * @param substitutionMap A <String, File> map for substituting the paths in the commands. use '${FILE}' for full
     * compatibility.
     * @param logToConsole If true the execution log will appear on console log.
     * @param monitor The progress monitor that can be incremented.
     * @param taskCount The number of separate jobs.
     * @return The process return value as a String wrapped in an @link {@link Optional}.
     */
    public Optional<String> progressableWaitReturnOutput(String cmd, @Nullable Map<String, File> substitutionMap,
            boolean logToConsole, IProgressMonitor monitor, int taskCount) {
        Executor ec = build();
        try {
            AllLineReader olr = new ProgressableAllLineReader(logToConsole, monitor, taskCount);
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(cmd, substitutionMap), environment);
            return Optional.of(olr.getOutput());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Executes the given bash script with a one sec time limit and returns based on it's exit
     * status.
     * @param cmd Bash script, executed with bash -c "{script}".
     * @param substitutionMap A <String, File> map for substituting the paths in the commands. use '${FILE}' for full
     * compatibility.
     * @return true if successful
     */
    public boolean quickAndSuccessfull(String cmd, @Nullable Map<String, File> substitutionMap) {
        Executor ec = build(DEFAULT_TIMEOUT);
        try {
            ec.execute(buildScriptCommandLine(cmd, substitutionMap), environment);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Method for building Shell commands.
     * @param cmd The raw command to be parsed. All file paths that's included in the command should be in the form of
     *          ${FILE}, This way the path will be escaped correctly no matter what.
     * @param substitutionMap The aforementioned ${FILE} has to have a value associated with it, FILE:{@link File}.
     * @return The {@link CommandLine} object with proper escaping.
     */
    private CommandLine buildScriptCommandLine(String cmd, @Nullable Map<String, File> substitutionMap) {
        StringBuilder sb = new StringBuilder("/bin/bash -c ");
        sb.append(WRAP_CHARACTER);
        sb.append(cmd);
        sb.append(WRAP_CHARACTER);

        CommandLine cl = fixCommandLine(CommandLine.parse(sb.toString(), substitutionMap));
        return cl;
    }

    private Executor build() {
        return build(Optional.<String>absent());
    }

    private Executor build(long timeOutInMilliSec) {
        return build(Optional.<String>absent(), timeOutInMilliSec);
    }

    private Executor build(Optional<String> workingDirectory) {
        return build(workingDirectory, ExecuteWatchdog.INFINITE_TIMEOUT);
    }

    private Executor build(Optional<String> workingDirectory, long timeoutInMilliSec) {
        ExecuteWatchdog ew = new ExecuteWatchdog(timeoutInMilliSec);
        DefaultExecutor executor = new DefaultExecutor();
        //executor.setWorkingDirectory(new File(workingDirectory.or(".")));
        executor.setWatchdog(ew);
        return executor;
    }

    class PidObject {
        int pid;
    }

    class OneLineReader extends LogOutputStream {

        public Optional<String> line = Optional.absent();

        public Optional<String> getLine() {
            return line;
        }

        @Override
        protected void processLine(String s, int i) {
            if (!line.isPresent()) line = Optional.of(s);
        }
    }

    class AllLineReader extends LogOutputStream {
        private boolean logToConsole=false;
        public AllLineReader(){
            logToConsole=false;
        }
        public AllLineReader(boolean ltc){
            logToConsole = ltc;
        }

        public StringBuffer buffer = new StringBuffer();

        public String getOutput() {
            return buffer.toString();
        }        

        @Override
        protected void processLine(String s, int i) {
            buffer.append(s + "\n");
            if (logToConsole){
                SLogger.consoleLog(s);
            }
        }
    }

    /**
     * Extension of the standard {@link AllLineReader}, added {@link IProgressMonitor} as member.
     *
     */
    class ProgressableAllLineReader extends AllLineReader {

        private IProgressMonitor submonitor;

        /**
         * Constructor.
         */
        public ProgressableAllLineReader() {
            super();
        }

        /**
         *
         * @param ltc Log to Console - If true the execution log will appear on console log.
         * @param monitor The progress monitor that can be incremented.
         * @param taskCount The number of separate jobs.
         */
        public ProgressableAllLineReader(boolean ltc, IProgressMonitor monitor, int taskCount) {
            super(ltc);
            submonitor = SubMonitor.convert(monitor, taskCount);
        }

        @Override
        protected void processLine(String s, int i) {
            submonitor.setTaskName(s);
            int pos = s.indexOf("successfully");
            if (pos != -1) {
                submonitor.worked(1);
            }
            super.processLine(s, i);
        }
    }

    class ServerSLoggerReader extends LogOutputStream {

        boolean firstLine = true;
        private PidObject pidObject;

        public ServerSLoggerReader(PidObject pidObject) {
            this.pidObject = pidObject;
        }

        @Override
        protected void processLine(String s, int i) {

            if (firstLine) {
                // this is the pid!
                String[] a = s.split(" ");
                if (pidObject != null) {
                    pidObject.pid = Integer.parseInt(a[a.length - 1]);
                    SLogger.log(LogI.INFO, "SERVER_SER_MSG >> Server PID: " + pidObject.pid);
                }
                firstLine = false;
            }

            // TODO: log to file!
            SLogger.log(LogI.INFO, "SERVER_SER_MSG >> " + s);
        }
    }

    /**
     * Source: https://stackoverflow.com/a/45203609/8149485
     * Fixes wonky apache Commandline parser, where quoting is enabled in the parser.
     * @param badCommandLine {@link CommandLine} object with bad fields.
     * @return The fixed {@link CommandLine} object.
     */
    public static CommandLine fixCommandLine(CommandLine badCommandLine) {
        try {
            CommandLine fixedCommandLine = new CommandLine(badCommandLine.getExecutable());
            fixedCommandLine.setSubstitutionMap(badCommandLine.getSubstitutionMap());
            Vector<?> arguments = (Vector<?>) FieldUtils.readField(badCommandLine, "arguments", true);
            arguments.stream()
                    .map(badArgument -> {
                        try {
                            return (String) FieldUtils.readField(badArgument, "value", true);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(goodArgument -> fixedCommandLine.addArgument(goodArgument, false));
            return fixedCommandLine;
        } catch (IllegalAccessException e) {
            SLogger.log(LogI.ERROR, "Cannot fix bad command line");
            return badCommandLine;
        }
    }
}
