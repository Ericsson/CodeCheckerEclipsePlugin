package cc.codechecker.plugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

import cc.codechecker.plugin.views.console.ConsoleFactory;


public class Logger {
    private static ILog logger = Activator.getDefault().getLog();
    public static void log(int level,String message){
        StackTraceElement s[] = Thread.currentThread().getStackTrace();
        if (s.length>2){
            StackTraceElement caller=s[2];
            String place=caller.getFileName()+":"+caller.getLineNumber()+" "+caller.getMethodName()+"(): ";
            logger.log(new Status(level,Activator.PLUGIN_ID,place+message));
        }
    }
    public static void extLog(int level,String message){
        StackTraceElement s[] = Thread.currentThread().getStackTrace();
        if (s.length>4){//we do not want to show the ExternalLogger.log function in the trace
            StackTraceElement caller=s[4];
            String place=caller.getFileName()+":"+caller.getLineNumber()+" "+caller.getMethodName()+"(): ";
            logger.log(new Status(level,Activator.PLUGIN_ID,place+message));
        }
    }
    //logs to CodeChecker console
    public static void consoleLog(String message){
        ConsoleFactory.consoleWrite(message);
    }
}
