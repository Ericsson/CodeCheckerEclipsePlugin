package org.codechecker.eclipse.plugin.views.report.list;

import java.util.LinkedList;

import org.eclipse.core.resources.IProject;

import org.codechecker.eclipse.plugin.config.filter.Filter;
import org.codechecker.eclipse.plugin.config.filter.FilterConfiguration;

public class ReportListViewCustom extends ReportListView {

    public static final String ID = "org.codechecker.eclipse.plugin.views.ReportListViewCustom";

    public void onEditorChanged(IProject project) {
        super.onEditorChanged(project, "");
    }

}
