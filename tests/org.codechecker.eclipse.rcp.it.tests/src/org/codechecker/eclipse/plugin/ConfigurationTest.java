package org.codechecker.eclipse.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.utils.GuiUtils;
import org.codechecker.eclipse.plugin.utils.ProjectImporter;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for testing the plugin configuration changes.
 *
 */
public class ConfigurationTest {
    //private static final int LONGWAIT = 1000000;

    private static final String CODECHECKER = "CodeChecker";
    private static final String CPP_PROJ = "cppTest";
    private static final String CR_PROJ = "createdProject";
    private static final String ERROR_GLOBAL = "Global configuration wasn't selected";
    private static final String ERROR_ENVIR_LOGGER_BIN = "Codechecker Path wasnt correct in CC_LOGGER_BIN";
    private static Path ccDir = Utils.prepareCodeChecker();
    private static Path ccModifDir = Utils.prepareCodeChecker("CodeChecker_Alternative");
    private static SWTBotTreeItem project;


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

        // Import project
        Path file = null;
        try {
            file = Utils.loadFileFromBundle("org.codechecker.eclipse.rcp.it.tests", Utils.RES + CPP_PROJ);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        Utils.copyFolder(file,
                Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator));

        File projectFile = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator
                + CPP_PROJ + File.separator + GuiUtils.DOT_PROJECT);
        try {
            ProjectImporter.importProject(projectFile.toPath(), CPP_PROJ);
        } catch (CoreException e1) {
            e1.printStackTrace();
        }

        project = bot.tree().getTreeItem(CPP_PROJ);
        project.contextMenu(GuiUtils.ADD_NATURE_MENU).click();
    }

    /**
     * Delete project from Workspace so others can import it.
     */
    @AfterClass
    public static void afterClass() {
        GuiUtils.deleteProject(CPP_PROJ, true, bot);
    }

    /**
     * Open preferences, CodeChecker page before every test.
     */
    @Before
    public void openPreferences() {
        preferencesShell = GuiUtils.getPreferencesTab(CODECHECKER, bot);
    }

    /**
     * Changes Configuration on the imported project.
     */
    @Test
    public void testCodeCheckerConfigAdded() {
        // set CodeChecker directory on preferences.
        GuiUtils.setCCBinDir(ccDir, bot);
        GuiUtils.applyClosePreferences(preferencesShell, bot);

        project = bot.tree().getTreeItem(CPP_PROJ);
        project.contextMenu(GuiUtils.PROPERTIES).click();

        SWTBotShell propertiesShell = bot.shell(GuiUtils.PROPERTIES_FOR + CPP_PROJ);
        propertiesShell.activate();
        bot.tree().getTreeItem(GuiUtils.CODECHECKER).select();

        // Check that global is selected
        assertThat(ERROR_GLOBAL, bot.radio(GuiUtils.GLOBAL_RADIO).isSelected(),
                is(true));
        // Check that fields are greyed out.
        assertThat(ERROR_GLOBAL,
                bot.textWithLabel(GuiUtils.CC_DIR_WIDGET).isEnabled(), is(false));

        SWTBotTreeItem build = bot.tree().getTreeItem(GuiUtils.C_CPP_BUILD).expand();
        build.getNode(GuiUtils.ENVIRONMENT).select();

        assertThat(ERROR_ENVIR_LOGGER_BIN,
                bot.table().getTableItem(GuiUtils.ENVIR_LOGGER_BIN).getText(1),
                equalTo(Paths.get(ccDir.toString(), GuiUtils.BIN, GuiUtils.LDOGGER).toString()));

        assertThat(ERROR_ENVIR_LOGGER_BIN,
                bot.table().getTableItem(GuiUtils.ENVIR_LOGGER_FILE).getText(1),
                equalTo(Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(),
                        GuiUtils.DOT_CODECHECKER,
                        CPP_PROJ, GuiUtils.COMP_COMMANDS).toString()));

        bot.tree().getTreeItem(GuiUtils.CODECHECKER).select();

        bot.radio(GuiUtils.PROJECT_RADIO).click();

        GuiUtils.setCCBinDir(ccModifDir, bot);

        bot.button(GuiUtils.APPLY).click();

        build.getNode(GuiUtils.ENVIRONMENT).select();

        assertThat(ERROR_ENVIR_LOGGER_BIN,
                bot.table().getTableItem(GuiUtils.ENVIR_LOGGER_BIN).getText(1),
                equalTo(Paths.get(ccModifDir.toString(), GuiUtils.BIN, GuiUtils.LDOGGER).toString()));

        bot.button(GuiUtils.APPLY_AND_CLOSE).click();
        bot.waitUntil(Conditions.shellCloses(propertiesShell));
    }

    /**
     * The critical remark here is that a new project is initialized correctly
     * without poking anything.
     */
    @Test
    public void testCodeCheckerConfigNewProject() {
        GuiUtils.applyClosePreferences(preferencesShell, bot);

        bot.menu("File").menu("New").menu(GuiUtils.CPP_PROJECT).click();

        SWTBotShell shell = bot.shell("C++ Project");
        shell.activate();

        bot.textWithLabel("&Project name:").setText(CR_PROJ);
        bot.tree().getTreeItem("Executable").getNode("Hello World C++ Project").select();
        bot.button(GuiUtils.FINISH).click();
        bot.editorByTitle("createdProject.cpp").show();

        project = bot.tree().getTreeItem(CR_PROJ);
        project.contextMenu(GuiUtils.ADD_NATURE_MENU).click();

        preferencesShell = GuiUtils.getPreferencesTab(CODECHECKER, bot);

        GuiUtils.setCCBinDir(ccDir, bot);
        GuiUtils.applyClosePreferences(preferencesShell, bot);

        project.contextMenu(GuiUtils.PROPERTIES).click();

        SWTBotShell propertiesShell = bot.shell(GuiUtils.PROPERTIES_FOR + CR_PROJ);
        propertiesShell.activate();
        bot.tree().getTreeItem(GuiUtils.CODECHECKER).select();

        // Check that global is selected
        assertThat(ERROR_GLOBAL, bot.radio(GuiUtils.GLOBAL_RADIO).isSelected(),
                is(true));

        SWTBotTreeItem build = bot.tree().getTreeItem(GuiUtils.C_CPP_BUILD).expand();
        build.getNode(GuiUtils.ENVIRONMENT).select();

        assertThat(ERROR_ENVIR_LOGGER_BIN,
                bot.table().getTableItem(GuiUtils.ENVIR_LOGGER_BIN).getText(1),
                equalTo(Paths.get(ccDir.toString(), GuiUtils.BIN, GuiUtils.LDOGGER).toString()));

        assertThat(ERROR_ENVIR_LOGGER_BIN,
                bot.table().getTableItem(GuiUtils.ENVIR_LOGGER_FILE).getText(1),
                equalTo(Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(),
                        GuiUtils.DOT_CODECHECKER,
                        CR_PROJ, GuiUtils.COMP_COMMANDS).toString()));

        GuiUtils.applyCloseProperties(propertiesShell, bot);

        bot.sleep(GuiUtils.SHORT_WAIT_TIME);
        GuiUtils.deleteProject(CR_PROJ, true, bot);
    }
}
