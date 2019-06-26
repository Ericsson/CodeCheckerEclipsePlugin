package org.codechecker.eclipse.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.utils.ProjectImporter;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the gui.
 */
public class PluginTest {

    private static final String CPP_PROJ = "cppTest";
    private static final String ADD_NATURE_MENU = "Add CodeChecker Nature";

    private static SWTWorkbenchBot bot;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Import cpp project into workspace, and setup SWTBot.
     */
    @BeforeClass
    public static void setup() {

        bot = new SWTWorkbenchBot();

        Path file = null;
        try {
            file = Utils.loadFileFromBundle("org.codechecker.eclipse.rcp.it.tests",
                    Utils.RES + CPP_PROJ);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        
        Utils.copyFolder(file,
                Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator));

        File project = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator
                + CPP_PROJ + File.separator + ".project");
        try {
            ProjectImporter.importProject(project.toPath(), CPP_PROJ);
        } catch (CoreException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Test that after adding nature to a C project, the add nature menu item disappears.
     */
    @Test
    public void testAddNatureDisappears() {

        SWTBotTreeItem project = bot.tree().getTreeItem(CPP_PROJ).doubleClick();
        SWTBotMenu menu = project.contextMenu(ADD_NATURE_MENU);

        assertThat("Add CodeChecker Nature menu item wasn't enabled", menu.isEnabled(), is(true));

        menu.click();

        // Widget should be missing now.
        thrown.expect(WidgetNotFoundException.class);
        thrown.expectMessage(containsString("Could not find"));
        project.contextMenu(ADD_NATURE_MENU);
    }
}