package org.codechecker.eclipse.plugin.codechecker;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelper;

/**
 * Implementation of the {@link ICodeCheckerFactory} interface.
 */
public class CodeCheckerFactory implements ICodeCheckerFactory {
    @Override
    public ICodeChecker createCodeChecker(Path pathToBin, ShellExecutorHelper she)
            throws InvalidCodeCheckerException {
        return new CodeChecker(pathToBin, she);
    }
}
