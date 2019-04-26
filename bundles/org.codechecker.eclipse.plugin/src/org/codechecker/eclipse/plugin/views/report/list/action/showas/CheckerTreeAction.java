package org.codechecker.eclipse.plugin.views.report.list.action.showas;

import org.eclipse.jface.action.IAction;

import org.codechecker.eclipse.plugin.views.report.list.ReportListView;
import org.codechecker.eclipse.plugin.views.report.list.provider.content.TreeCheckerContentProvider;
import org.codechecker.eclipse.plugin.views.report.list.provider.label.LastPartLabelProvider;

public class CheckerTreeAction extends TreeAwareAction {

    public CheckerTreeAction(ReportListView listView, boolean checked) {
        super(listView, "Group by hierarchical checkers", IAction.AS_RADIO_BUTTON, checked);
    }

    @Override
    public void run() {
        if (isChecked()) {
            listView.setProviders(new LastPartLabelProvider(listView), new
                    TreeCheckerContentProvider(listView));
        }
    }
}
