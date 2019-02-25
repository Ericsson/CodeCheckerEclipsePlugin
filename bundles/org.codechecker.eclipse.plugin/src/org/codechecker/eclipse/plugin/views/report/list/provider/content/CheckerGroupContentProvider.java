package org.codechecker.eclipse.plugin.views.report.list.provider.content;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.codechecker.eclipse.plugin.report.BugPathItem;
import org.codechecker.eclipse.plugin.report.ProblemInfo;
import org.codechecker.eclipse.plugin.report.ReportInfo;
import org.codechecker.eclipse.plugin.report.SearchList;
import org.codechecker.eclipse.plugin.views.report.list.ReportListView;

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
        	ReportInfo ri = (ReportInfo) parentElement;
        	Optional<ProblemInfo> bp = ri.getChildren();
        	if (bp != null && bp.isPresent()) {
                ArrayList<BugPathItem> result = new ArrayList<>(bp.get().getItems());
                Iterables.removeIf(result, new Predicate<BugPathItem>() {
                    @Override
                    public boolean apply(BugPathItem pi) {
                        return "".equals(pi.getMessage());
                    }
                });
                return result.toArray();
            }
        }

        return ArrayUtils.toArray();
    }

    @Override
    public boolean hasChildren(Object element) {
        if(element instanceof ReportInfo || element instanceof SearchList || element instanceof String) {
        	return true;
        }
        return false;
    }

}
