package cc.codechecker.plugin.runtime;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnvironmentParserTest {

    @Test
    public void testOneLine() {
        EnvironmentParser ep = new EnvironmentParser();
        ImmutableMap<String, String> result = ep.parse("ALMA=korte");
        assertThat(result.containsKey("ALMA"), is(true));
        assertThat(result.get("ALMA"), is(equalTo("korte")));
    }

    @Test
    public void testTwoLine() {
        EnvironmentParser ep = new EnvironmentParser();
        ImmutableMap<String, String> result = ep.parse("ALMA=korte\nBARACK=nemalma");
        assertThat(result.containsKey("ALMA"), is(true));
        assertThat(result.get("ALMA"), is(equalTo("korte")));
        assertThat(result.containsKey("BARACK"), is(true));
        assertThat(result.get("BARACK"), is(equalTo("nemalma")));
    }
}
