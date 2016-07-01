package cc.ecl.action;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ActionResultTest {

    @Test
    public void testDefaultConstructor() {
        ActionResult<Object> ar = new ActionResult<Object>();
        assertThat(ar.getStatus(), is(equalTo(ActionStatus.PENDING)));
    }

    @Test
    public void testStatusConstructor() {
        ActionResult<Integer> ar = new ActionResult<Integer>(ActionStatus.COMMUNICATION_ERROR);
        assertThat(ar.getResult().isPresent(), is(false));
        assertThat(ar.getStatus(), is(equalTo(ActionStatus.COMMUNICATION_ERROR)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStatusConstructorException() {
        new ActionResult<Object>(ActionStatus.SUCCEEDED);
    }

    @Test
    public void testResultConstructor() {
        Integer i = 1;
        ActionResult<Integer> ar = new ActionResult<Integer>(i);
        assertThat(ar.getStatus(), is(equalTo(ActionStatus.SUCCEEDED)));
        assertThat(ar.getResult().get(), is(i));
    }

    @Test
    public void testBetterThan() {
        ActionResult<Integer> pending = new ActionResult<Integer>();
        ActionResult<Integer> commErr = new ActionResult<Integer>(ActionStatus.COMMUNICATION_ERROR);
        ActionResult<Integer> logiErr = new ActionResult<Integer>(ActionStatus.LOGIC_ERROR);
        ActionResult<Integer> success = new ActionResult<Integer>(1);

        assertThat(pending.betterThan(pending), is(true));
        assertThat(pending.betterThan(commErr), is(false));
        assertThat(pending.betterThan(logiErr), is(false));
        assertThat(pending.betterThan(success), is(false));

        assertThat(commErr.betterThan(pending), is(true));
        assertThat(commErr.betterThan(commErr), is(true));
        assertThat(commErr.betterThan(logiErr), is(false));
        assertThat(commErr.betterThan(success), is(false));

        assertThat(logiErr.betterThan(pending), is(true));
        assertThat(logiErr.betterThan(commErr), is(true));
        assertThat(logiErr.betterThan(logiErr), is(false));
        assertThat(logiErr.betterThan(success), is(false));

        assertThat(success.betterThan(pending), is(true));
        assertThat(success.betterThan(commErr), is(true));
        assertThat(success.betterThan(logiErr), is(false));
        assertThat(success.betterThan(success), is(false));
    }
}
