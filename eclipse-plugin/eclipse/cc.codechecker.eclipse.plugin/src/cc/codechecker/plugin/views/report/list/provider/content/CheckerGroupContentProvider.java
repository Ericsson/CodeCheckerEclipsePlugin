package cc.codechecker.plugin.views.report.list.provider.content;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.joda.time.Instant;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.bug.path.ProblemInfo;
import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.plugin.config.CodeCheckerContext;
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
        	ReportInfo ri = (ReportInfo) parentElement;
        	@SuppressWarnings("unchecked") Optional<ProblemInfo> bp = ri.getBugPath();
        	if(bp == null) {
	        	CodeCheckerContext.getInstance().displayBugPath( ri,
	        			reportListView.getReportList().get().getBugPathJobFor(ri, 1, Optional.of((new Instant()).plus(120))),
	        			reportListView.getCurrentProject());
	        	bp = ri.getBugPath();
        	}
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
        return getChildren(element).length > 0;
    }

}
