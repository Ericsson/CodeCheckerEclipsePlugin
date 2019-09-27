package org.codechecker.eclipse.plugin.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * This class can be used to create an (dummy) analyze log to the specified
 * project.
 */
public class CompilationLogHelper {

    /**
     * Never called.
     */
    private CompilationLogHelper() {
    }

    /**
     * Creates an empty compilation commands .json file, into the correct
     * destination with the correct name. This can be forwarded to the dummy
     * analysis tests.
     * 
     * @param projectName
     *            The project name to be used.
     */
    public static void createCompilationLog(String projectName) {
        // get the correct location which is eclipseWs/.codechecker/{$projectName}/
        Path workspaceRoot = Paths.get(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(),
                GuiUtils.DOT_CODECHECKER, projectName, CodeCheckerProject.COMPILATION_COMMANDS);
        // create a compilation log to the project.
        try {
            Files.createFile(workspaceRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
