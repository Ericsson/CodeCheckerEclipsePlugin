package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.views.config.filter.FilterConfigurationDialog;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

public class ShowFilterConfigurationDialog extends TreeAwareAction {

	private static final Logger logger = LogManager.getLogger(ShowFilterConfigurationDialog.class.getName());
	
    public ShowFilterConfigurationDialog(ReportListView listView) {
        super(listView, "Show Filter Configurators", IAction.AS_PUSH_BUTTON);
        setToolTipText("Show Filter Configurators");
        setImageDescriptor(JavaPluginImages.DESC_DLCL_FILTER);
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
