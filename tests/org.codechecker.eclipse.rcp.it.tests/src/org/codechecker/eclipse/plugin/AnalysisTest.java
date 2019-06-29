/**
 * 
 */
package org.codechecker.eclipse.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codechecker.eclipse.plugin.utils.CompilationLogCreator;
import org.codechecker.eclipse.plugin.utils.GuiUtils;
import org.codechecker.eclipse.plugin.utils.ProjectImporter;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * Integration test for testing the Analysis functionality of the plugin.
 */
public class AnalysisTest {

    private static final int LONG_TIMEOUT = 120000;
    private static final int DEF_TIMEOUT = 30000;

    private static final String CPP_PROJ = "cppTest";
    private static final String SRC = "src";
    private static final String CPP_SRC = "cppTest.cpp";
    private static final String UNIX = "unix";

    private static final String ERROR_MISSING_CHECKERS = "Reports were missing";
    private static Path ccDir = Utils.prepareCodeChecker();
    private static SWTBotTreeItem project;

    private static SWTWorkbenchBot bot;

    private static final Set<String> CHECKERS = new HashSet<String>() {
        {
            add(" core.uninitialized.Assign");
            add(" cplusplus.NewDeleteLeaks");
            add(" cplusplus.NewDelete");
            add(" cppcoreguidelines-no-malloc");
            add(" unix.Malloc");
            add(" unix.MallocSizeof");
        }
    };


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SWTBotShell preferencesShell;

    /**
     * Import project - Set nature.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
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

        // Add CodeChecker Nature.
        project = bot.tree().getTreeItem(CPP_PROJ);
        project.contextMenu(GuiUtils.ADD_NATURE_MENU).click();
    }

    /**
     * Test the analysis.
     */
    @Test
    public void test() {
        preferencesShell = GuiUtils.getPreferencesTab(GuiUtils.CODECHECKER, bot);
        GuiUtils.setCCBinDir(ccDir, bot);

        bot.sleep(GuiUtils.SHORT_WAIT_TIME);
        bot.button(GuiUtils.TOGGLE_CHECKERS).click();
        // BUG - Expected fail checkers should be configurable right after a correct
        // Codechecker is found! -> Apply shouldn't be clicked after the bug is fixed!
        // thrown.expect(java.lang.ArrayIndexOutOfBoundsException.class);
        // thrown.expectMessage(containsString("1"));

        preferencesShell.activate();
        bot.button(GuiUtils.APPLY).click();

        // bot.sleep(GuiUtils.SHORT_WAIT_TIME);
        bot.button(GuiUtils.TOGGLE_CHECKERS).click();
        SWTBotShell checkersShell = bot.activeShell();
        checkersShell.activate();

        // Check that initially all checkers show up in the disabled part of the dialog.
        List<String> checkers = new ArrayList<>();
        for (int i = 0 ; i < bot.table().rowCount() ; i++) {
            checkers.add(bot.table().getTableItem(i).getText());
        }
        assertThat(CHECKERS.stream().allMatch(str -> checkers.contains(str)), is(true));

        bot.buttonWithId("selectallbutton").click();

        // Check that all checkers moved into the enabled part of the dialog.
        List<String> checkersEnabled = new ArrayList<>();
        for (int i = 0; i < bot.table(1).rowCount(); i++) {
            checkersEnabled.add(bot.table(1).getTableItem(i).getText());
        }
        assertThat(CHECKERS.stream().allMatch(str -> checkersEnabled.contains(str)), is(true));
        assertThat("Disabled list should be empty", bot.table().getTableItem(0).getText(), isEmptyString());

        bot.button(GuiUtils.OK).click();
        bot.waitUntil(Conditions.shellCloses(checkersShell));

        preferencesShell.activate();

        GuiUtils.applyClosePreferences(preferencesShell, bot);

        bot.tree().getTreeItem(CPP_PROJ).select();
        bot.tree().getTreeItem(CPP_PROJ).doubleClick();
        bot.tree().getTreeItem(CPP_PROJ).getNode(SRC).expand();
        bot.tree().getTreeItem(CPP_PROJ).getNode(SRC).getNode(CPP_SRC).select();
        bot.tree().getTreeItem(CPP_PROJ).getNode(SRC).getNode(CPP_SRC).doubleClick();
        bot.editorByTitle(CPP_SRC).show();

        GuiUtils.changePerspectiveTo(GuiUtils.CODECHECKER, bot);
        CompilationLogCreator.createCompilationLog(CPP_PROJ);

        assertThat(ERROR_MISSING_CHECKERS, bot.tree(1).hasItems(), is(false));

        bot.viewByTitle(GuiUtils.CURRENT_PROJ).show();
        project.contextMenu(GuiUtils.BUILD_PROJ).click();

        bot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return bot.tree(1).getTreeItem(UNIX) != null;
            }

            @Override
            public String getFailureMessage() {
                return null;
            }
        });

        bot.tree(1).getTreeItem(UNIX).doubleClick();
        assertThat(ERROR_MISSING_CHECKERS, bot.tree(1).hasItems(), is(true));
    }

}
