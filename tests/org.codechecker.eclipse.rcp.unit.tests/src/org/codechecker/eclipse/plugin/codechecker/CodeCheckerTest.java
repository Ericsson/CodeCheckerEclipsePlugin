package org.codechecker.eclipse.plugin.codechecker;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelper;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelperFactory;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests to test functionality of {@link CodeChecker}.
 */
public class CodeCheckerTest {
    private static final int RUN_COUNT = 123;

    private static final String DUMMY = "/home";
    private static final String ERROR_COULDNT_CREATE_CC = "Couldn't create CodeChecker instance!";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Path codeCheckerPath = Utils.prepareCodeChecker();
    private Path notACodeCheckerPath = Paths.get(DUMMY);
    private ShellExecutorHelper she;

    /**
     * Initialitze a CodeChecker test package and also a dummy path, and a
     * {@link ShellExecutorHelper} with a default system environment.
     */
    @Before
    public void init() {
        codeCheckerPath = Utils.prepareCodeChecker().resolve(Paths.get("bin", "CodeChecker"));
        notACodeCheckerPath = Paths.get(DUMMY);
        she = new ShellExecutorHelperFactory().createShellExecutorHelper(System.getenv());
    }

    /**
     * Test that an exception is thrown when a CodeChecker is tred to be constructed
     * with an invalid(no codechecker at that location).
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testInvalidPath() throws InvalidCodeCheckerException {
        thrown.expect(InvalidCodeCheckerException.class);
        new CodeChecker(notACodeCheckerPath, she);
    }

    /**
     * Test that the version string is returned.
     */
    @Test
    public void testVersionReturned() {
        ICodeChecker codeChecker = null;
        try {
            codeChecker = new CodeChecker(codeCheckerPath, she);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }

        try {
            String version = codeChecker.getVersion();
            assertThat("Missing Version String", version.startsWith("CodeChecker analyzer version:"));
        } catch (InvalidCodeCheckerException e) {
            fail("An exception was thrown after a successful initialization!");
        }
    }

    /**
     * Test that the correct path is returned.
     */
    @Test
    public void testPathIsCorrect() {
        ICodeChecker codeChecker = null;
        try {
            codeChecker = new CodeChecker(codeCheckerPath, she);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }
        assertThat("Path to CodeChecker wasn't the same", codeCheckerPath.compareTo(codeChecker.getLocation()) == 0);
    }

    /**
     * Simple test for running an analysis.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void analyzeTest() {
        ICodeChecker codeChecker = null;
        ShellExecutorHelper mockShe = Mockito.spy(new ShellExecutorHelper(System.getenv()));
        try {
            codeChecker = new CodeChecker(codeCheckerPath, mockShe);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }
        CcConfigurationBase configMock = mock(CcConfigurationBase.class);
        final String cores = "3 ";
        final String extra = "-e unix";
        when(configMock.get(ConfigTypes.ANAL_THREADS)).thenReturn(cores);
        when(configMock.get(ConfigTypes.ANAL_OPTIONS)).thenReturn(extra);
        NullProgressMonitor mon = new NullProgressMonitor();
        when(mockShe.progressableWaitReturnOutput(anyString(), Mockito.anyMap(), Mockito.anyBoolean(), Mockito.eq(mon),
                Mockito.eq(RUN_COUNT))).then(new Answer<Optional<String>>() {

                    @Override
                    public Optional<String> answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return Optional.of((String) args[0]);
                    }
                });
        String analyzeResult = codeChecker.analyze(Paths.get(DUMMY), true, mon, RUN_COUNT, configMock);
        assertThat("Analyze result isn't the same as specified", analyzeResult.contains("-j " + cores));
        assertThat("Analyze result isn't contains the extra parameters", analyzeResult.contains(extra));
    }
}
