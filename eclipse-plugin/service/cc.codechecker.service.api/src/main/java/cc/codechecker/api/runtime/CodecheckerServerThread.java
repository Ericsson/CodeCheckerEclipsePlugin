package cc.codechecker.api.runtime;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.HashSet;
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
    private OnCheckedCallback callback = null;
    private ShellExecutorHelper.Executable serverExecutor = null;
    private Thread serverThread = null;
    private Thread queueThread = null;
    private boolean running = false;

    public void setCallback(OnCheckedCallback callback) {
        this.callback = callback;
    }

    public CodeCheckEnvironmentChecker getCodecheckerEnvironment() {
        return ccec;
    }

    public void setCodecheckerEnvironment(CodeCheckEnvironmentChecker ccec) {
        if (this.ccec == null || !this.ccec.equals(ccec)) {
            this.ccec = ccec;
            if (running) start(); // restart
        }
    }

    public synchronized void start() {
        if (running) stop();
        logger.log(Level.DEBUG, "SERVER_SER_MSG >> Starting CC");
        if (ccec != null && serverExecutor == null) {
            final String cmd = ccec.codeCheckerCommand + " server --not-host-only -w " + ccec
                    .workspaceName + " --view-port " + serverPort;
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
                        String s = processingQueue.take();
                        if (currentlyRunning.add(s)) {
                            logger.log(Level.DEBUG, "SERVER_SER_MSG >> Queue size (-1): " + processingQueue
                                    .size() + " >> " + s);
                            logger.log(Level.DEBUG, "SERVER_SER_MSG >> " + ccec.processLog(s)); // TODO: logging!
                            currentlyRunning.remove(s);
                            if (callback != null) callback.built();
                        }
                    }
                } catch (InterruptedException e) {
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
            queueThread.interrupt();
            queueThread = null;
        }

        try {
            logger.log(Level.DEBUG, "SERVER_SER_MSG >> Waiting...");
            Thread.sleep(2000);
            logger.log(Level.DEBUG, "SERVER_SER_MSG >> Done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        running = false;
    }

    public void recheck() {
        if (ccec != null) {
            Optional<String> newF = ccec.moveLogFile();
            if (newF.isPresent()) {
                try {
                    processingQueue.put(newF.get());
                    logger.log(Level.DEBUG, "SERVER_SER_MSG >> Queue size (+1): " + processingQueue.size() +
                            " << " + newF.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getServerUrl() {
        return "http://localhost:" + serverPort + "/";
    }

    public boolean isRunning() {
        return running;
    }
}
