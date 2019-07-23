package org.codechecker.eclipse.plugin.codechecker;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface representing a CodeChecker package.
 */
public interface ICodeChecker {
    /**
     * Returns the unformatted output of the CodeChecker checkers command.
     * 
     * @return The checker list.
     */
    public String getCheckers();

    /**
     * Returns the full and complete version string of the CodeChecker package.
     * 
     * @return The version String.
     * @throws InvalidCodeCheckerException
     *             Thrown when no version string can be returned.
     */
    public String getVersion() throws InvalidCodeCheckerException;

    /**
     * To get the location of the CodeChecker binary.
     * 
     * @return The path.
     */
    public Path getLocation();

    /**
     * Executes CodeChecker check command on the build log received in the fileName
     * parameter.
     * 
     * @param logFile
     *            A Path to the build log in the followin format:
     *            http://clang.llvm.org/docs/JSONCompilationDatabase.html .
     * @param logToConsole
     *            Flag for indicating console logging.
     * @param monitor
     *            ProgressMonitor for to be able to increment progress bar.
     * @param taskCount
     *            How many analyze step to be taken.
     * @param config
     *            The configuration being used.
     * @return CodeChecker The full analyze command output.
     */
    public String analyze(Path logFile, boolean logToConsole, IProgressMonitor monitor, int taskCount,
            CcConfigurationBase config);
}
