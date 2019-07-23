package org.codechecker.eclipse.plugin.codechecker.locator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codechecker.eclipse.plugin.codechecker.ICodeChecker;
import org.codechecker.eclipse.plugin.codechecker.ICodeCheckerFactory;
import org.codechecker.eclipse.plugin.runtime.IShellExecutorHelperFactory;
import org.codechecker.eclipse.plugin.runtime.ShellExecutorHelper;

import com.google.common.base.Optional;

/**
 * Provides a CodeChecker instance which is tied to a Custom built CodeChecker
 * package.
 */
public class CustomBuiltCodeCheckerLocatorService extends CodeCheckerLocatorService {
    public static final String ERROR = "Couldn't find CodeChecker at the given destination!";
    public static final String ENV_ERROR = "There was an error when reading the given python environment!";

    @Override
    public ICodeChecker findCodeChecker(Path pathToBin, Path pathToVenv,
            ICodeCheckerFactory ccfactory, IShellExecutorHelperFactory sheFactory) throws InvalidCodeCheckerException {

        ShellExecutorHelper she = sheFactory.createShellExecutorHelper(System.getenv());
        Map<String, File> subMap = new HashMap<String, File>();
        subMap.put("venv", pathToVenv.resolve(Paths.get("bin", "activate")).toFile());
        Optional<String> output = she
                .quickReturnOutput("source ${venv} ; env", subMap);
        if (!output.isPresent())
            throw new IllegalArgumentException();
        try {
            Map<String, String> env = parseEnvironment(output.get());
            ShellExecutorHelper pyShe = sheFactory.createShellExecutorHelper(env);
            return ccfactory.createCodeChecker(pathToBin, pyShe);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(ENV_ERROR);
        } catch (InvalidCodeCheckerException e) {
            throw new InvalidCodeCheckerException(ERROR);
        }
    }

    /**
     * Returns a String - String map from a raw "Key=Value\n..." String.
     * @param envOutput The String input.
     * @return A Map<String,String> containing all the environment variables.
     */
    private Map<String, String> parseEnvironment(String envOutput){
        Map<String, String> map = Stream.of(envOutput.split("\n")).map(String::trim).map(line -> line.split("="))
                .collect(Collectors.toMap(k -> k[0], v -> v.length < 2 ? "" : v[1]));
        return map;
    }
}
