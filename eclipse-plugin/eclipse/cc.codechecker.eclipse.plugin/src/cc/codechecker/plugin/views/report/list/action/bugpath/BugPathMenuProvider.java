package cc.codechecker.plugin.views.report.list.action.bugpath;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.views.report.details.BugPathListView;
import cc.codechecker.plugin.views.report.list.ReportListView;

public class BugPathMenuProvider implements IMenuListener {

    private Separator separator;
    private NewBugPathView newBugPathView;
    private ReportListView listView;

    public BugPathMenuProvider(ReportListView listView) {
        separator = new Separator();
        newBugPathView = new NewBugPathView(listView);
        this.listView = listView;
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {

        for (IViewReference vp : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getViewReferences()) {
            if (vp.getId().equals(BugPathListView.ID)) {
                manager.add(new BugPathViewSelector(listView, (BugPathListView) vp.getView(true)));
            }
        }

        manager.add(separator);
        manager.add(newBugPathView);
    }

}
