package org.codechecker.eclipse.plugin.views.report.list.action.rerun;

import org.eclipse.jface.action.IAction;

import org.codechecker.eclipse.plugin.views.report.list.ReportListView;
import org.codechecker.eclipse.plugin.views.report.list.action.showas.TreeAwareAction;

public class RefreshAction extends TreeAwareAction {

    public RefreshAction(ReportListView listView) {
        super(listView, "Refresh reports", IAction.AS_PUSH_BUTTON);
    }

}
