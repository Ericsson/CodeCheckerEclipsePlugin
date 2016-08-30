package cc.codechecker.plugin.views.report.list;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;

import cc.codechecker.plugin.config.filter.Filter;
import cc.codechecker.plugin.config.filter.FilterConfiguration;

public class ReportListViewProject extends ReportListView {

    public static final String ID = "cc.codechecker.plugin.views.ReportListProject";

    public void onEditorChanged(IProject project) {
        super.onEditorChanged(project, "");
    }

}
