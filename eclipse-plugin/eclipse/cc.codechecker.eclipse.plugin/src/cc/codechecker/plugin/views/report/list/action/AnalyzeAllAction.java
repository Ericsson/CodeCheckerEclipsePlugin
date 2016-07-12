package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.config.project.CcConfiguration;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class AnalyzeAllAction extends TreeAwareAction {

    public AnalyzeAllAction(ReportListView listView) {
        super(listView, "Refresh hits", IAction.AS_PUSH_BUTTON);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor
                (ISharedImages.IMG_TOOL_REDO));


    }

    @Override
    public void run() {
        CodeCheckerContext.getInstance().runAnalyzeJob(listView);
    }

}
