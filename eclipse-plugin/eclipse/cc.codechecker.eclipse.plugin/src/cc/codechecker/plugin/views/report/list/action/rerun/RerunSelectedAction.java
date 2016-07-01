package cc.codechecker.plugin.views.report.list.action.rerun;

import org.eclipse.jface.action.IAction;

import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.action.showas.TreeAwareAction;

public class RerunSelectedAction extends TreeAwareAction {

    public RerunSelectedAction(ReportListView listView) {
        super(listView, "Rerun selected reports", IAction.AS_PUSH_BUTTON);
        setEnabled(false);
    }

}
