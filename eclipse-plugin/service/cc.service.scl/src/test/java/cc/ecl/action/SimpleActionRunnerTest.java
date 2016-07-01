package cc.ecl.action;

import cc.ecl.action.mock.AddOneIntegerAction;
import cc.ecl.action.mock.AddOneIntegerActionImpl;
import cc.ecl.action.mock.FailFirstCharAction;
import cc.ecl.action.mock.FailFirstCharActionImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SimpleActionRunnerTest {

    SimpleActionRunner<Object> sar;

    Thread sarRunner;

    int c1, c2, c3;

    public static SimpleActionRunner<Object> setupActionRunner() {
        ActionImplementationRegistry<Object> simpleRegistry = new
                ActionImplementationRegistry<Object>();
        AddOneIntegerActionImpl impl1 = new AddOneIntegerActionImpl();
        simpleRegistry.addImplementation(impl1);
        FailFirstCharActionImpl impl2 = new FailFirstCharActionImpl();
        simpleRegistry.addImplementation(impl2);

        return new SimpleActionRunner<Object>(1, simpleRegistry);
    }

    static boolean waitForResult(Action a, int timeout) {
        while (!a.isConcreteResult()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            timeout -= 100;
            if (timeout <= 0) {
                return false;
            }
        }
        return true;
    }

    @Before
    public void startSar() {
        sar = setupActionRunner();

        sarRunner = new Thread(sar);

        c1 = 0;
        c2 = 0;
        c3 = 0;
    }

    @After
    public void stopSar() {
        if (sarRunner != null && sarRunner.isAlive() && !sarRunner.isInterrupted()) {
            sarRunner.interrupt();
        }
        sarRunner = null;
        sar = null;
    }

    @Test
    public void testReactToInterrupt() {
        sarRunner = new Thread() {

            @Override
            public void run() {

                sar.run();

                c1 = 1;
            }
        };

        sarRunner.start();

        while (sarRunner.isAlive()) {
            sarRunner.interrupt();
        }

        assertThat(c1, is(equalTo(1)));
    }

    @Test
    public void testActionRun() {
        sarRunner.start();

        final Action q = new AddOneIntegerAction(1);

        sar.queueAction(q, 1, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                assertThat(action, is(equalTo(q)));
                assertThat(action.getResult().isPresent(), is(true));
                assertThat(action.isConcreteResult(), is(true));
            }
        });

        assertThat(waitForResult(q, 1000), is(true));
    }

    @Test
    public void testFailingOrder() {

        final Action a1 = new AddOneIntegerAction(1);
        final Action a2 = new FailFirstCharAction('c');

        ActionStatusNotifier asn = new ActionStatusNotifier() {

            Action[] expected = {a1, a2};

            int callId = 0;

            @Override
            public void onActionCompleted(Action action) {
                assertThat(action, is(equalTo(expected[callId])));
                callId++;
            }
        };

        sar.queueAction(a1, 1, asn);
        sar.queueAction(a2, 1, asn);

        sarRunner.start();

        assertThat(waitForResult(a1, 1000), is(true));
        assertThat(waitForResult(a2, 1000), is(true));
    }

    @Test
    public void testSurvivesNotificationException() {
        sarRunner.start();

        final Action q = new AddOneIntegerAction(1);

        sar.queueAction(q, 1, new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                throw new RuntimeException("Suspended, but logged");
            }
        });

        assertThat(waitForResult(q, 1000), is(true));
    }

    @Test
    public void testDoubleNotification() {

        final Action q = new AddOneIntegerAction(1);

        ActionStatusNotifier asn = new ActionStatusNotifier() {
            @Override
            public void onActionCompleted(Action action) {
                c1++;
            }
        };

        sar.queueAction(q, 1, asn);
        sar.queueAction(q, 1, asn);

        sarRunner.start();

        assertThat(waitForResult(q, 1000), is(true));
        assertThat(c1, is(equalTo(2)));
    }
}
