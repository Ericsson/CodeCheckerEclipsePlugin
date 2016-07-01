package cc.codechecker.plugin.views.report.list.provider.content;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.plugin.views.report.list.ReportListView;

public class CheckerGroupContentProvider implements ITreeContentProvider {

    private final ReportListView reportListView;

    public CheckerGroupContentProvider(ReportListView reportListView) {
        this.reportListView = reportListView;
    }

    @Override
    public Object getParent(Object child) {
        if (child instanceof String) {
            return this.reportListView.getReportList().orNull();
        }

        if (child instanceof ReportInfo) {
            return ((ReportInfo) child).getCheckerId();
        }

        return null;
    }

    @Override
    public void dispose() {
        // nop
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nop
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement) {

        if (parentElement instanceof SearchList) {
            return this.reportListView.getReportList().get().getCheckers().toArray();
        }

        if (parentElement instanceof String) {
            return this.reportListView.getReportList().get().getReportsFor((String)
                    parentElement).toArray();
        }

        if (parentElement instanceof ReportInfo) {
            // no children
        }

        return ArrayUtils.toArray();
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

}
