package cc.codechecker.plugin.views.report.list.action.bugpath;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.views.report.details.BugPathListView;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class NewBugPathView extends TreeAwareAction {

    public NewBugPathView(ReportListView listView) {
        super(listView, "New view", IAction.AS_PUSH_BUTTON);
    }

    @Override
    public void run() {
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();

        try {
            page.showView(BugPathListView.ID, "Test" + Math.ceil(Math.random() * 100),
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
