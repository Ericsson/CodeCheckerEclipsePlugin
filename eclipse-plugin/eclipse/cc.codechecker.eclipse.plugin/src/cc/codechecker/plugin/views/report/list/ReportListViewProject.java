package cc.codechecker.plugin.views.report.list;

import java.util.LinkedList;

import org.eclipse.swt.widgets.Composite;

import cc.codechecker.plugin.config.filter.Filter;
import cc.codechecker.plugin.config.filter.FilterConfiguration;

public class ReportListViewProject extends ReportListView {
	
	public static final String ID = "cc.codechecker.plugin.views.ReportListProject";
	
	public ReportListViewProject() {
		Filter all = new Filter();
		all.setFilepath("*");
		LinkedList<Filter> filter = new LinkedList<>();
		filter.add(all);
		this.activeConfiguration = new FilterConfiguration();
		activeConfiguration.setFilters(filter);
		activeConfiguration.setLinkToCurrentEditorByDefalt(false);
	}

}
