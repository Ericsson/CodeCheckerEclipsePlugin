package org.codechecker.eclipse.plugin.codechecker.locator;

/**
 * This enum represents the available types of CodeChecker resolution methods.
 */
public enum ResolutionMethodTypes {
    PATH("PATH"), PRE("PRE"), PY("PY");
    
    private String value;
    
    /**
    *
    * @param def utility method for setting the default value.
    */
    private ResolutionMethodTypes(String def) {
        this.value = def;
    }

    /**
     *
     * @return Returns the value associated with the enum.
     */
    public String getDefaultValue() {
        return value;
    }

    /**
     *
     * @param s
     *            The query string.
     * @return The matching ResolutionMethodTypes if exists null otherwise.
     */
    public static ResolutionMethodTypes getFromString(String s) {
        for (ResolutionMethodTypes r : ResolutionMethodTypes.values()) {
            if (s.equals(r.toString()))
                return r;
        }
        return null;
    }

}
