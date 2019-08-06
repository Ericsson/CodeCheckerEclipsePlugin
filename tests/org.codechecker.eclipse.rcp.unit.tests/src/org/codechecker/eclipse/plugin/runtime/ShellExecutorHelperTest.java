package org.codechecker.eclipse.plugin.runtime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.environment.EnvironmentUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ShellExecutorHelperTest {

    private ShellExecutorHelper emptyEnvExecutor;
    private ShellExecutorHelper defaultEnvExecutor;

    @Before
    public void setupExecutors() throws IOException {
        emptyEnvExecutor = new ShellExecutorHelper(new HashMap<String, String>());
        defaultEnvExecutor = new ShellExecutorHelper(EnvironmentUtils.getProcEnvironment());
    }

    /**
     * Test that Change Directory command succeds on a directory with spaces. 
     */
    @Test
    public void cdSucceeds() {
        File file = null;
        try {
            file = Files.createTempDirectory("dir with spaces").toFile();
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThat("File not exists", file.exists(), is(true));
        Map<String, File> map = new HashMap<String, File>();
        map.put("FILE", file);

        assertThat(defaultEnvExecutor.quickAndSuccessfull("cd '${FILE}'", map), is(true));
    }

    @Test
    public void emptyEnvironmentEchoes() {
        assertThat(emptyEnvExecutor.quickReturnFirstLine("/bin/echo 6", null).or(""), is(equalTo("6")));
    }

    @Test
    public void emptyEnvironmentSequence() {
        assertThat(emptyEnvExecutor.quickReturnFirstLine("/bin/echo 4 ; /bin/echo 10", null).or(""), is
                (equalTo("4")));
    }

    @Test
    public void defaultEnvironmentEcho() { /* Should work on any sane POSIX system */
        assertThat(defaultEnvExecutor.quickReturnFirstLine("echo 5", null).or(""), is(equalTo("5")));
    }

    @Test
    public void echoSucceeds() {
        assertThat(emptyEnvExecutor.quickAndSuccessfull("/bin/echo 3", null), is(true));
    }

    @Test
    public void echoooFails() {
        assertThat(emptyEnvExecutor.quickAndSuccessfull("echooo 4;", null), is(false));
    }
}
