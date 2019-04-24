package org.codechecker.eclipse.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.utils.Utils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.osgi.framework.Bundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the gui.
 */
public class PluginTest {

    private static final String CPP_PROJ = "cppTest";
    private static final String WINDOW_MENU = "Window";
    private static final String PERSP_MENU = "Perspective";
    private static final String OPEN_PERSP = "Open Perspective";
    private static final String OTHER_MENU = "Other...";
    private static final String ADD_NATURE_MENU = "Add CodeChecker Nature";

    private static SWTWorkbenchBot bot;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Import cpp project into workspace, and setup SWTBot.
     *
     */
    @BeforeClass
    public static void setup() {

        // http://blog.vogella.com/2010/07/06/reading-resources-from-plugin/
        Bundle bundle = Platform.getBundle("org.codechecker.eclipse.rcp.it.tests");
        URL fileURL = bundle.getEntry("resources/cppTest");
        File file = null;
        try {
            file = new File(FileLocator.resolve(fileURL).toURI());
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        assertThat("File not exists.", file.exists());
        Utils.copyFolder(file.toPath(),
                Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator));

        File project = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + File.separator
                + CPP_PROJ + File.separator + ".project");
        try {
            importProject(project, CPP_PROJ);
        } catch (CoreException e1) {
            e1.printStackTrace();
        }

        bot = new SWTWorkbenchBot();
        UIThreadRunnable.syncExec(new VoidResult() {
            public void run() {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
            }
        });

        try {
            bot.viewByTitle("Welcome").close();
        } catch (WidgetNotFoundException e) {
            System.out.println("Welcome Screen wasn't present.");
        }

        // Change the perspective via the Open Perspective dialog
        bot.menu(WINDOW_MENU).menu(PERSP_MENU).menu(OPEN_PERSP).menu(OTHER_MENU).click();
        SWTBotShell openPerspectiveShell = bot.shell(OPEN_PERSP);
        openPerspectiveShell.activate();

        // select the dialog
        bot.table().select("C/C++");
        bot.button("Open").click();

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


    /**
     * Imports a project into workspace.
     * https://www.eclipse.org/forums/index.php/t/560903/
     *
     * @param projectFile
     *            The project file to be imported.
     * @param projectName
     *            The project name that will be used to create the project
     * @throws CoreException
     *             Project cannot be created: if this method fails. Reasons include:
     *             - This project already exists in the workspace. - The name of
     *             this resource is not valid (according to
     *             IWorkspace.validateName). - The project location is not valid
     *             (according to IWorkspace.validateProjectLocation). - The project
     *             description file could not be created in the project content
     *             area. - Resource changes are disallowed during certain types of
     *             resource change event notification. See IResourceChangeEvent for
     *             more details. .project file has troubles. Reasons include: - The
     *             project description file does not exist. - The file cannot be
     *             opened or read. - The file cannot be parsed as a legal project
     *             description. or during opening - Resource changes are disallowed
     *             during certain types of resource change event notification. See
     *             IResourceChangeEvent for more details.
     */
    private static void importProject(final File projectFile, final String projectName) throws CoreException {
        IProjectDescription description = ResourcesPlugin.getWorkspace()
                .loadProjectDescription(new Path(projectFile.getAbsolutePath()));
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
        project.create(description, null);
        project.open(null);
    }
}
