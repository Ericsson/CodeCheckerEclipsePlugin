package cc.codechecker.api.runtime;

import com.google.common.base.Optional;

import cc.codechecker.api.config.Config.ConfigTypes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

public class CodecheckerServerThread {
	
	//Logger
	private final static Logger logger = LogManager.getLogger(CodecheckerServerThread.class.getName());	
	
	private static Random random = new Random();
    public final int serverPort = random.nextInt(10000) + 15000;
    final BlockingQueue<String> processingQueue = new LinkedBlockingDeque<>();
    final Set currentlyRunning = Collections.synchronizedSet(new HashSet());
    CodeCheckEnvironmentChecker ccec = null;
    private OnCheckCallback callback = null;
    private ShellExecutorHelper.Executable serverExecutor = null;
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
        logger.log(Level.DEBUG, "setCodeCheckerEnvironment is called.");
        boolean restartNeeded = true;
        if (ccec!=null){
            Map<ConfigTypes, String> oldConfig = ccec.getConfig();
            Map<ConfigTypes, String> config=newEnv.getConfig();
            if (config.get(ConfigTypes.CHECKER_PATH).equals(oldConfig.get(ConfigTypes.CHECKER_PATH))
                    && config.get(ConfigTypes.PYTHON_PATH).equals(oldConfig.get(ConfigTypes.PYTHON_PATH)))
                restartNeeded = false;
        }

        this.ccec=newEnv;
        if (restartNeeded)
            start();//restart	
    }

    public synchronized void start() {
        if (running) stop();
        logger.log(Level.DEBUG, "SERVER_SER_MSG >> Starting CC");
        if (ccec != null && serverExecutor == null) {
            final String cmd = ccec.createServerCommand(String.valueOf(serverPort));
            ShellExecutorHelper she = new ShellExecutorHelper(ccec.environmentBefore);
            serverExecutor = she.getServerObject(cmd);
            serverThread = new Thread(new Runnable() {
                public void run() {
                	logger.log(Level.DEBUG, "SERVER_SER_MSG >> started server thread");
                    logger.log(Level.DEBUG, "SERVER_SER_MSG >> HTTP server command: " + cmd);
                    logger.log(Level.DEBUG, "SERVER_SER_MSG >> HTTP server URL: " + getServerUrl());
                    serverExecutor.start();
                }
            });
            serverThread.start();
        }

        queueThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.log(Level.DEBUG, "SERVER_SER_MSG >> started queue thread");
                    while (true) {
                    	if(Thread.interrupted()) {
                    		break;
                    	}
                        String s = processingQueue.take();
                        if(s.equals("STOP!")) {
                        	break;
                        }
                        if (currentlyRunning.add(s)) {
                        	callback.analysisStarted(ccec.createCheckCommmand(s));
                            logger.log(Level.DEBUG, "SERVER_SER_MSG >> Queue size (-1): " + processingQueue
                                    .size() + " >> " + s);
                            String checkResult=ccec.processLog(s);
                            logger.log(Level.INFO, "SERVER_SER_MSG >> " + checkResult);
                            currentlyRunning.remove(s);
                            if (callback != null) callback.analysisFinished(checkResult);
                        }
                    }
                } catch (InterruptedException e) {
                	logger.log(Level.ERROR, "SERVER_SER_MSG >> queueThread >> " + e);
                }
            }
        });
        queueThread.start();
        running = true;
    }

    public synchronized void stop() {
        logger.log(Level.DEBUG, "SERVER_SER_MSG >> stopping CC");
        if (serverExecutor != null) {
            logger.log(Level.DEBUG, "SERVER_SER_MSG >> killing server thread");
            serverExecutor.kill();
            serverThread.interrupt();
            serverThread = null;
            serverExecutor = null;
        }
        if (queueThread != null) {
            logger.log(Level.DEBUG, "SERVER_SER_MSG >> killing queue thread");
            processingQueue.add("STOP!");
            queueThread.interrupt();
            queueThread = null;
        }

        try {
            logger.log(Level.DEBUG, "SERVER_SER_MSG >> Waiting...");
            Thread.sleep(1000);
            logger.log(Level.DEBUG, "SERVER_SER_MSG >> Done");
        } catch (Exception e) {
        	logger.log(Level.ERROR, "SERVER_SER_MSG >> " + e);
        	logger.log(Level.DEBUG, "SERVER_SER_MSG >> " + e.getStackTrace());
        }

        running = false;
    }

    public void recheck() {
    	logger.log(Level.DEBUG, "Recheck called");
        if (ccec != null) {
            Optional<String> newF = ccec.moveLogFile();
            if (newF.isPresent()) {
                try {
                    processingQueue.put(newF.get());
                    logger.log(Level.DEBUG, "SERVER_SER_MSG >> Queue size (+1): " + processingQueue.size() +
                            " << " + newF.get());
                } catch (InterruptedException e) {
                	logger.log(Level.ERROR, "SERVER_SER_MSG >> " + e);
                	logger.log(Level.DEBUG, "SERVER_SER_MSG >> " + e.getStackTrace());
                }
            }
        }else
        	logger.log(Level.ERROR, "CodeChecker env is null!");
    }

    public String getServerUrl() {
        return "http://localhost:" + serverPort + "/";
    }

    public boolean isRunning() {
        return running;
    }
}
