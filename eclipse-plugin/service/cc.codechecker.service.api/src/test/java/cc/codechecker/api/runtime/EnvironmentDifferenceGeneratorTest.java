package cc.codechecker.api.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EnvironmentDifferenceGeneratorTest {

    @Test
    public void testReplace() {
        EnvironmentParser ep = new EnvironmentParser();
        EnvironmentDifferenceGenerator gen = new EnvironmentDifferenceGenerator();

        ImmutableMap<String, String> srcEnv = ep.parse("ALMA=korte");
        ImmutableMap<String, String> dstEnv = ep.parse("ALMA=barack");

        ImmutableList<EnvironmentDifference> result = gen.difference(srcEnv, dstEnv);

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result.get(0).action, is(equalTo(EnvironmentDifference.ModificationAction
                .REPLACE)));
    }

    @Test
    public void testAddRemove() {
        EnvironmentParser ep = new EnvironmentParser();
        EnvironmentDifferenceGenerator gen = new EnvironmentDifferenceGenerator();

        ImmutableMap<String, String> srcEnv = ep.parse("ALMA1=korte");
        ImmutableMap<String, String> dstEnv = ep.parse("ALMA2=barack");

        ImmutableList<EnvironmentDifference> result = gen.difference(srcEnv, dstEnv);

        assertThat(result.size(), is(equalTo(2)));
        assertThat(result.get(0).action, is(equalTo(EnvironmentDifference.ModificationAction
                .REMOVE)));
        assertThat(result.get(1).action, is(equalTo(EnvironmentDifference.ModificationAction.ADD)));
    }

    @Test
    public void testAutoPrepend() {
        EnvironmentParser ep = new EnvironmentParser();
        EnvironmentDifferenceGenerator gen = new EnvironmentDifferenceGenerator();

        ImmutableMap<String, String> srcEnv = ep.parse("PATH=korte");
        ImmutableMap<String, String> dstEnv = ep.parse("PATH=alma:korte");

        ImmutableList<EnvironmentDifference> result = gen.difference(srcEnv, dstEnv);

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result.get(0).action, is(equalTo(EnvironmentDifference.ModificationAction
                .PREPEND)));
        assertThat(result.get(0).parameter, is(equalTo("alma:")));
    }

    @Test
    public void testAutoAppend() {
        EnvironmentParser ep = new EnvironmentParser();
        EnvironmentDifferenceGenerator gen = new EnvironmentDifferenceGenerator();

        ImmutableMap<String, String> srcEnv = ep.parse("PATH=korte");
        ImmutableMap<String, String> dstEnv = ep.parse("PATH=korte:alma");

        ImmutableList<EnvironmentDifference> result = gen.difference(srcEnv, dstEnv);

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result.get(0).action, is(equalTo(EnvironmentDifference.ModificationAction
                .APPEND)));
        assertThat(result.get(0).parameter, is(equalTo(":alma")));
    }
}
