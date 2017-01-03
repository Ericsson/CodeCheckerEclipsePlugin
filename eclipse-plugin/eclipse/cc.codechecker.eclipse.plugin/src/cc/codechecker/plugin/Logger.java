package cc.codechecker.plugin;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

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
}
