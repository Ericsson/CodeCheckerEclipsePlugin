package org.codechecker.eclipse.plugin.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Interface for CodeChecker configuration.
 */
public abstract class CcConfigurationBase {

    protected static final String STR_EMPTY = "";
    
    protected Map<ConfigTypes, String> config;
    protected IEclipsePreferences preferences;

    private final Set<ConfigurationChangedListener> listeners = new HashSet<>();

    /**
     * Base Constructor.
     */
    public CcConfigurationBase() {}

    /**
     * Copy constructor for initializing.
     * @param other The other instance.
     */
    public CcConfigurationBase(CcConfigurationBase other) {
        this.config = other.get().entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    /**
     * For Updates the configuration.
     * @param config The new configuration.
     */
    public abstract void update(Map<ConfigTypes, String> config);

    /**
     * For initializing the internal configuration.
     */
    public abstract void load();

    /**
     * For checking that the loaded configuration is valid.
     */
    protected abstract void validate();

    /**
     * @return the internal configuration.
     */
    public Map<ConfigTypes, String> get() {
        return config;
    }

    /**
     * @param type The key of the value that we are interested in.
     * @return The value.
     */
    public String get(ConfigTypes type) {
        return config.get(type);
    }

    /**
     * Returns the default configuration for the plug-in.
     * @return An Unmodifiable view of the default configuration Map.
     */
    public Map<ConfigTypes, String> getDefaultConfig() {
        return ConfigTypes.getCommonDefault();
    }

    /**
     * @param listener The listener to be added.
     */
    public void registerChangeListener(ConfigurationChangedListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener The listener to be removed.
     */
    public void deRegisterChangeListener(ConfigurationChangedListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    /**
     * Notifies registered listeners that the configuration had been changed.
     * @param config the new configuration.
     */
    public void notifyListeners(Map<ConfigTypes, String> config) {
        for (ConfigurationChangedListener configurationChangedListener : listeners) {
            configurationChangedListener.configUpdated(config);
        }
    }

    /**
     * Implement this interface and use {@link registerChangeListener} to get
     * notifications about configuration changes. 
     */
    public interface ConfigurationChangedListener{
        /**
         * {@link notifyListenersn} will call this method when the configuration changes.
         * @param config The new configuration.
         */
        public void configUpdated(Map<ConfigTypes, String> config);
    }
}
