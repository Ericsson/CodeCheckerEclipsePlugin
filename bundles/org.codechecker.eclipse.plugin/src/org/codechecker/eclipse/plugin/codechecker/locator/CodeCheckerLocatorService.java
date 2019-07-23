package org.codechecker.eclipse.plugin.codechecker.locator;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.ICodeChecker;
import org.codechecker.eclipse.plugin.codechecker.ICodeCheckerFactory;
import org.codechecker.eclipse.plugin.runtime.IShellExecutorHelperFactory;

/**
 * Implementations of this interface should return the location of the
 * CodeChecker package.
 */
public abstract class CodeCheckerLocatorService {
    /**
     * @param pathToBin
     *            Path to CodeChecker package root.
     * @param pathToVenv
     *            Path to the root of the virtual environment.
     * @param ccfactory
     *            An {@link ICodeCheckerFactory} that will create the CodeChecker.
     * @param sheFactory
     *            A {@link IShellExecutorHelperFactory} to be used.
     * @return A CodeChecker Instance.
     * @throws InvalidCodeCheckerException
     *             Thrown when the {@link ICodeChecker} instantiation fails.
     */
    public abstract ICodeChecker findCodeChecker(Path pathToBin, Path pathToVenv, ICodeCheckerFactory ccfactory,
            IShellExecutorHelperFactory sheFactory)
            throws InvalidCodeCheckerException;
}
