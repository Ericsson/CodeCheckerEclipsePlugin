package cc.codechecker.plugin.runtime;

import com.google.common.base.Optional;

import org.apache.commons.exec.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import java.io.*;
import java.util.Map;

public class ShellExecutorHelper {
	
	
    final Map<String, String> environment;

    public ShellExecutorHelper(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * Executes the given bash script with a one sec time limit and returns it's first output line
     * from STDOUT.
     *
     * @param script Bash script, executed with bash -c "{script}"
     * @return first line of the script's output
     */
    public Optional<String> quickReturnFirstLine(String script) {
        Executor ec = build(1000);
        try {
            OneLineReader olr = new OneLineReader();
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(script), environment);
            return olr.getLine();
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Returns the full output.
     */
    public Optional<String> quickReturnOutput(String script) {
    	return this.quickReturnOutput(script, 1000);
    }
    
    public Optional<String> quickReturnOutput(String script, double timeOut) {
        Executor ec = build(new Double(timeOut).longValue());
        try {
            AllLineReader olr = new AllLineReader();
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(script), environment);
            return Optional.of(olr.getOutput());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    public Optional<String> waitReturnOutput(String script,boolean logToConsole) {
        Executor ec = build();
        try {
            AllLineReader olr = new AllLineReader(logToConsole);
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(script), environment);
            return Optional.of(olr.getOutput());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Job {@link IProgressMonitor} compatible version of the standard
     * {@link #waitReturnOutput(String,boolean) waitReturnOutput} method.
     * @param script The string that will be executed.
     * @param logToConsole If true the execution log will appear on console log.
     * @param monitor The progress monitor that can be incremented.
     * @param taskCount The number of separate jobs.
     * @return The process return value as a String wrapped in an @link {@link Optional}.
     */
    public Optional<String> progressableWaitReturnOutput(String script,boolean logToConsole,
            IProgressMonitor monitor, int taskCount) {
        Executor ec = build();
        try {
            AllLineReader olr = new ProgressableAllLineReader(logToConsole, monitor, taskCount);
            ec.setStreamHandler(new PumpStreamHandler(olr));
            ec.execute(buildScriptCommandLine(script), environment);
            return Optional.of(olr.getOutput());
        } catch (IOException e) {
            return Optional.absent();
        }
    }

    /**
     * Executes the given bash script with a one sec time limit and returns based on it's exit
     * status.
     *
     * @param script Bash script, executed with bash -c "{script}"
     * @return true if successful
     */
    public boolean quickAndSuccessfull(String script) {
        Executor ec = build(1000);
        try {
            ec.execute(buildScriptCommandLine(script), environment);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private CommandLine buildScriptCommandLine(String script) {
        CommandLine cl = new CommandLine("/bin/bash");
        cl.addArgument("-c");
        cl.addArgument(script, false);
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

    /*public class Executable {
        private final Executor executor;
        private final CommandLine cmdLine;
        private final PidObject pidObject;

        public Executable(Executor executor, CommandLine cmdLine, PidObject pidObject) {
            this.executor = executor;
            this.cmdLine = cmdLine;
            this.pidObject = pidObject;
        }

        public void kill() {
        	SLogger.log(LogI.INFO, "SERVER_SER_MSG >> Killing PID " + this.pidObject.pid);
            if (pidObject.pid > 1000) {
                // Slightly less AWFUL BASH MAGIC, which gets the pids of the pidObject process and
                //     all its descendant processes and kills them.
                // The pidObject process should always be the main CodeChecker process this plugin
                //     starts.
        		String cpid = waitReturnOutput("echo $(ps -o pid= --ppid \"" + pidObject.pid + "\")",false).get().replace("\n", "");
            	SLogger.log(LogI.INFO, "SERVER_SER_MSG >> Children CodeChecker PID is  " + cpid);
            	try {
            		waitReturnOutput("kill " + cpid,false);
            	} catch(Exception e) {}
            }
        }

        public void start() {
            try {
                executor.execute(cmdLine, environment);
            } catch (IOException e) {
            }
        }
    }
*/
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
}
