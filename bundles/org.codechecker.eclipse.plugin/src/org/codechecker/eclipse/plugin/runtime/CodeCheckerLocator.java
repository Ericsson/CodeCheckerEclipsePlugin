package org.codechecker.eclipse.plugin.runtime;

import com.google.common.base.Optional;

import java.io.IOException;

/**
 * Locates a local CodeChecker installation.
 * Either the CodeChecker executable is in the path, or the class is
 * initialized with a path to the CodeChecker package root directory.
 */
public class CodeCheckerLocator {

    // Linux-specific path to CodeChecker executable in PATH
    private final Optional<String> systemCcExecutable;
    // Linux-specific CodeChecker executable in CodeChecker package directory
    //     given by the user on the configuration panel
    private final Optional<String> customCcExecutable;
    private ShellExecutorHelper shellExecutor; // TODO: inject?

    public CodeCheckerLocator(ShellExecutorHelper executor, Optional<String> customCcExecutable)
            throws IOException {
        this.shellExecutor = executor;
        this.systemCcExecutable = locateSystemCodeChecker();
        this.customCcExecutable = customCcExecutable;
    }

    public Optional<String> getRunnerCommand() {
        return customCcExecutable.or(systemCcExecutable);
    }

    public boolean foundCcExecutable() {
        return customCcExecutable.isPresent() || systemCcExecutable.isPresent();
    }

    private Optional<String> locateSystemCodeChecker() {
        return shellExecutor.quickReturnFirstLine("/usr/bin/which CodeChecker", null);
    }

}
