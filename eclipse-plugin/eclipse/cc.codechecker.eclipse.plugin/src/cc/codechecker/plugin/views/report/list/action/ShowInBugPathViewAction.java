package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.views.report.details.BugPathListView;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class ShowInBugPathViewAction extends TreeAwareAction implements ISelectionChangedListener {

    private BugPathListView targetView;

    public ShowInBugPathViewAction(ReportListView listView, BugPathListView targetView) {
        super(listView, "Show in Bug Path view", IAction.AS_PUSH_BUTTON);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor
                (ISharedImages.IMG_TOOL_FORWARD));
        listView.getViewer().addSelectionChangedListener(this);
        this.targetView = targetView;
        if (targetView == null) {
            lookForDefaultView();
        }
    }

    private void lookForDefaultView() {
        for (IViewReference vp : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getViewReferences()) {
            if (vp.getId().equals(BugPathListView.ID)) {
                targetView = ((BugPathListView) vp.getView(true));
                break;
            }
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        ITreeSelection selection = ((ITreeSelection) listView.getViewer().getSelection());

        if (targetView == null) { // look for defailt view
            lookForDefaultView();
        }

        boolean shouldBeEnabled = (selection.size() == 1 && selection.getFirstElement()
                instanceof ReportInfo) && targetView != null;
        if (shouldBeEnabled != super.isEnabled()) {
            setEnabled(shouldBeEnabled);
        }
    }

    public BugPathListView getTargetView() {
        return targetView;
    }

    public void setTargetView(BugPathListView viewRef) {
        this.targetView = viewRef;
    }

    @Override
    public void run() {
        ITreeSelection selection = ((ITreeSelection) listView.getViewer().getSelection());

        ReportInfo ri = (ReportInfo) selection.getFirstElement();

        // Preventing nullptrexception caused by doubleclicking on a parent node or its arrow icon.
        if (ri == null) {
            return;
        }

        CodeCheckerContext.getInstance().displayBugPath(targetView, ri,
                listView.getReportList().get().getBugPathJobFor(ri, 1, Optional.of((new Instant()).plus(120))),
                listView.getCurrentProject());
    }

}
