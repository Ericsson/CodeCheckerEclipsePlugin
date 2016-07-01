package cc.ecl.action;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Implements a simple threadsafe priority queue. Sors based on priority and date.
 */
public class ActionQueue {

    volatile long lastId;
    PriorityBlockingQueue<QueueItem> queue;

    public ActionQueue() {
        queue = new PriorityBlockingQueue<QueueItem>();
    }

    private synchronized long nextId() {
        return lastId++;
    }

    public void enqueue(int priority, Action action, ActionImpl impl, ActionStatusNotifier
            notifier) {
        queue.add(new QueueItem(priority, nextId(), action, impl, notifier));
    }

    public QueueItem take() throws InterruptedException {
        return queue.take();
    }

    /**
     * Called when an error happens, putting back with a greater id
     *
     * @param qi QueueItem
     */
    public void putBack(QueueItem qi) {
        qi.id = -nextId(); // places at the end of the same priority queue, is this really good?
        queue.add(qi);
    }

    /**
     * Queue item, with some action runner stuff mixed in.
     *
     * @todo refactor the mixed in stuff
     */
    public class QueueItem implements Comparable<QueueItem> {
        private int priority;
        private long id;

        private Action action;
        private ActionImpl impl;

        private ActionStatusNotifier notifier;

        private QueueItem(int priority, long id, Action action, ActionImpl impl,
                          ActionStatusNotifier notifier) {
            this.priority = priority;
            this.id = id;
            this.action = action;
            this.impl = impl;
            this.notifier = notifier;
        }

        @Override
        public int compareTo(QueueItem o) {
            if (priority != o.priority) {
                return Integer.compare(o.priority, priority);
            }
            return Long.compare(o.id, id);
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        public void notifyListener() {
            if (action.getStatus() == ActionStatus.SUCCEEDED) {
                notifier.onActionCompleted(action);
            }
            if (action.getStatus() == ActionStatus.LOGIC_ERROR) { // TODO: another callback
                notifier.onActionCompleted(action);
            }
        }

        public void runAction(InnerRunner innerRunner) {
            action.runWithImpl(impl, innerRunner);
        }

        @Override
        public String toString() {
            return action.toString() + " P: " + priority + "@" + id;
        }
    }
}
