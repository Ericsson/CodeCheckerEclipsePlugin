package cc.codechecker.plugin.views.report.list.action.bugpath;

import java.util.Objects;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewReference;

import cc.codechecker.plugin.views.report.details.BugPathListView;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class BugPathViewSelector extends TreeAwareAction {

    BugPathListView viewRef;

    public BugPathViewSelector(ReportListView listView, BugPathListView vp) {
        super(listView, "View: " + vp.getPartName(), IAction.AS_RADIO_BUTTON);
        this.viewRef = vp;

        if (Objects.equals(vp, listView.getDefaultBugPathView())) {
            setChecked(true);
        }
    }

    @Override
    public void run() {
        listView.setDefaultBugPathView(viewRef);
    }

}
