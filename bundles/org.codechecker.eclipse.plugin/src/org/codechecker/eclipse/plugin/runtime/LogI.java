package org.codechecker.eclipse.plugin.runtime;

public interface LogI {
    //IStatus severities
    static int CANCEL=8;
    static int ERROR=4;
    static int INFO=1;
    static int OK=0;
    static int WARNING=2;
    public void log(int severity,String message);
    public void consoleLog(String message);

}
