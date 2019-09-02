package org.codechecker.eclipse.plugin.codechecker.locator;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.codechecker.CodeCheckerFactory;
import org.codechecker.eclipse.plugin.codechecker.ICodeCheckerFactory;
import org.codechecker.eclipse.plugin.runtime.IShellExecutorHelperFactory;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelperFactory;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.fail;

/**
 * {@link CodeCheckerLocatorService} tests.
 */
public class CodeCheckerLocatorTest {

    private static final String ENV_PATH = "PATH";
    private static final String DUMMY = "/home";
    private static final String ERROR_COULDNT_CREATE_CC = "Couldn't create CodeChecker instance!";
    private static final Path CC_PATH = Utils.prepareCodeChecker();
    private static final Path PATH_CC_PATH = Utils.prepareCodeChecker();
    private static final Path NOT_CC_PATH = Paths.get(DUMMY);
    private static final ICodeCheckerFactory CC_FACTORY = new CodeCheckerFactory();
    private static final IShellExecutorHelperFactory SHEF = new ShellExecutorHelperFactory();

    private static CodeCheckerLocatorFactory cclf;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    /**
     * This factory can be used for the entire test.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        cclf = new CodeCheckerLocatorFactory();
    }

    /**
     * Test {@link EnvCodeCheckerLocatorService}.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPath() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PATH);
        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(EnvCodeCheckerLocatorService.CC_NOT_FOUND);

        serv.findCodeChecker(null, CC_FACTORY, SHEF);
        // prepare PATH envval
        String origPath = System.getenv(ENV_PATH);
        String newPath = origPath.concat(":" + PATH_CC_PATH.toAbsolutePath().toString() + "/bin/");

        // Set the prepared PATH
        environmentVariables.set(ENV_PATH, newPath);

        serv = cclf.create(ResolutionMethodTypes.PATH);
        try {
            serv.findCodeChecker(null, CC_FACTORY, SHEF);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }
        // reset env
        environmentVariables.set(ENV_PATH, origPath);
    }

    /**
     * Test {@link PreBuiltCodeCheckerLocatorService}.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPre() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PRE);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage(PreBuiltCodeCheckerLocatorService.CC_INVALID);
        // Test null
        serv.findCodeChecker(null, CC_FACTORY, SHEF);

        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(PreBuiltCodeCheckerLocatorService.CC_NOT_FOUND);
        // Test garbage
        serv.findCodeChecker(Paths.get("gdfsg"), CC_FACTORY, SHEF);

        // Test not valid
        serv.findCodeChecker(NOT_CC_PATH, CC_FACTORY, SHEF);

        // Test valid
        serv = cclf.create(ResolutionMethodTypes.PATH);
        try {
            serv.findCodeChecker(CC_PATH, CC_FACTORY, SHEF);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }
    }
}
