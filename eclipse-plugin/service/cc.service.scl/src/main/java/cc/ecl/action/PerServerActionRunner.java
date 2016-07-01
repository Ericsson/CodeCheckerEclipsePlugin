package cc.ecl.action;

import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Specific action runner which handles same interface actions in their own queue for multiple
 * servers.
 *
 * This implementation manages it's own threads.
 */
public class PerServerActionRunner<CommT> implements ActionRunner<CommT> {

    private final static Logger LOGGER = Logger.getLogger(PerServerActionRunner.class.getName());
    private final HashMap<String, RunnerThread<CommT>> servers;
    private final CommT communicationInterface;
    private final ActionRunnerFactory<CommT> runnerFactory;

    public PerServerActionRunner(CommT communicationInterface, ActionRunnerFactory<CommT>
            runnerFactory) {
        servers = new HashMap<>();
        this.communicationInterface = communicationInterface;
        this.runnerFactory = runnerFactory;
    }

    @Override
    public void queueAction(Action a, int priority, ActionStatusNotifier notifier) {
        if (!(a.getRequest() instanceof ServerRequest)) {
            LOGGER.severe("Queueing a non-server based action in the PerServerActionRunner: " + a
                    .getClass());
            throw new IllegalArgumentException("Non server based action");
        }

        String server = ((ServerRequest) a.getRequest()).getServer();

        if (!servers.containsKey(server)) {
            servers.put(server, new RunnerThread<CommT>(runnerFactory.createNewInstance
                    (communicationInterface)).start());
        }

        servers.get(server).queueAction(a, priority, notifier);
    }

    @Override
    public CommT getCommunicationInterface() {
        return communicationInterface;
    }

    @Override
    public ActionCacheFilter getCacheFilter() {
        return new MassCacheFilter();
    }

    @Override
    public void run() {
        // callers expect this method to newer return
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                for (RunnerThread rt : servers.values()) {
                    rt.interrupt();
                }
                break;
            }
        }
    }

    static class RunnerThread<CommT> {
        ActionRunner<CommT> runner;
        Thread runnerThread;

        public RunnerThread(ActionRunner<CommT> runner) {
            this.runner = runner;
            this.runnerThread = new Thread(runner);
        }

        public RunnerThread<CommT> start() {
            runnerThread.start();
            return this;
        }

        public void queueAction(Action a, int priority, ActionStatusNotifier notifier) {
            runner.queueAction(a, priority, notifier);
        }

        public void interrupt() {
            runnerThread.interrupt();
        }
    }

    class MassCacheFilter implements ActionCacheFilter {

        @Override
        public void remove(ActionParameterInfo actionType) {
            for (RunnerThread<CommT> rt : servers.values()) {
                rt.runner.getCacheFilter().remove(actionType);
            }
        }

        @Override
        public <ReqT, ResT> void remove(ActionParameterInfo actionType, ActionCache
                .ActionFilter<ReqT, ResT> filter) {
            for (RunnerThread<CommT> rt : servers.values()) {
                rt.runner.getCacheFilter().remove(actionType, filter);
            }
        }

        @Override
        public void removeAll() {
            for (RunnerThread<CommT> rt : servers.values()) {
                rt.runner.getCacheFilter().removeAll();
            }
        }
    }
}
