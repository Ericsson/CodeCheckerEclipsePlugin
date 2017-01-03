package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.views.report.list.ReportListViewCustom;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;
import cc.codechecker.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class NewInstanceAction extends TreeAwareAction {


    public NewInstanceAction(ReportListViewCustom listView) {
        super(listView, "Create new ReportList", IAction.AS_PUSH_BUTTON);
        setToolTipText("Create new ReportList");
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor
                (ISharedImages.IMG_OBJ_ADD));
    }

    @Override
    public void run() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        try {
            String secondaryId = "ReportList" + Math.ceil(Math.random() * 100);
            page.showView(ReportListViewCustom.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
            CodeCheckerContext.getInstance().refreshAddCustomReportListView(secondaryId);
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            Logger.log(IStatus.ERROR, " " + e);
            Logger.log(IStatus.INFO, " " + e.getStackTrace());
        }
    }
}
