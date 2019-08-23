package org.codechecker.eclipse.plugin;

import java.nio.file.Path;

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
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * CodeChecker plugin preferences indicator tests.
 */
public class IndicatorTest {
    
    private static final int SHORT_WAIT_TIME = 500; // in milliseconds
    
    private static final String CODECHECKER = "CodeChecker";
    
    private static SWTWorkbenchBot bot;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
     * Test that with unconfigured CodeChecker, a warning message is displayed.
     */
    @Test
    public void testNoCodeCheckerFound() {
        SWTBotCLabel label = null;
        try {
            label = bot.clabel("CodeChecker package directory is invalid");
        } catch (WidgetNotFoundException e) {
            System.out.println(e.getMessage());
        }
        assertThat("There was no invalid CodeChecker message displayed", label, is(IsNull.notNullValue()));
        
        preferencesShell.close();
    }
    
    /**
     * Test that with CodeChecker configured, a confirmation message is displayed.
     */
    @Test
    public void testCodeCheckerFound() {
        Path ccDir = Utils.prepareCodeChecker();
        GuiUtils.setCCBinDir(ccDir, bot);

        SWTBotCLabel label = null;
        try {
            label = bot.clabel("CodeChecker package directory is valid");
        } catch (WidgetNotFoundException e) {
            System.out.println(e.getMessage());
        }
        assertThat("There was no valid CodeChecker message displayed", label, is(IsNull.notNullValue()));
        
        preferencesShell.close();
    }
}
