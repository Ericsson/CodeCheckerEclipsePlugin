package org.codechecker.eclipse.plugin.codechecker;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface representing a CodeChecker package.
 */
public interface ICodeChecker {
    /**
     * Returns the unformatted output of the CodeChecker checkers command.
     * 
     * @return The checker list.
     */
    @NonNull
    public String getCheckers();

    /**
     * Returns the full and complete version string of the CodeChecker package. The
     * returned String will never be empty.
     * 
     * @return The version String.
     * @throws InvalidCodeCheckerException
     *             Thrown when no version string can be returned.
     */
    @NonNull
    public String getVersion() throws InvalidCodeCheckerException;

    /**
     * To get the location of the CodeChecker binary. The returned String will never
     * be empty.
     * 
     * @return The path.
     */
    public Path getLocation();

    /**
     * Returns the analyze command to be run, without extra parameters.
     * 
     * @param logFile
     *            A Path to the build log in the following format:
     *            http://clang.llvm.org/docs/JSONCompilationDatabase.html .
     * @param config
     *            The configuration being used.
     * @param skipFile
     *            Skipfile to be used.
     * @return The analyze command as String.
     */
    public String getAnalyzeString(CcConfigurationBase config, @Nullable Path logFile, @Nullable Path skipFile);

    /**
     * Executes CodeChecker check command on the build log received in the logFile
     * parameter.
     * 
     * @param logFile
     *            A Path to the build log in the following format:
     *            http://clang.llvm.org/docs/JSONCompilationDatabase.html .
     * @param logToConsole
     *            Flag for indicating console logging.
     * @param monitor
     *            ProgressMonitor for to be able to increment progress bar.
     * @param taskCount
     *            How many analyze step to be taken.
     * @param config
     *            The configuration being used.
     * @param FileToBeAnalyzed TODO
     * @return CodeChecker The full analyze command output.
     */
    public String analyze(Path logFile, boolean logToConsole, IProgressMonitor monitor, int taskCount,
            CcConfigurationBase config, Path FileToBeAnalyzed);

    public void cancelAnalyze();
}
