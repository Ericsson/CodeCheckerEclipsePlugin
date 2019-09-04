package org.codechecker.eclipse.plugin.runtime;

import java.util.Map;

/**
 * Implementation of {@link IShellExecutorHelperFactory}.
 */
public class ShellExecutorHelperFactory implements IShellExecutorHelperFactory {

    @Override
    public ShellExecutorHelper createShellExecutorHelper(Map<String, String> env) {
        return new ShellExecutorHelper(env);
    }

}
