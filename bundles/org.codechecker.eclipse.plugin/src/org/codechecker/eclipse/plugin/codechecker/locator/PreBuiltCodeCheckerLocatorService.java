package org.codechecker.eclipse.plugin.codechecker.locator;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.ICodeChecker;
import org.codechecker.eclipse.plugin.codechecker.ICodeCheckerFactory;
import org.codechecker.eclipse.plugin.runtime.IShellExecutorHelperFactory;

/**
 * Provides a CodeChecker instance which is tied to a pre-built CodeChecker
 * package.
 */
public class PreBuiltCodeCheckerLocatorService extends CodeCheckerLocatorService {
    public static final String INVALID = "The path to the CodeChecker binary is not valid";
    public static final String ERROR = "Couldn't find CodeChecker at the given destination!";

    @Override
    public ICodeChecker findCodeChecker(Path pathToBin, Path pathToVenv,
            ICodeCheckerFactory ccfactory, IShellExecutorHelperFactory sheFactory) throws InvalidCodeCheckerException {
        if (pathToBin == null)
            throw new IllegalArgumentException(INVALID);
        try {
            return ccfactory.createCodeChecker(pathToBin, sheFactory.createShellExecutorHelper(System.getenv()));
        } catch (InvalidCodeCheckerException e) {
            throw new InvalidCodeCheckerException(ERROR);
        }
    }

}
