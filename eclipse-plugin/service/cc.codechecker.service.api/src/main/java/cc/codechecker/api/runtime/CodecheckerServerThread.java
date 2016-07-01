package cc.codechecker.api.runtime;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class CodecheckerServerThread {

    public final int serverPort = (new Random()).nextInt(10000) + 15000;
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
        System.out.println("SERVER-MGR> starting CC");
        if (ccec != null && serverExecutor == null) {
            final String cmd = ccec.codeCheckerCommand + " server --not-host-only -w " + ccec
                    .workspaceName + " --view-port " + serverPort;
            ShellExecutorHelper she = new ShellExecutorHelper(ccec.environmentBefore);
            serverExecutor = she.getServerObject(cmd);
            serverThread = new Thread(new Runnable() {
                public void run() {
                    System.out.println("SERVER-MGR> started server thread");
                    System.out.println("SERVER-MGR> HTTP server command: " + cmd);
                    System.out.println("SERVER-MGR> HTTP server URL: " + getServerUrl());
                    serverExecutor.start();
                }
            });
            serverThread.start();
        }

        queueThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("SERVER-MGR> started queue thread");
                    while (true) {
                        String s = processingQueue.take();
                        if (currentlyRunning.add(s)) {
                            System.out.println("SERVER-MGR> Queue size (-1): " + processingQueue
                                    .size() + " >> " + s);
                            System.out.println(ccec.processLog(s)); // TODO: logging!
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
        System.out.println("SERVER-MGR> stopping CC");
        if (serverExecutor != null) {
            System.out.println("SERVER-MGR> killing server thread");
            serverExecutor.kill();
            serverThread.interrupt();
            serverThread = null;
            serverExecutor = null;
        }
        if (queueThread != null) {
            System.out.println("SERVER-MGR> killing queue thread");
            queueThread.interrupt();
            queueThread = null;
        }

        try {
            System.out.println("waiting");
            Thread.sleep(2000);
            System.out.println("done");
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
                    System.out.println("SERVER-MGR> Queue size (+1): " + processingQueue.size() +
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
