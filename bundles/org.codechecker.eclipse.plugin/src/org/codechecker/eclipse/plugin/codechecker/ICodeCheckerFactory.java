package org.codechecker.eclipse.plugin.codechecker;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelper;

/**
 * Interface for CodeChecker factory.
 */
public interface ICodeCheckerFactory {
    /**
     * Method for creating CodeChecker instances.
     * 
     * @param pathToBin
     *            Path to the CodeChecker binary. (Not to root!)
     * @param she
     *            The shell executor helper that will be used.
     * @return A newly created {@link ICodeChecker} ICodeChecker instance.
     * @throws InvalidCodeCheckerException
     *             Thrown when a new instance couldn't be created.
     */
    public ICodeChecker createCodeChecker(Path pathToBin, ShellExecutorHelper she)
            throws InvalidCodeCheckerException;
}
