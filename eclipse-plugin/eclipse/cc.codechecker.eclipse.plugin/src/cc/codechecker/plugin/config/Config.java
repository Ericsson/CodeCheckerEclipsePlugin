package cc.codechecker.plugin.config;

public class Config {
	public interface ConfigTypes {}
	
    public enum ConfigTypesCommon implements ConfigTypes {
        CHECKER_PATH, 
        PYTHON_PATH, 
        COMPILERS, 
        ANAL_THREADS,
        CHECKER_LIST;

		public static ConfigTypes GetFromString(String s) {
			for (ConfigTypes c :ConfigTypesCommon.values()) {
				if (s.equals(c.toString())) 
					return c;
			}
			return null;
		}
    }
    
    public enum ConfigTypesProject implements ConfigTypes {
    	IS_GLOBAL, 
    	CHECKER_WORKSPACE;
    	
		public static ConfigTypes GetFromString(String s) {
			for (ConfigTypes c :ConfigTypesProject.values()) {
				if (s.equals(c.toString())) 
					return c;
			}
			return null;
		}
    }
}
