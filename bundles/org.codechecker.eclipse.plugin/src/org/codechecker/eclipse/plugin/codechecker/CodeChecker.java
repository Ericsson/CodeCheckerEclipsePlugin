package org.codechecker.eclipse.plugin.codechecker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, File> subMap;

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
        subMap = new HashMap<String, File>();
        subMap.put("location", path.toAbsolutePath().toFile());
        getVersion();
    }

    @Override
    public String getCheckers() {
        String cmd = "${location} checkers";
        Optional<String> ccOutput = she.waitReturnOutput(cmd, subMap, false);
        return ccOutput.or("No Checkers found");
    }

    @Override
    public String getVersion() throws InvalidCodeCheckerException {
        String cmd = "${location} version";
        Optional<String> ccOutput = she.waitReturnOutput(cmd, subMap, false);
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

        subMap.put("results", logFile.getParent().toAbsolutePath().resolve(Paths.get("results")).toFile());
        subMap.put("logFile", logFile.toAbsolutePath().toFile());
        String cmd = "${location} analyze " + config.get(ConfigTypes.CHECKER_LIST) + " -j "
                + config.get(ConfigTypes.ANAL_THREADS) + " -n javarunner" + " -o "
                + "${results} ${logFile}";

        SLogger.log(LogI.INFO, "Running analyze Command: " + cmd);
        Optional<String> ccOutput = she.progressableWaitReturnOutput(cmd, subMap, logToConsole, monitor, taskCount);

        return ccOutput.or("");
    }

}
