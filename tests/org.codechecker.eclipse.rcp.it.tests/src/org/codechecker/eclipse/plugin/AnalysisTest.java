/**
 * 
 */
package org.codechecker.eclipse.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.utils.CompilationLogHelper;
import org.codechecker.eclipse.plugin.utils.GuiUtils;
import org.codechecker.eclipse.plugin.utils.ProjectImporter;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test for testing the Analysis functionality of the plugin.
 */
public class AnalysisTest {

    private static final String CPP_PROJ = "cppTest";
    private static final String SRC = "src";
    private static final String CPP_SRC = "cppTest.cpp";
    private static final String EDITOR = CPP_SRC;
    private static final String UNIX = "unix";
    private static final String UNIX_MALLOC = "unix.MallocSizeof";
    private static final String REPORT = "#1: cppTest.cpp [19:20]";
    private static final String REPORT_LONG = "cppTest.cpp : 19 : Result of 'malloc' is converted to a pointer of type 'long', which is incompatible with sizeof operand type 'short'";

    private static final String ERROR_MISSING_CHECKERS = "Reports were missing";
    private static Path ccDir = Utils.prepareCodeChecker();
    private static SWTBotTreeItem project;

    private static SWTWorkbenchBot bot;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        SWTBotShell preferencesShell = GuiUtils.getPreferencesTab(GuiUtils.CODECHECKER, bot);
        GuiUtils.setCCBinDir(ccDir, bot);
        GuiUtils.applyClosePreferences(preferencesShell, bot);

        bot.tree().getTreeItem(CPP_PROJ).select();
        bot.tree().getTreeItem(CPP_PROJ).doubleClick();
        bot.tree().getTreeItem(CPP_PROJ).getNode(SRC).expand();
        bot.tree().getTreeItem(CPP_PROJ).getNode(SRC).getNode(CPP_SRC).select();
        bot.tree().getTreeItem(CPP_PROJ).getNode(SRC).getNode(CPP_SRC).doubleClick();
        bot.editorByTitle(CPP_SRC).show();

        GuiUtils.changePerspectiveTo(GuiUtils.CODECHECKER, bot);
        CompilationLogHelper.createCompilationLog(CPP_PROJ);

        assertThat(ERROR_MISSING_CHECKERS, bot.tree(1).hasItems(), is(false));

        SWTBotButton analyzeBtn = bot.buttonWithTooltip("Reanalyze current file");
        SWTBotEditor editor = bot.activeEditor();
        editor.setFocus();

        analyzeBtn.click();

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

        bot.tree(1).getTreeItem(UNIX).getNode(UNIX_MALLOC).expand();
        bot.tree(1).getTreeItem(UNIX).getNode(UNIX_MALLOC).getNode(REPORT).expand();
        bot.tree(1).getTreeItem(UNIX).getNode(UNIX_MALLOC).getNode(REPORT).getNode(REPORT_LONG).select();
        bot.tree(1).getTreeItem(UNIX).getNode(UNIX_MALLOC).getNode(REPORT).getNode(REPORT_LONG).doubleClick();

        assertThat("Editor did not get active upon report selection", bot.activeEditor().getTitle().equals(EDITOR));
    }

}
