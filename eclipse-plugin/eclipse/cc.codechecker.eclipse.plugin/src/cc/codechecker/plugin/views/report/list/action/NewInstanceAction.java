package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Instant;

import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class NewInstanceAction extends TreeAwareAction {

    public NewInstanceAction(ReportListView listView) {
        super(listView, "Create another view", IAction.AS_PUSH_BUTTON);
    }

    @Override
    public void run() {
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();

        try {
            page.showView(ReportListView.ID, "Test" + Math.ceil(Math.random() * 100),
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
