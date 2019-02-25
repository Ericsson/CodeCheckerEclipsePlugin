package org.codechecker.eclipse.plugin.views.report.list.provider.label;

import org.codechecker.eclipse.plugin.views.report.list.ReportListView;

public class LastPartLabelProvider extends BasicViewLabelProvider {

    public LastPartLabelProvider(ReportListView reportListView) {
        super(reportListView);
        // TODO Auto-generated constructor stub
    }

    public String getText(Object obj) {
        String sup = super.getText(obj);
        if (obj instanceof String) {
            String[] parts = sup.split("\\.");
            sup = parts[parts.length - 1];
        }
        return sup;
    }

}
