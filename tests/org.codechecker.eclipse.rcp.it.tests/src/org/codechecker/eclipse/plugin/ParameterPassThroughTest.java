package org.codechecker.eclipse.plugin;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.config.CommonGui;
import org.codechecker.eclipse.plugin.utils.GuiUtils;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the parameter pass through graphical user interface functionalities.
 */
public class ParameterPassThroughTest {

    private static final String CR_PROJ = "parameterProject";
    private static final String CODECHECKER = "CodeChecker";

    private static final String EXTRA = "-e unix";

    private static SWTBotTreeItem project;
    private static SWTWorkbenchBot bot;
    private static Path ccDir = Utils.prepareCodeChecker();

    /**
     * Setup the bot.
     */
    @BeforeClass
    public static void setUp() {
        bot = new SWTWorkbenchBot();
    }

    /**
     * Test the global preferences.
     */
    @Test
    public void testGlobal() {
        SWTBotShell preferencesShell = GuiUtils.getPreferencesTab(CODECHECKER, bot);

        GuiUtils.setCCBinDir(ccDir, bot);

        SWTBotText display = bot.textWithLabel(CommonGui.CC_FINAL_DISP_LABEL);
        assertThat("Preferences page Display is incorrect!", display.getText().startsWith(ccDir.toString()));

        GuiUtils.applyClosePreferences(preferencesShell, bot);
    }

    /**
     * Test in project properties.
     */
    @Test
    public void testProject() {
        bot.menu("File").menu("New").menu(GuiUtils.CPP_PROJECT).click();

        SWTBotShell shell = bot.shell("C++ Project");
        shell.activate();
        
        bot.textWithLabel("&Project name:").setText(CR_PROJ);
        bot.tree().getTreeItem("Executable").getNode("Hello World C++ Project").select();
        bot.button(GuiUtils.FINISH).click();

        project = bot.tree().getTreeItem(CR_PROJ);
        project.contextMenu(GuiUtils.ADD_NATURE_MENU).click();
        project.contextMenu(GuiUtils.PROPERTIES).click();

        SWTBotShell propertiesShell = bot.shell(GuiUtils.PROPERTIES_FOR + CR_PROJ);
        propertiesShell.activate();
        bot.tree().getTreeItem(GuiUtils.CODECHECKER).select();
        
        bot.radio(GuiUtils.PROJECT_RADIO).click();

        GuiUtils.setCCBinDir(ccDir, bot);

        SWTBotText display = bot.textWithLabel(CommonGui.CC_FINAL_DISP_LABEL);
        assertThat("Properties page Display is incorrect!", display.getText().startsWith(ccDir.toString()));

        SWTBotText extra = bot.textWithLabel(CommonGui.CC_EXTRA_CMD_LABEL);
        extra.setText(EXTRA);

        assertThat("Display not contains extra parameters", extra.getText().contains(EXTRA));

        GuiUtils.applyCloseProperties(propertiesShell, bot);

        GuiUtils.deleteProject(CR_PROJ, true, bot);
    }

}
