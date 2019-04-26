package org.codechecker.eclipse.plugin.runtime;

import com.google.common.base.Optional;

import org.codechecker.eclipse.plugin.runtime.CodeCheckerLocator;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelper;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CodeCheckerLocatorTest {

    @Test
    public void noSystemLocatorWithoutEnv() throws IOException {
        CodeCheckerLocator onlySystemLocatorWithoutEnv = new CodeCheckerLocator(new
                ShellExecutorHelper(new HashMap<String, String>()), Optional.<String>absent());
        assertThat(onlySystemLocatorWithoutEnv.foundCcExecutable(), is(false));
        assertThat(onlySystemLocatorWithoutEnv.getRunnerCommand(), is(equalTo(Optional
                .<String>absent())));
    }

    /*@Test
    public void findsInPath() throws IOException {
        CodeCheckerLocator onlySystemLocatorWithEnv = new CodeCheckerLocator(new
                ShellExecutorHelper(envWithPath(getExecutorDirectoryLocation("usage"))), Optional
                .<String>absent());
        assertThat(onlySystemLocatorWithEnv.foundCcExecutable(), is(true));
        assertThat(onlySystemLocatorWithEnv.getRunnerCommand(), is(equalTo(Optional.of
                (getExecutorLocation("usage")))));
    }*/

   /* @Test
    public void findsSpecificLocatorWithSh() throws IOException {
        CodeCheckerLocator specificLocator = new CodeCheckerLocator(new ShellExecutorHelper(new
                HashMap<String, String>()), Optional.of(getExecutorInitScriptLocation("usage")));
        assertThat(specificLocator.foundCcExecutable(), is(true));
        assertThat(specificLocator.getRunnerCommand(), is(equalTo(Optional.of("source " +
                getExecutorInitScriptLocation("usage") + "; CodeChecker"))));
    }*.

    /*@Test
    public void findsSpecificLocatorWithDirectory() throws IOException {
        CodeCheckerLocator specificLocator = new CodeCheckerLocator(new ShellExecutorHelper(new
                HashMap<String, String>()), Optional.of(getExecutorDirectoryLocation("usage")));
        assertThat(specificLocator.foundCcExecutable(), is(true));
        assertThat(specificLocator.getRunnerCommand(), is(equalTo(Optional.of("source " +
                getExecutorInitScriptLocation("usage") + "; CodeChecker"))));
    }*/

   /* @Test
    public void preferSpecificLocator() throws IOException {
        CodeCheckerLocator specificLocator = new CodeCheckerLocator(new ShellExecutorHelper
                (envWithPath(getExecutorDirectoryLocation("usage"))), Optional.of
                (getExecutorDirectoryLocation("usage")));
        assertThat(specificLocator.foundCcExecutable(), is(true));
        assertThat(specificLocator.getRunnerCommand(), is(equalTo(Optional.of("source " +
                getExecutorInitScriptLocation("usage") + "; CodeChecker"))));
    }*/

   /* @Test
    public void handlesBadSpecificLocator() throws IOException {
        CodeCheckerLocator specificLocator = new CodeCheckerLocator(new ShellExecutorHelper(new
                HashMap<String, String>()), Optional.of(getExecutorInitScriptLocation
                ("bad-usage")));
        assertThat(specificLocator.foundCcExecutable(), is(false));
        assertThat(specificLocator.getRunnerCommand(), is(equalTo(Optional.<String>absent())));
    }*/

    /*@Test
    public void findsSystemWithBadSpecific() throws IOException {
        CodeCheckerLocator specificLocator = new CodeCheckerLocator(new ShellExecutorHelper
                (envWithPath(getExecutorDirectoryLocation("usage"))), Optional.of
                (getExecutorDirectoryLocation("bad-usage")));
        assertThat(specificLocator.foundCcExecutable(), is(true));
        assertThat(specificLocator.getRunnerCommand(), is(equalTo(Optional.of(getExecutorLocation
                ("usage")))));
    }*/

    private String getExecutorDirectoryLocation(String name) {
        return getClass().getResource("/mock-codechecker/").getPath() + name;
    }

    private String getExecutorInitScriptLocation(String name) {
        return getExecutorDirectoryLocation(name) + "/init/init.sh";
    }

    private String getExecutorLocation(String name) {
        return getExecutorDirectoryLocation(name) + "/CodeChecker";
    }

    private Map<String, String> envWithPath(String path) {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("PATH", path);

        return hm;
    }

}
