package org.codechecker.eclipse.plugin.codechecker;

import java.nio.file.Path;

import org.codechecker.eclipse.plugin.codechecker.locator.InvalidCodeCheckerException;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.runtime.LogI;
import org.codechecker.eclipse.plugin.runtime.SLogger;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelper;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.base.Optional;

/**
 * Internal representation of a CodeChecker package.
 */
public class CodeChecker implements ICodeChecker {

    private Path location;
    private ShellExecutorHelper she;

    /**
     * 
     * @param path
     *            Path to the binary itself.
     * @param she
     *            The ShellExecutor to be used.
     * 
     * @throws InvalidCodeCheckerException
     *             Thrown when no CodeChecker found.
     */
    public CodeChecker(Path path, ShellExecutorHelper she) throws InvalidCodeCheckerException {
        location = path;
        this.she = she;
        getVersion();
    }

    @Override
    public String getCheckers() {
        String cmd = location.toAbsolutePath().toString() + " checkers";
        Optional<String> ccOutput = she.waitReturnOutput(cmd, false);
        return ccOutput.or("No Checkers found");
    }

    @Override
    public String getVersion() throws InvalidCodeCheckerException {
        String cmd = location.toAbsolutePath().toString() + " version";
        Optional<String> ccOutput = she.waitReturnOutput(cmd, false);
        if (!ccOutput.isPresent() || ccOutput.get().isEmpty())
            throw new InvalidCodeCheckerException("Couldn't run CodeChecker version!");
        return ccOutput.get();
    }

    @Override
    public Path getLocation() {
        return location;
    }

    @Override
    public String analyze(Path logFile, boolean logToConsole, IProgressMonitor monitor, int taskCount,
            CcConfigurationBase config) {

        String cmd = location.toAbsolutePath().toString() + " analyze " + config.get(ConfigTypes.CHECKER_LIST) + " -j "
                + config.get(ConfigTypes.ANAL_THREADS) + " -n javarunner" + " -o "
                + logFile.toAbsolutePath().getParent().toString() + "/results/ "
                + logFile.toAbsolutePath().toString();

        SLogger.log(LogI.INFO, "Running analyze Command: " + cmd);
        Optional<String> ccOutput = she.progressableWaitReturnOutput(cmd, logToConsole, monitor, taskCount);

        return ccOutput.or("");
    }

}
