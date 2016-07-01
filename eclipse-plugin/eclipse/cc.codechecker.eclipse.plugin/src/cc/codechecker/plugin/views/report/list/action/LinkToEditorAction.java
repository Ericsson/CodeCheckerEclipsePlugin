package cc.codechecker.plugin.views.report.list.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class LinkToEditorAction extends TreeAwareAction {

    public LinkToEditorAction(ReportListView listView, boolean checked) {
        super(listView, "Link to Editor", IAction.AS_CHECK_BOX, checked);
        setToolTipText("Link to Editor");
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor
                (ISharedImages.IMG_ELCL_SYNCED));
    }

}
