package cc.codechecker.plugin.views.report.details.action;

import org.eclipse.jface.action.Action;

import cc.codechecker.plugin.views.report.details.BugPathListView;

public class BugPathAwareAction extends Action {

    protected final BugPathListView bugPathView;

    public BugPathAwareAction(BugPathListView bugPathView, String title, int style) {
        super(title, style);
        this.bugPathView = bugPathView;
    }


}
