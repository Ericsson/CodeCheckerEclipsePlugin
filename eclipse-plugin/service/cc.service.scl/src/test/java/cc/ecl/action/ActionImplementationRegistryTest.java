package cc.ecl.action;

import cc.ecl.action.mock.AddOneIntegerAction;
import cc.ecl.action.mock.AddOneIntegerActionImpl;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ActionImplementationRegistryTest {

    ActionImplementationRegistry<Object> simpleRegistry;

    ActionParameterInfo addOneParameters;

    @Before
    public void setupRegistry() {

        simpleRegistry = new ActionImplementationRegistry<Object>();
        AddOneIntegerActionImpl impl = new AddOneIntegerActionImpl();
        simpleRegistry.addImplementation(impl);
        addOneParameters = impl.getParameterInfo();
    }

    @Test
    public void testFind() {
        Optional<ActionImpl> impl = simpleRegistry.getImplementationFor(addOneParameters);

        assertThat(impl.isPresent(), is(true));
        assertThat(impl.get().getParameterInfo(), is(equalTo(addOneParameters)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleAddError() {
        simpleRegistry.addImplementation(new AddOneIntegerActionImpl());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRegistration() {
        ActionRunner<Object> simpleRunner = new SimpleActionRunner<Object>(1, simpleRegistry);

        // we can run actions after registration
        AddOneIntegerAction act = new AddOneIntegerAction(1);

        // suppressing this warning
        act.runWithImpl(simpleRegistry.getImplementationFor(act.getParameterInfo()).get(), null);

        assertThat(act.getStatus(), is(ActionStatus.SUCCEEDED));
    }

    @Test(expected = RuntimeException.class)
    public void testDoubleRegistration() {
        new SimpleActionRunner<Object>(1, simpleRegistry);
        new SimpleActionRunner<Object>(1, simpleRegistry);
    }
}
