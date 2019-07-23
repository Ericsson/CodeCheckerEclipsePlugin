package org.codechecker.eclipse.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.codechecker.locator.EnvCodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.PreBuiltCodeCheckerLocatorService;
import org.codechecker.eclipse.plugin.codechecker.locator.ResolutionMethodTypes;
import org.codechecker.eclipse.plugin.utils.GuiUtils;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * CodeChecker plugin preferences indicator tests.
 */
public class IndicatorTest {
    
    private static final int SHORT_WAIT_TIME = 500; // in milliseconds
    
    private static final String CODECHECKER = "CodeChecker";
    private static final String FORM_MESSAGE_CC_FOUND = "CodeChecker being used:";
    private static final String ENV_PATH = "PATH";
    private static final Path DUMMY = Paths.get("/home");

    private static final String ERROR_NO_VALID_CC = "There was no valid CodeChecker message displayed";
    
    private static SWTWorkbenchBot bot;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private SWTBotShell preferencesShell;

    /**
     * Import cpp project into workspace, and setup SWTBot.
     *
     */
    @BeforeClass
    public static void setup() {
        bot = new SWTWorkbenchBot();
    }
    
    /**
     * Open preferences, CodeChecker page before every test.
     */
    @Before
    public void openPreferences() {
        preferencesShell = GuiUtils.getPreferencesTab(CODECHECKER, bot);
    }

    /**
     * Test that with path option selected, the proper warning message is displayed.
     */
    @Test
    public void testNoCodeCheckerFoundInPath() {
        SWTBotCLabel label = null;
        try {
            label = bot.clabel(EnvCodeCheckerLocatorService.ERROR);
        } catch (WidgetNotFoundException e) {
            System.out.println(e.getMessage());
        }
        assertThat(ERROR_NO_VALID_CC, label, is(IsNull.notNullValue()));
        
        preferencesShell.close();
    }
    
    /**
     * Test that with prebuilt option selected, the proper warning message is
     * displayed.
     */
    @Test
    public void testNoCodeCheckerFoundPre() {
        GuiUtils.setCCBinDir(ResolutionMethodTypes.PRE, DUMMY, bot, false);

        SWTBotCLabel label = null;
        try {
            label = bot.clabel(PreBuiltCodeCheckerLocatorService.ERROR);
        } catch (WidgetNotFoundException e) {
            System.out.println(e.getMessage());
        }
        assertThat(ERROR_NO_VALID_CC, label, is(IsNull.notNullValue()));

        preferencesShell.close();
    }

    /**
     * Test that with CodeChecker configured, a confirmation message is displayed.
     */
    @Test
    public void testCodeCheckerFound() {
        Path ccDir = Utils.prepareCodeChecker();
        GuiUtils.setCCBinDir(ccDir, bot);
        
        bot.sleep(SHORT_WAIT_TIME);

        SWTBotCLabel label = null;
        try {
            label = new SWTBotCLabel(GuiUtils.findCLabel(FORM_MESSAGE_CC_FOUND, bot));
        } catch (WidgetNotFoundException e) {
            System.out.println(e.getMessage());
        }
        assertThat(ERROR_NO_VALID_CC, label, is(IsNull.notNullValue()));
        
        preferencesShell.close();
    }

    /**
     * Test that with CodeChecker added to PATH environment variable, the Plugin
     * picks it up as a valid package and a confirmation message is displayed.
     */
    @Test
    public void testCodeCheckerFoundInPath() {
        GuiUtils.applyClosePreferences(preferencesShell, bot);
        Path ccDir = Utils.prepareCodeChecker();

        // prepare PATH envval
        String origPath = System.getenv(ENV_PATH);
        String newPath = origPath.concat(":" + ccDir.toAbsolutePath().toString() + "/bin/");

        // Set the prepared PATH
        environmentVariables.set(ENV_PATH, newPath);

        preferencesShell = GuiUtils.getPreferencesTab(CODECHECKER, bot);
        GuiUtils.setCCBinDir(ResolutionMethodTypes.PATH, null, bot, false);
        SWTBotCLabel label = null;
        try {
            label = new SWTBotCLabel(GuiUtils.findCLabel(FORM_MESSAGE_CC_FOUND, bot));
        } catch (WidgetNotFoundException e) {
            System.out.println(e.getMessage());
        }
        assertThat(ERROR_NO_VALID_CC, label, is(IsNull.notNullValue()));

        preferencesShell.close();

        // reset env
        environmentVariables.set(ENV_PATH, origPath);
    }
}
