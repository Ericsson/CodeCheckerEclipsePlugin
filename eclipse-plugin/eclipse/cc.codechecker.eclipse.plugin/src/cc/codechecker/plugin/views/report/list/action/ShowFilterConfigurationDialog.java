package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import cc.codechecker.plugin.Activator;
import cc.codechecker.plugin.views.config.filter.FilterConfigurationDialog;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.net.URL;

import org.apache.log4j.Level;

public class ShowFilterConfigurationDialog extends TreeAwareAction {

    private static final Logger logger = LogManager.getLogger(ShowFilterConfigurationDialog.class.getName());

    public ShowFilterConfigurationDialog(ReportListView listView) {
        super(listView, "Show Filter Configurators", IAction.AS_PUSH_BUTTON);
        setToolTipText("Show Filter Configurators");
        Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
        final URL fullPathString = FileLocator.find(bundle, new Path("icons/filter.png"), null);
        setImageDescriptor(ImageDescriptor.createFromURL(fullPathString));
    }

    @Override
    public void run() {
        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        FilterConfigurationDialog dialog = new FilterConfigurationDialog(activeShell, listView
                .getActiveConfiguration().dup(), listView.getCurrentProject().getName());

        int result = dialog.open();

        if (result == 0) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Setting: " + dialog.getCurrentConfiguration().getName());
            listView.setActiveConfiguration(dialog.getCurrentConfiguration());
        }

        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> RESULT: " + result);
    }

}
