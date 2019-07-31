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
    private static final String GARBAGE = "garbage";
    private static final String ERROR_COULDNT_CREATE_CC = "Couldn't create CodeChecker instance!";
    private static final Path CC_PATH = Utils.prepareCodeChecker();
    private static final Path PATH_CC_PATH = Utils.prepareCodeChecker();
    private static final Path CUSTOM_CC_PATH = Utils.prepareCodeChecker(true).resolve(Paths.get("bin", "CodeChecker"));
    private static final Path VENV_PATH = CUSTOM_CC_PATH.getParent().getParent().toAbsolutePath()
            .resolve(Paths.get("venv"));
    private static final Path NOT_CC_PATH = Paths.get(DUMMY);
    private static final Path NOT_VENV_PATH = Paths.get(DUMMY);
    // TODO implement virtual environment emulation
    // private static final Path venvPath = Paths.get(DUMMY);
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
        thrown.expectMessage(EnvCodeCheckerLocatorService.ERROR);

        serv.findCodeChecker(null, null, CC_FACTORY, SHEF);
        // prepare PATH envval
        String origPath = System.getenv(ENV_PATH);
        String newPath = origPath.concat(":" + PATH_CC_PATH.toAbsolutePath().toString() + "/bin/");

        // Set the prepared PATH
        environmentVariables.set(ENV_PATH, newPath);

        serv = cclf.create(ResolutionMethodTypes.PATH);
        try {
            serv.findCodeChecker(null, null, CC_FACTORY, SHEF);
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
        thrown.expectMessage(PreBuiltCodeCheckerLocatorService.INVALID);
        // Test null
        serv.findCodeChecker(null, null, CC_FACTORY, SHEF);

        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(PreBuiltCodeCheckerLocatorService.ERROR);
        // Test garbage
        serv.findCodeChecker(Paths.get(GARBAGE), null, CC_FACTORY, SHEF);

        // Test not valid
        serv.findCodeChecker(NOT_CC_PATH, null, CC_FACTORY, SHEF);

        // Test valid
        serv = cclf.create(ResolutionMethodTypes.PATH);
        try {
            serv.findCodeChecker(CC_PATH, null, CC_FACTORY, SHEF);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }
    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with null - null
     * Path paramaters as input throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyNullNull() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        thrown.expect(NullPointerException.class);
        // Test null
        serv.findCodeChecker(null, null, CC_FACTORY, SHEF);
    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with null venv Path
     * paramaters as input throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyPreCCNull() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        thrown.expect(NullPointerException.class);
        // Test null
        serv.findCodeChecker(CC_PATH, null, CC_FACTORY, SHEF);
    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with null venv Path
     * paramaters as input throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyCustCCNull() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        thrown.expect(NullPointerException.class);
        // Test valid Custom - null
        serv.findCodeChecker(CUSTOM_CC_PATH, null, CC_FACTORY, SHEF);

    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with Paths pointing
     * to some random location throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyRandRand() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);

        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(CustomBuiltCodeCheckerLocatorService.ERROR);

        // Test not valid
        serv.findCodeChecker(NOT_CC_PATH, NOT_VENV_PATH, CC_FACTORY, SHEF);
    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with Paths pointing
     * to some random location throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyPreCCRand() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(CustomBuiltCodeCheckerLocatorService.ERROR);
        // Test valid Pre - not Venv
        serv.findCodeChecker(CC_PATH, NOT_VENV_PATH, CC_FACTORY, SHEF);
    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with Paths pointing
     * to some random location throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyCustCCGarb() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(CustomBuiltCodeCheckerLocatorService.ERROR);
        // Test valid Custom - garbage
        serv.findCodeChecker(CUSTOM_CC_PATH, Paths.get(GARBAGE), CC_FACTORY, SHEF);
    }

    /**
     * Test {@link CustomBuiltCodeCheckerLocatorService} called with Paths pointing
     * to some random location throws nullpointer exception.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPyCustCCRand() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        thrown.expect(InvalidCodeCheckerException.class);
        thrown.expectMessage(CustomBuiltCodeCheckerLocatorService.ERROR);
        // Test valid Custom - not Venv
        serv.findCodeChecker(CUSTOM_CC_PATH, NOT_VENV_PATH, CC_FACTORY, SHEF);
    }

    /**
     * Test {@link PreBuiltCodeCheckerLocatorService}.
     * 
     * @throws InvalidCodeCheckerException
     *             For testing purposes.
     */
    @Test
    public void testPy() throws InvalidCodeCheckerException {
        CodeCheckerLocatorService serv = cclf.create(ResolutionMethodTypes.PY);
        // Test valid Custom - Venv
        try {
            serv.findCodeChecker(CUSTOM_CC_PATH, VENV_PATH, CC_FACTORY, SHEF);
        } catch (InvalidCodeCheckerException e) {
            fail(ERROR_COULDNT_CREATE_CC);
        }
    }
}
