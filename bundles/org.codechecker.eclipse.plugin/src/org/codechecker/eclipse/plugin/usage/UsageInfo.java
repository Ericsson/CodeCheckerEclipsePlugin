package org.codechecker.eclipse.plugin.usage;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.gson.annotations.SerializedName;

/**
 * Class for Storing usage logging related info.
 */
public class UsageInfo {
    private static final String UNKNOWN = "unknown";
    private static final String UNKNOWN_VER = "unknown version";

    @SuppressWarnings("unused")
    private final String machine;
    @SuppressWarnings("unused")
    private final String hostname;
    //@SuppressWarnings("unused")
    //private String clangsa = UNKNOWN;
    @SuppressWarnings("unused")
    private final String version;
    // TODO make this parameter dynamic, from build parameters.
    @SuppressWarnings("unused")
    private final String pluginVersion;
    @SuppressWarnings("unused")
    private final String user;
    @SuppressWarnings("unused")
    @SerializedName("command_type")
    private final CommandType commandType;
    //@SuppressWarnings("unused")
    //private String clang_tidy = UNKNOWN;

    /**
     * Specify the event type and the CodeChecker version if in context.
     * 
     * @param ct
     *            The command (event) type to be logged.
     * @param ccVersion
     *            The CodeChecker version to be logged. Not every context has
     *            CodeChecker.
     */
    public UsageInfo(CommandType ct, @Nullable String ccVersion) {
        StringBuilder tempos = new StringBuilder(System.getProperty("os.name"));
        tempos.append(" ").append(System.getProperty("os.version"));
        tempos.append(" / Eclipse ").append(getEclipseVersion());
        machine = tempos.toString();
        String tHostName = UNKNOWN;
        try {
            tHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            ;
        }
        hostname = tHostName;

        if (ccVersion != null)
            version = ccVersion;
        else
            version = UNKNOWN_VER;

        pluginVersion = setPluginVersion();
        user = System.getProperty("user.name");
        commandType = ct;
    }

    /**
     * Used for returning the eclipse version number. From:
     * https://stackoverflow.com/a/28855362/8149485
     * 
     * @return The eclipse version number in 1.2.3.v19700101-0000 format.
     */
    private String getEclipseVersion() {
        String version = UNKNOWN_VER;
        String product = System.getProperty("eclipse.product");
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.eclipse.core.runtime.products");
        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            for (IExtension ext : extensions)
                if (product.equals(ext.getUniqueIdentifier())) {
                    IContributor contributor = ext.getContributor();
                    if (contributor != null) {
                        Bundle bundle = Platform.getBundle(contributor.getName());
                        if (bundle != null) 
                            version = bundle.getVersion().toString();
                    }
                }
        }
        return version;
    }

    /**
     * Sets the plugin version from a properties file, which gets substituted during
     * build.
     * 
     * @return The plugin version read from the config file.
     */
    private String setPluginVersion() {
        String ver = UNKNOWN_VER;
        try (InputStream is = FileLocator.openStream(FrameworkUtil.getBundle(getClass()),
                new Path("resources/config.properties"), false)) {
            Properties prop = new Properties();
            prop.load(is);
            ver = prop.getProperty("version");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return ver;
    }

    /**
     * Command (Event) types.
     */
    public enum CommandType {
        started, analyze_started
    }
}
