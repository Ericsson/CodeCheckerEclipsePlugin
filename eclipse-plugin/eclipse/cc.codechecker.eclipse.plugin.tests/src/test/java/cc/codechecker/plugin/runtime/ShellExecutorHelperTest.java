package cc.codechecker.plugin.runtime;

import org.apache.commons.exec.environment.EnvironmentUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ShellExecutorHelperTest {

    private ShellExecutorHelper emptyEnvExecutor;
    private ShellExecutorHelper defaultEnvExecutor;

    @Before
    public void setupExecutors() throws IOException {
        emptyEnvExecutor = new ShellExecutorHelper(new HashMap<String, String>());
        defaultEnvExecutor = new ShellExecutorHelper(EnvironmentUtils.getProcEnvironment());
    }

    @Test
    public void emptyEnvironmentEchoes() {
        assertThat(emptyEnvExecutor.quickReturnFirstLine("/bin/echo 5").or(""), is(equalTo("5")));
    }

    @Test
    public void emptyEnvironmentSequence() {
        assertThat(emptyEnvExecutor.quickReturnFirstLine("/bin/echo 4; /bin/echo 10").or(""), is
                (equalTo("4")));
    }

    @Test
    public void defaultEnvironmentEcho() { /* Should work on any sane POSIX system */
        assertThat(defaultEnvExecutor.quickReturnFirstLine("echo 5").or(""), is(equalTo("5")));
    }

    @Test
    public void echoSucceeds() {
        assertThat(emptyEnvExecutor.quickAndSuccessfull("/bin/echo 4"), is(true));
    }

    @Test
    public void echoooFails() {
        assertThat(emptyEnvExecutor.quickAndSuccessfull("echooo 4;"), is(false));
    }
}
