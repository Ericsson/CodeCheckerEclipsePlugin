package org.codechecker.eclipse.plugin.runtime;


public class SLogger {
    private static  LogI logger = null;
    public static void setLogger(LogI l){
        logger=l;
    }
    public static void log(int level,String message){
        if (logger!=null){
            logger.log(level,message);
        }
    }
    public static void consoleLog(String message){
        if (logger!=null){
            logger.consoleLog(message);
        }
    }
    
}
