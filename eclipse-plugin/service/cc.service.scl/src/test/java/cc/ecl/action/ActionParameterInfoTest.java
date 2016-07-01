package cc.ecl.action;

import com.google.common.reflect.TypeToken;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ActionParameterInfoTest {

    private TypeToken<Integer> integerType = TypeToken.of(Integer.class);
    private TypeToken<Long> longType = TypeToken.of(Long.class);

    @Test
    public void testEquals() {
        ActionParameterInfo api1 = new ActionParameterInfo(integerType, longType);
        ActionParameterInfo api2 = new ActionParameterInfo(integerType, longType);

        assertThat(api1, is(equalTo(api2)));
    }

    @Test
    public void testNotEquals() {
        ActionParameterInfo api1 = new ActionParameterInfo(integerType, longType);
        ActionParameterInfo api2 = new ActionParameterInfo(longType, longType);

        assertThat(api1, not(equalTo(api2)));
    }


    /**
     * Bad example to show this won't work with generic classes (unless they are anonymous classes)
     */
    @Test
    public void testCantTestGenerics() {
        List<Integer> i = new LinkedList<Integer>();
        List<Long> l = new LinkedList<Long>();
        ActionParameterInfo api1 = new ActionParameterInfo(TypeToken.of(i.getClass()), TypeToken
                .of(i.getClass()));
        ActionParameterInfo api2 = new ActionParameterInfo(TypeToken.of(l.getClass()), TypeToken
                .of(l.getClass()));

        assertThat(api1, is(equalTo(api2)));
    }

}

