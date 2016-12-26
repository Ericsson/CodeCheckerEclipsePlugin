package cc.codechecker.plugin.views.report.list;

import org.eclipse.core.resources.IProject;

public class ReportListViewCustom extends ReportListView {

    public static final String ID = "cc.codechecker.plugin.views.ReportListViewCustom";

    public void onEditorChanged(IProject project) {
        super.onEditorChanged(project, "");
    }

}
