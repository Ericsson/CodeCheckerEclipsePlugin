package cc.codechecker.plugin;

import cc.codechecker.api.runtime.LogI;;

//logger service for non plugin internal
//components (such as the codechecker service)
public class ExternalLogger implements LogI{
    public void log(int severity,String message){
        Logger.extLog(severity, message);        
    }
}
