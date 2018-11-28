package cc.codechecker.plugin.runtime;

import com.google.common.base.Optional;

import cc.codechecker.plugin.config.Config.ConfigTypes;
import cc.codechecker.plugin.config.Config.ConfigTypesCommon;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import cc.codechecker.plugin.runtime.CodeCheckEnvironmentChecker;


public class CodecheckerServerThread {
	
	
	private static Random random = new Random();
    public final int serverPort = random.nextInt(10000) + 15000;
    final BlockingQueue<String> processingQueue = new LinkedBlockingDeque<>();
    final Set currentlyRunning = Collections.synchronizedSet(new HashSet());
    CodeCheckEnvironmentChecker ccec = null;
    private OnCheckCallback callback = null;
    //TODO UPLIFT
    //private ShellExecutorHelper.Executable serverExecutor = null;
    private Thread serverThread = null;
    private Thread queueThread = null;
    private boolean running = false;

    public void setCallback(OnCheckCallback callback) {
        this.callback = callback;
    }

    public CodeCheckEnvironmentChecker getCodecheckerEnvironment() {
        return ccec;
    }

    public void setCodecheckerEnvironment(CodeCheckEnvironmentChecker newEnv) {
        SLogger.log(LogI.INFO, "setCodeCheckerEnvironment is called.");
        boolean restartNeeded = true;
        if (ccec!=null){
            Map<ConfigTypes, String> oldConfig = ccec.getConfig();
            Map<ConfigTypes, String> config=newEnv.getConfig();
            if (config.get(ConfigTypesCommon.CHECKER_PATH).equals(oldConfig.get(ConfigTypesCommon.CHECKER_PATH))
                    && config.get(ConfigTypesCommon.PYTHON_PATH).equals(oldConfig.get(ConfigTypesCommon.PYTHON_PATH)))
                restartNeeded = false;
        }

        this.ccec=newEnv;
        if (restartNeeded)
            start();//restart	
    }

    public synchronized void start() {
        if (running) stop();
        SLogger.log(LogI.INFO, "SERVER_SER_MSG >> Starting CC");
        					//TODO UPLIFT
        if (ccec != null /*&& serverExecutor == null*/) {
            //final String cmd = ccec.createServerCommand(String.valueOf(serverPort));
        	// TODO UPLIFT
        	final String cmd = "lel";
            ShellExecutorHelper she = new ShellExecutorHelper(ccec.environmentBefore);
            //serverExecutor = she.getServerObject(cmd);
            serverThread = new Thread(new Runnable() {
                public void run() {
                	// TODO UPLIFT
                	/*SLogger.log(LogI.INFO, "SERVER_SER_MSG >> started server thread");
                    SLogger.log(LogI.INFO, "SERVER_SER_MSG >> HTTP server command: " + cmd);
                    SLogger.log(LogI.INFO, "SERVER_SER_MSG >> HTTP server URL: " + getServerUrl());
                    SLogger.consoleLog("Server started on URL:" + getServerUrl());
                    serverExecutor.start();*/
                }
            });
            serverThread.start();
        }

        queueThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> started queue thread");
                    while (true) {
                    	if(Thread.interrupted()) {
                    		break;
                    	}
                        String s = processingQueue.take();
                        if(s.equals("STOP!")) {
                        	break;
                        }
                        if (currentlyRunning.add(s)) {
                        	callback.analysisStarted(ccec.createAnalyzeCommmand(s));
                            SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> Queue size (-1): " + processingQueue
                                    .size() + " >> " + s);
                            String checkResult=ccec.processLog(s,true);
                            SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> " + checkResult);
                            currentlyRunning.remove(s);
                            if (callback != null) callback.analysisFinished(checkResult);
                        }
                    }
                } catch (InterruptedException e) {
                	SLogger.log(LogI.ERROR, "QUEUE_THREAD_MSG >> queueThread >> " + e);
                }
            }
        });
        queueThread.start();
        running = true;
    }

    public synchronized void stop() {
    	//TODO UPLIFT
    	/*SLogger.log(LogI.INFO, "SERVER_SER_MSG >> stopping CC");
        if (serverExecutor != null) {
            SLogger.log(LogI.INFO, "SERVER_SER_MSG >> killing server thread");
            serverExecutor.kill();
            serverThread.interrupt();
            serverThread = null;
            serverExecutor = null;
        }*/
        if (queueThread != null) {
            SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> killing queue thread");
            processingQueue.add("STOP!");
            queueThread.interrupt();
            queueThread = null;
        }

        try {
            SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> Waiting...");
            Thread.sleep(1000);
            SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> Done");
        } catch (Exception e) {
        	SLogger.log(LogI.ERROR, "QUEUE_THREAD_MSG >> " + e);
        	SLogger.log(LogI.INFO, "QUEUE_THREAD_MSG >> " + e.getStackTrace());
        }

        running = false;
    }

    public void recheck() {
    	SLogger.log(LogI.INFO, "Recheck called");
        if (ccec != null) {
            Optional<String> newF = ccec.moveLogFile();
            if (newF.isPresent()) {
                try {
                    processingQueue.put(newF.get());
                    SLogger.log(LogI.INFO, "SERVER_SER_MSG >> Queue size (+1): " + processingQueue.size() +
                            " << " + newF.get());
                } catch (InterruptedException e) {
                	SLogger.log(LogI.ERROR, "SERVER_SER_MSG >> " + e);
                	SLogger.log(LogI.INFO, "SERVER_SER_MSG >> " + e.getStackTrace());
                }
            }
        }else
        	SLogger.log(LogI.ERROR, "CodeChecker env is null!");
    }

   /* public String getServerUrl() {
        return "http://localhost:" + serverPort + "/";
    }*/

    public boolean isRunning() {
        return running;
    }
}
