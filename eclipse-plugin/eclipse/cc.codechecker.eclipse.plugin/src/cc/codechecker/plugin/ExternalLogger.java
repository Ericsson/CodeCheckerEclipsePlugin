package cc.codechecker.plugin;

import cc.codechecker.api.runtime.LogI;
import cc.codechecker.plugin.views.console.ConsoleFactory;;

//logger service for non plugin internal
//components (such as the codechecker service)
public class ExternalLogger implements LogI{
    public void log(int severity,String message){
        Logger.extLog(severity, message);        
    }
    //logs to CodeChecker console
    public void consoleLog(String message){
        ConsoleFactory.consoleWrite(message);
    }

}
