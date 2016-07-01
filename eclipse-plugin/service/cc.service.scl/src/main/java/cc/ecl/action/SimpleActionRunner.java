package cc.ecl.action;

import com.google.common.base.Optional;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class responsible for running actions.
 *
 * This is a basic single threaded implementation: runs a single queue, one action at a time.
 */
public class SimpleActionRunner<CommT> implements ActionRunner<CommT>, InnerRunner {

    private final static Logger LOGGER = Logger.getLogger(SimpleActionRunner.class.getName());

    private final CommT communicationInterface;

    private final ActionCache cache;

    private final ActionQueue queue;

    private final ActionImplementationRegistry implementationRegistry;

    public SimpleActionRunner(CommT communicationInterface, ActionImplementationRegistry
            implementationRegistry) {
        this.communicationInterface = communicationInterface;
        this.implementationRegistry = implementationRegistry;
        this.cache = new SimpleActionCache();
        this.queue = new ActionQueue();

        implementationRegistry.registerToRunner(this);
    }

    @Override
    public void queueAction(Action a, int priority, ActionStatusNotifier notifier) {
        ActionImpl impl = findImplementationFor(a.getParameterInfo());

        queue.enqueue(priority, a, impl, notifier);
    }

    /**
     * @return final result?
     */
    public boolean runAction(ActionQueue.QueueItem qi) {
        Optional<Action> fromCache = cache.get(qi.getAction());

        if (fromCache.isPresent()) {
            LOGGER.finest("Found action in cache");
            qi.setAction(fromCache.get());
        } else {
            LOGGER.finer("Running action");
            qi.runAction(this);
            LOGGER.finest("Finished action");
        }

        return qi.getAction().isConcreteResult();
    }

    @Override
    public CommT getCommunicationInterface() {
        return communicationInterface;
    }

    @Override
    public ActionCacheFilter getCacheFilter() {
        return cache;
    }

    /**
     * @todo Move to helper
     */
    private ActionImpl findImplementationFor(ActionParameterInfo parameterInfo) {
        Optional<ActionImpl> ai = implementationRegistry.getImplementationFor(parameterInfo);

        if (!ai.isPresent()) {
            LOGGER.severe("No implementation for action parameters: " + parameterInfo.toString());
            throw new IllegalArgumentException("No implementation for " + parameterInfo);
        }

        return ai.get();
    }


    @Override
    public void run() {

        int sleepDuration = 100;

        while (true) {
            try {
                ActionQueue.QueueItem qi = queue.take();

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Running action: " + qi.toString());
                }

                if (runAction(qi)) {
                    cache.add(qi.getAction());

                    try {
                        qi.notifyListener();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Listener crashed", e);
                    }
                    sleepDuration = 100;
                } else {

                    if (qi.getAction().getStatus() == ActionStatus.COMMUNICATION_ERROR) {
                        Thread.sleep(sleepDuration);
                        if (sleepDuration < 32000) sleepDuration *= 1.4;
                    }

                    queue.putBack(qi);
                }

            } catch (InterruptedException ie) {
                LOGGER.log(Level.INFO, "ActionRunner interrupted", ie);
                break;
            }
        }

    }

    @Override
    public <T extends Action> T requireResult(T otherAction) {

        LOGGER.fine("Running inner action: " + otherAction);

        if (otherAction.isConcreteResult()) {
            return otherAction;
        }

        Optional<T> fromCache = (Optional<T>) cache.get(otherAction);

        if (fromCache.isPresent()) {
            LOGGER.finest("Found action in cache");
            return fromCache.get();
        } else {
            Optional<ActionImpl> impl = implementationRegistry.getImplementationFor(otherAction
                    .getParameterInfo());

            if (!impl.isPresent()) {
                LOGGER.severe("No implementation for action parameters: " + otherAction
                        .getParameterInfo().toString());
                throw new IllegalArgumentException("No implementation for " + otherAction
                        .getParameterInfo());
            }

            LOGGER.finer("Running action");

            otherAction.runWithImpl(impl.get(), this);

            LOGGER.finest("Finished action");

            if (otherAction.isConcreteResult()) {
                cache.add(otherAction);
            }

            return otherAction;
        }
    }
}
