package org.codechecker.eclipse.plugin.codechecker.locator;

/**
 * Class responsible to create {@link CodeCheckerLocatorService} instances.
 */
public class CodeCheckerLocatorFactory {
    /**
     * Returns a {@link CodeCheckerLocatorService} depending on a the input
     * parameter.
     * 
     * @param t
     *            Any of the {@link ResolutionMethodTypes} enum values.
     * @return A {@link CodeCheckerLocatorService} instance.
     */
    public CodeCheckerLocatorService create(ResolutionMethodTypes t) {
        switch (t) {
            case PATH:
                return new EnvCodeCheckerLocatorService();
            case PRE:
                return new PreBuiltCodeCheckerLocatorService();
            default:
                return null;
        }
    }
}
