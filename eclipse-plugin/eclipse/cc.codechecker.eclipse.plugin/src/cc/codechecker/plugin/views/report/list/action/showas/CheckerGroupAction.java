package cc.codechecker.plugin.views.report.list.action.showas;

import org.eclipse.jface.action.IAction;

import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.provider.content.CheckerGroupContentProvider;
import cc.codechecker.plugin.views.report.list.provider.label.BasicViewLabelProvider;

public class CheckerGroupAction extends TreeAwareAction {

    public CheckerGroupAction(ReportListView listView, boolean checked) {
        super(listView, "Group by checkers", IAction.AS_RADIO_BUTTON, checked);
    }

    @Override
    public void run() {
        if (isChecked()) {
            listView.setProviders(new BasicViewLabelProvider(listView), new
                    CheckerGroupContentProvider(listView));
        }
    }
}
