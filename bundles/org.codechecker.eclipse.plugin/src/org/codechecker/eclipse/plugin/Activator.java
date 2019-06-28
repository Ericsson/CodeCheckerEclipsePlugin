package org.codechecker.eclipse.plugin;

import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import org.codechecker.eclipse.plugin.init.StartupJob;
import org.codechecker.eclipse.plugin.views.console.ConsoleFactory;

import java.net.URL;

import org.apache.log4j.PropertyConfigurator;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup {
	
    // The plug-in ID
    public static final String PLUGIN_ID = "org.codechecker.eclipse.plugin"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private static IPath preferencesPath;

    /**
     * The constructor
     */
    public Activator() {
    	Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        final URL fullPathString = FileLocator.find(bundle, new Path("log4j.properties"), null);
    	PropertyConfigurator.configure(fullPathString);
    }

    public static IPath getPreferencesPath() {
        return preferencesPath;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        ConsoleFactory.consoleWrite("CodeChecker Plugin Started");
        super.start(context);

        //earlyStartup();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        ConsoleFactory.consoleWrite("CodeChecker Plugin Stopped");
        super.stop(context);
    }

    @Override
    public void earlyStartup() {
        plugin = this;

        preferencesPath = getStateLocation();

        Job j = new StartupJob();
        j.setPriority(Job.INTERACTIVE);
        j.schedule();
    }
}
