package cc.codechecker.plugin.views.report.list.action.rerun;

import org.eclipse.jface.action.IAction;

import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class RerunAllAction extends TreeAwareAction {

    public RerunAllAction(ReportListView listView) {
        super(listView, "Rerun everything", IAction.AS_PUSH_BUTTON);
        setEnabled(false);
    }

}
