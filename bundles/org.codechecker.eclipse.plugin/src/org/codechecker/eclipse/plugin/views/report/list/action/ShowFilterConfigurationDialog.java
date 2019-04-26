package org.codechecker.eclipse.plugin.views.report.list.action;

import org.eclipse.cdt.utils.Platform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import org.codechecker.eclipse.plugin.Activator;
import org.codechecker.eclipse.plugin.views.config.filter.FilterConfigurationDialog;
import org.codechecker.eclipse.plugin.views.report.list.ReportListView;
import org.codechecker.eclipse.plugin.views.report.list.action.showas.TreeAwareAction;


import java.net.URL;

public class ShowFilterConfigurationDialog extends TreeAwareAction {

	
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
            listView.setActiveConfiguration(dialog.getCurrentConfiguration());
        }

    }

}
