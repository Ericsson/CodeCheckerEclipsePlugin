package cc.ecl.job;

import cc.ecl.action.Action;
import cc.ecl.action.SimpleActionRunner;
import cc.ecl.action.SimpleActionRunnerTest;
import cc.ecl.action.mock.AddOneIntegerAction;
import cc.ecl.action.mock.FailFirstCharAction;

import com.google.common.base.Optional;

import org.joda.time.DurationFieldType;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SimpleJobRunnerTest {

    SimpleJobRunner sjr;

    Thread sjrRunner;

    static boolean waitForResult(Job j, int timeout) {
        while (j.getStatus() == Job.Status.PENDING) {
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
    public void setupRunner() {
        SimpleActionRunner<Object> sar = SimpleActionRunnerTest.setupActionRunner();
        sjr = new SimpleJobRunner(sar);
        sjrRunner = new Thread(sjr);
    }

    @After
    public void stopRunner() {
        if (sjrRunner != null && sjrRunner.isAlive() && !sjrRunner.isInterrupted()) {
            sjrRunner.interrupt();
        }
        sjrRunner = null;
        sjr = null;
    }

    @Test
    public void testJobCompletes() {
        sjrRunner.start();

        SimpleTestJob job = new SimpleTestJob();

        sjr.addJob(job);

        assertThat(waitForResult(job, 1000), is(true));

        assertThat(job.startCallCount, is(equalTo(1)));
        assertThat(job.processCallCount, is(equalTo(1)));
        assertThat(job.result, is(equalTo(6)));
    }

    @Test
    public void testChain() {
        sjrRunner.start();

        SlowTestJob job = new SlowTestJob(3);

        sjr.addJob(job);

        assertThat(waitForResult(job, 2000), is(true));

        assertThat(job.startCallCount, is(equalTo(1)));
        assertThat(job.processCallCount, is(equalTo(2)));
        assertThat(job.result, is(equalTo('c')));
    }

    @Test
    public void testTimeout() {
        sjrRunner.start();

        SlowTestJob job = new SlowTestJob(5, 0, Optional.of((new Instant()).toDateTime()
                .withFieldAdded(DurationFieldType.millis(), 1500).toInstant()));

        sjr.addJob(job);

        assertThat(waitForResult(job, 2000), is(true));

        assertThat(job.getStatus(), is(equalTo(Job.Status.TIMEOUT)));
    }

    class SimpleTestJob extends Job {

        Integer result;

        int startCallCount, processCallCount;

        public SimpleTestJob() {
            super(0, Optional.<Instant>absent());
        }

        public SimpleTestJob(int priority, Optional<Instant> deadline) {
            super(priority, deadline);
        }

        @Override
        protected List<ActionRequest> startActions() {
            startCallCount++;

            LinkedList<ActionRequest> ret = new LinkedList<ActionRequest>();

            ret.add(new ActionRequest(new AddOneIntegerAction(5)));
            return ret;
        }

        @Override
        protected List<ActionRequest> processActionResult(Action completedAction) {
            processCallCount++;

            // no error handling!
            result = (Integer) completedAction.getResult().get();

            return new LinkedList<ActionRequest>();
        }

        @Override
        protected List<ActionRequest> preemptiveActions() {
            return Arrays.asList();
        }
    }

    class SlowTestJob extends Job {

        final int callsRequired;
        Character result;
        int startCallCount, processCallCount;

        public SlowTestJob(int callsRequired) {
            super(0, Optional.<Instant>absent());
            this.callsRequired = callsRequired;
        }

        public SlowTestJob(int callsRequired, int priority, Optional<Instant> deadline) {
            super(priority, deadline);
            this.callsRequired = callsRequired;
        }

        @Override
        protected List<ActionRequest> startActions() {
            startCallCount++;

            LinkedList<ActionRequest> ret = new LinkedList<ActionRequest>();

            ret.add(new ActionRequest(new FailFirstCharAction('d')));
            return ret;
        }

        @Override
        protected List<ActionRequest> processActionResult(Action completedAction) {
            processCallCount++;

            // no error handling!
            result = (Character) completedAction.getResult().get();

            if (processCallCount + 1 < callsRequired) {
                LinkedList<ActionRequest> ret = new LinkedList<ActionRequest>();
                ret.add(new ActionRequest(new FailFirstCharAction(Character.forDigit
                        (processCallCount, 16))));
                return ret;
            } else {
                return new LinkedList<ActionRequest>();
            }
        }

        @Override
        protected List<ActionRequest> preemptiveActions() {
            return Arrays.asList();
        }
    }
}
