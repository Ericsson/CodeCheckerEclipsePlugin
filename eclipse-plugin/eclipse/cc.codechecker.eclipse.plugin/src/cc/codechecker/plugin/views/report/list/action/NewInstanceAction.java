package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.views.report.list.ReportListViewCustom;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

public class NewInstanceAction extends TreeAwareAction {

    //Logger
    private final static Logger logger = LogManager.getLogger(NewInstanceAction.class.getName());

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
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }
}
