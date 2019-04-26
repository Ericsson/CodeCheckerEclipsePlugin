package org.codechecker.eclipse.plugin.views.report.list.action.showas;

import org.eclipse.jface.action.Action;

import org.codechecker.eclipse.plugin.views.report.list.ReportListView;

public class TreeAwareAction extends Action {

    protected final ReportListView listView;

    public TreeAwareAction(ReportListView listView, String name, int type) {
        super(name, type);
        this.listView = listView;
    }

    public TreeAwareAction(ReportListView listView, String name, int type, boolean checked) {
        super(name, type);
        this.listView = listView;
        setChecked(checked);
    }

}
