package org.codechecker.eclipse.plugin.utils;

import java.nio.file.Path;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.ui.PlatformUI;

/**
 * Helper functions for navigating the gui.
 *
 */
public final class GuiUtils {

    public static final String DOT_PROJECT = ".project";
    public static final String APPLY = "Apply";
    public static final String APPLY_AND_CLOSE = "Apply and Close";
    public static final String BUILD_PROJ = "Build Project";
    public static final String CANCEL = "Cancel";
    public static final String C_CPP_BUILD = "C/C++ Build";
    public static final String C_CPP_PESPECTIVE = "C/C++";
    public static final String CPP_PROJECT = "C++ Project";
    public static final String DELETE_RESOURCES = "Delete Resources";
    public static final String ENVIRONMENT = "Environment";
    public static final String FINISH = "Finish";
    public static final String OK = "OK";
    public static final String OPEN_PERSP = "Open Perspective";
    public static final String OTHER_MENU = "Other...";
    public static final String PERSP_MENU = "Perspective";
    public static final String PREFERENCES = "Preferences";
    public static final String PROPERTIES = "Properties";
    public static final String PROPERTIES_FOR = "Properties for ";
    public static final String WINDOW_MENU = "Window";
    // CodeChecker related strings.
    public static final String DOT_CODECHECKER = ".codechecker";
    public static final String ADD_NATURE_MENU = "Add CodeChecker Nature";
    public static final String COMP_COMMANDS = "compilation_commands.json.javarunner";
    public static final String CURRENT_PROJ = "Current Project";
    public static final String BIN = "bin";
    public static final String CC_DIR_WIDGET = "CodeChecker package root directory";
    public static final String CODECHECKER = "CodeChecker";
    public static final String ENVIR_LOGGER_BIN = "CC_LOGGER_BIN";
    public static final String ENVIR_LOGGER_FILE = "CC_LOGGER_FILE";
    public static final String LDOGGER = "ldlogger";
    public static final String PY_DIR_WIDGET = "Python virtualenv root directory (optional)";
    public static final String GLOBAL_RADIO = "Use global configuration";
    public static final String PROJECT_RADIO = "Use project configuration";
    public static final String TOGGLE_CHECKERS = "Toggle enabled checkers";
    
    public static final int SHORT_WAIT_TIME = 500; // in milliseconds
    /**
     * Not called.
     */
    private GuiUtils() {}
    
    /**
     * Closes a preferences/properties type shell. Use {@link applyClosePreferences}
     * or {@link applyCloseProperties} for better readability.
     * 
     * @param shell
     *            The shell to be closed.
     * @param apply
     *            If true Apply and Close will be clicked, else Cancel.
     * @param bot
     *            The bot to be used.
     */
    public static void closeShell(SWTBotShell shell, boolean apply, SWTWorkbenchBot bot) {
        if (apply)
            bot.button(GuiUtils.APPLY_AND_CLOSE).click();
        else
            bot.button(GuiUtils.CANCEL).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    /**
     * Applies and closes and waits for closure of Preferences.
     * 
     * @param preferencesShell
     *            The Preferences shell to be closed.
     * @param bot
     *            The bot to be used.
     */
    public static void applyClosePreferences(SWTBotShell preferencesShell, SWTWorkbenchBot bot) {
        closeShell(preferencesShell, true, bot);
    }

    /**
     * Applies and closes and waits for closure of Properties.
     * 
     * @param propertiesShell
     *            The Properties shell to be closed.
     * @param bot
     *            The bot to be used.
     */
    public static void applyCloseProperties(SWTBotShell propertiesShell, SWTWorkbenchBot bot) {
        closeShell(propertiesShell, true, bot);
    }

    /**
     * You MUST navigate to the correct preferences page, or the method fails and
     * possibly throws widget not found exception. Sets The CodeChecker Directory on
     * the preferences pages.
     * 
     * @param ccDir
     *            Path to the CodeChecker Root directory.
     * @param bot
     *            The bot to be guided.
     */
    public static void setCCBinDir(Path ccDir, SWTWorkbenchBot bot) {
        SWTBotText text = bot.textWithLabel(GuiUtils.CC_DIR_WIDGET);
        text.setText(ccDir.toString());
        text.setFocus();
        bot.textWithLabel(GuiUtils.PY_DIR_WIDGET).setFocus();

        bot.sleep(SHORT_WAIT_TIME);
    }

    /**
     * Deletes a project from workspace.
     * 
     * @param projectName
     *            The project to be deleted.
     * @param deleteFromDisk
     *            Delete from the disk.
     * @param bot
     *            The workbench bot to be used.
     */
    public static void deleteProject(String projectName, boolean deleteFromDisk, SWTWorkbenchBot bot) {
        bot.tree().getTreeItem(projectName).contextMenu("Delete").click();
        bot.waitUntil(Conditions.shellIsActive(DELETE_RESOURCES));
        SWTBotShell shell = bot.shell(DELETE_RESOURCES);
        shell.activate();
        if (deleteFromDisk)
            bot.checkBox("Delete project contents on disk (cannot be undone)").select();
        bot.button(OK).click();
        bot.waitUntil(Conditions.shellCloses(shell));
    }

    /**
     * Convenience method for deleting a project. Exactly the same as
     * deleteProject(projectName, false, bot)
     * 
     * @param projectName
     *            The project to be deleted.
     * @param bot
     *            The workbench bot to be used.
     */
    public static void deleteProject(String projectName, SWTWorkbenchBot bot) {
        deleteProject(projectName, false, bot);
    }

    /**
     * Closes welcome window.
     * @param bot the workbench bot to be used.
     */
    public static void closeWelcomeIfPresent(SWTWorkbenchBot bot) {
        try {
            bot.viewByTitle("Welcome").close();
        } catch (WidgetNotFoundException e) {
            System.out.println("Welcome Screen wasn't present.");
        }
    }

    /**
     * Changes to CDT project.
     * 
     * @param perspective
     *            The perspective to be changed to.
     * @param bot
     *            the workbench bot to be used.
     */
    public static void changePerspectiveTo(String perspective, SWTWorkbenchBot bot) {
        UIThreadRunnable.syncExec(new VoidResult() {
            public void run() {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
            }
        });

        // Change the perspective via the Open Perspective dialog
        bot.menu(WINDOW_MENU).menu(PERSP_MENU).menu(OPEN_PERSP).menu(OTHER_MENU).click();
        SWTBotShell openPerspectiveShell = bot.shell(OPEN_PERSP);
        openPerspectiveShell.activate();

        // select the dialog
        bot.table().select(perspective);
        bot.button("Open").click();

    }

    /**
     * Open desired dialog specified by it's title in preferences dialog.
     * 
     * @param tab
     *            The desired preferences tab.
     * @param bot
     *            the workbench bot to be used.
     * @return The newly opened shell.
     */
    public static SWTBotShell getPreferencesTab(String tab, SWTWorkbenchBot bot) {
        bot.menu(WINDOW_MENU).menu(PREFERENCES).click();
        SWTBotShell shell = bot.shell(PREFERENCES);
        shell.activate();
        bot.tree().getTreeItem(tab).select();
        return shell;
    }
}
