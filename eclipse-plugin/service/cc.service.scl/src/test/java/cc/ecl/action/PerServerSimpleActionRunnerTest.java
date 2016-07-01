package cc.ecl.action;

import cc.ecl.action.mock.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class PerServerSimpleActionRunnerTest {

    PerServerSimpleActionRunner<Object> sar;

    Thread sarRunner;

    int c1, c2, c3;

    public static PerServerSimpleActionRunner<Object> setupActionRunner() {
        ActionImplementationRegistry<Object> simpleRegistry = new
                ActionImplementationRegistry<Object>();
        ServerAddOneActionImpl impl1 = new ServerAddOneActionImpl();
        simpleRegistry.addImplementation(impl1);

        return new PerServerSimpleActionRunner<Object>(1, simpleRegistry);
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

        final Action q = new ServerAddOneAction(new ServerIntegerRequest("localhost", 5));

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
}
