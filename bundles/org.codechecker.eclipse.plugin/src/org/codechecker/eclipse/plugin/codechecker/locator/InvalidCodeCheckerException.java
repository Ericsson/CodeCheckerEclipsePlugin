package org.codechecker.eclipse.plugin.codechecker.locator;

/**
 * Custom exception indicating a failed CodeChecker instance creation.
 */
@SuppressWarnings("serial")
public class InvalidCodeCheckerException extends Exception {
    /**
     * Ctor.
     * 
     * @param errorMessage
     *            Error message.
     */
    public InvalidCodeCheckerException(String errorMessage) {
        super(errorMessage);
    }
}
