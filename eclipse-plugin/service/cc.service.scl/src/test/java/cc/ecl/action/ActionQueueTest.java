package cc.ecl.action;

import cc.ecl.action.mock.AddOneIntegerAction;
import cc.ecl.action.mock.AddOneIntegerActionImpl;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ActionQueueTest {

    ActionQueue queue;

    int notificationCount;

    @Before
    public void setupQueue() {
        queue = new ActionQueue();

        notificationCount = 0;

        AddOneIntegerActionImpl mai = new AddOneIntegerActionImpl();
        mai.setCommunicationInterface(1); // not nice

        queue.enqueue(1, new AddOneIntegerAction(1), mai, new MyActionListener());
        queue.enqueue(1, new AddOneIntegerAction(2), mai, new MyActionListener());

        queue.enqueue(2, new AddOneIntegerAction(3), mai, new MyActionListener());
        queue.enqueue(2, new AddOneIntegerAction(4), mai, new MyActionListener());

    }

    @Test
    public void testOrder() throws InterruptedException {
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(4)));
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(3)));
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(2)));
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(1)));
    }

    @Test
    public void testPutBackOrder() throws InterruptedException {

        queue.putBack(queue.take());

        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(3)));
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(4)));
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(2)));
        assertThat((Integer) queue.take().getAction().getRequest(), is(equalTo(1)));
    }

    @Test
    public void testRun() throws InterruptedException {
        ActionQueue.QueueItem qi = queue.take();
        AddOneIntegerAction ma = (AddOneIntegerAction) qi.getAction();

        assertThat(ma.getResult().isPresent(), is(false));
        qi.runAction(null);
        assertThat(ma.getResult().isPresent(), is(true));
        assertThat(ma.getResult().get(), is(equalTo(ma.getRequest() + 1)));
    }

    @Test
    public void testCompletionNotification() throws InterruptedException {
        ActionQueue.QueueItem qi = queue.take();
        AddOneIntegerAction ma = (AddOneIntegerAction) qi.getAction();

        qi.runAction(null);
        assertThat(notificationCount, is(equalTo(0)));
        qi.notifyListener();
        assertThat(notificationCount, is(equalTo(1)));
    }

    class MyActionListener implements ActionStatusNotifier {
        @Override
        public void onActionCompleted(Action action) {
            notificationCount++;
        }
    }
}

