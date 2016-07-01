package cc.ecl.action;

import cc.ecl.action.mock.AddOneIntegerAction;
import cc.ecl.action.mock.AddOneIntegerActionImpl;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ActionCacheTest {

    ActionCache cache;

    AddOneIntegerAction sampleAction;

    @Before
    public void setupCache() {

        this.cache = new SimpleActionCache();

        // only concrete result can be added to the cache
        sampleAction = new AddOneIntegerAction(1);
        AddOneIntegerActionImpl impl = new AddOneIntegerActionImpl();
        impl.setCommunicationInterface(1);
        sampleAction.runWithImpl(impl, null);

        cache.add(sampleAction);
    }

    @Test
    public void testCacheResult() {
        Optional<Action> oth = cache.get(new AddOneIntegerAction(1));

        assertThat(oth.isPresent(), is(true));
        assertThat((AddOneIntegerAction) oth.get(), is(sampleAction));
    }

    @Test
    public void testCacheMiss() {
        Optional<Action> oth = cache.get(new AddOneIntegerAction(2));

        assertThat(oth.isPresent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotConcreteItem() {
        cache.add(new AddOneIntegerAction(1));
    }

}
