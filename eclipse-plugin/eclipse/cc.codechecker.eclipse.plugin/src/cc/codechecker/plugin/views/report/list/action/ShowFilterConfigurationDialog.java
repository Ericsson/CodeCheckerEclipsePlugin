package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.views.config.filter.FilterConfigurationDialog;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class ShowFilterConfigurationDialog extends TreeAwareAction {

    public ShowFilterConfigurationDialog(ReportListView listView) {
        super(listView, "Show Filter Configurator", IAction.AS_PUSH_BUTTON);
    }

    @Override
    public void run() {
        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        FilterConfigurationDialog dialog = new FilterConfigurationDialog(activeShell, listView
                .getActiveConfiguration().dup(), listView.getCurrentProject().getName());

        int result = dialog.open();

        if (result == 0) {
            System.out.println("Setting! " + dialog.getCurrentConfiguration().getName());
            listView.setActiveConfiguration(dialog.getCurrentConfiguration());
        }

        System.out.println("RESULT: " + result);
    }

}
