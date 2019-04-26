package org.codechecker.eclipse.plugin.views.report.list;

import org.eclipse.core.resources.IProject;

public class ReportListViewProject extends ReportListView {

    public static final String ID = "org.codechecker.eclipse.plugin.views.ReportListProject";

    public void onEditorChanged(IProject project) {
        super.onEditorChanged(project, "");
    }

}
