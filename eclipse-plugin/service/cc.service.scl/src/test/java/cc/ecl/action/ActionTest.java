package cc.ecl.action;


import cc.ecl.action.mock.AddOneIntegerAction;
import cc.ecl.action.mock.AddOneIntegerActionImpl;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;

public class ActionTest {

    @Test
    public void testEquals() {
        AddOneIntegerAction ai1 = new AddOneIntegerAction(1);
        AddOneIntegerAction ai2 = new AddOneIntegerAction(1);

        assertThat(ai1, is(equalTo(ai2)));
    }

    @Test
    public void testNotEquals() {
        AddOneIntegerAction ai1 = new AddOneIntegerAction(1);
        AddOneIntegerAction ai2 = new AddOneIntegerAction(2);

        assertThat(ai1, not(equalTo(ai2)));
    }

    @Test()
    public void testRunSurvivesExceptions() {
        AddOneIntegerAction ai1 = new AddOneIntegerAction(1);
        AddOneIntegerActionImpl impl = new AddOneIntegerActionImpl();

        // setCommuncationObject not called => throws exception
        ai1.runWithImpl(impl, null);

        assertThat(ai1.getStatus(), is(ActionStatus.COMMUNICATION_ERROR));
    }
}
