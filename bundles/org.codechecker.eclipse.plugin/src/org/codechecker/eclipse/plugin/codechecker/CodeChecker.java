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
import org.eclipse.jdt.annotation.NonNull;

import com.google.common.base.Optional;

/**
 * Internal representation of a CodeChecker package.
 */
public class CodeChecker implements ICodeChecker {

    private static final String LOCATION_KEY = "location";
    private static final String RESULTS_KEY = "results";
    private static final String LOGFILE_KEY = "logFile";
    private static final String LOCATION_SUB = "${location}";
    private static final String RESULTS_SUB = "${results}";
    private static final String LOGFILE_SUB = "${logFile}";

    private static final String RESULTS_FOLDER = RESULTS_KEY;

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
        subMap.put(LOCATION_KEY, path.toAbsolutePath().toFile());
        getVersion();
    }

    @Override
    @NonNull
    public String getCheckers() {
        String cmd = LOCATION_SUB + " checkers";
        Optional<String> ccOutput = she.waitReturnOutput(cmd, subMap, false);
        return ccOutput.or("No Checkers found");
    }

    @Override
    @NonNull
    public String getVersion() throws InvalidCodeCheckerException {
        String cmd = LOCATION_SUB + " version";
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

        subMap.put(RESULTS_KEY, logFile.getParent().toAbsolutePath().resolve(Paths.get(RESULTS_FOLDER)).toFile());
        subMap.put(LOGFILE_KEY, logFile.toAbsolutePath().toFile());
        String cmd = getSubstituteAnalyzeString(config);

        SLogger.log(LogI.INFO, "Running analyze Command: " + cmd);
        Optional<String> ccOutput = she.progressableWaitReturnOutput(cmd, subMap, logToConsole, monitor, taskCount);

        return ccOutput.or("");
    }

    @Override
    public String getAnalyzeString(CcConfigurationBase config, Path logFile) {
        if (logFile != null) {
            subMap.put(RESULTS_KEY, logFile.getParent().toAbsolutePath().resolve(Paths.get(RESULTS_FOLDER)).toFile());
            subMap.put(LOGFILE_KEY, logFile.toAbsolutePath().toFile());
        }

        String[] temp = getSubstituteAnalyzeString(config).split(" ");
        StringBuilder cmd = new StringBuilder();
        for (String s : temp) {
            if (s.startsWith("${")) {
                StringBuilder sb = new StringBuilder(s);
                sb.delete(0, 2).deleteCharAt(sb.length() - 1);
                if (subMap.containsKey(sb.toString()))
                    s = subMap.get(sb.toString()).toString();
            }
            cmd.append(s);
            cmd.append(' ');
        }
        return cmd.toString();
    }

    private String getSubstituteAnalyzeString(CcConfigurationBase config) {
        return LOCATION_SUB + " analyze" + " -j "
                + config.get(ConfigTypes.ANAL_THREADS) + " -n javarunner" + " -o " + RESULTS_SUB + " " + LOGFILE_SUB
                + " " + config.get(ConfigTypes.ANAL_OPTIONS);
    }
}
