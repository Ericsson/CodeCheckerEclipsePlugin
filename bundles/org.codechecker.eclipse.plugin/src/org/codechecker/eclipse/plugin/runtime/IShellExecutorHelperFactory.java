package org.codechecker.eclipse.plugin.runtime;

import java.util.Map;

/**
 * Interface for {@link ShellExecutorHelper} factory.
 */
public interface IShellExecutorHelperFactory {
    /**
     * Method for creating {@link ShellExecutorHelper}.
     * 
     * @param env
     *            An environment to be used with the ShellExecutorHelper.
     * @return A {@link ShellExecutorHelper} instance.
     */
    public ShellExecutorHelper createShellExecutorHelper(Map<String, String> env);
}
