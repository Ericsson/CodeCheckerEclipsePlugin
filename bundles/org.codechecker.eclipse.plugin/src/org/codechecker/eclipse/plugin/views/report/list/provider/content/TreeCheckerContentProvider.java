package org.codechecker.eclipse.plugin.views.report.list.provider.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.codechecker.eclipse.plugin.report.BugPathItem;
import org.codechecker.eclipse.plugin.report.ProblemInfo;
import org.codechecker.eclipse.plugin.report.ReportInfo;
import org.codechecker.eclipse.plugin.report.SearchList;
import org.codechecker.eclipse.plugin.views.report.list.ReportListView;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class TreeCheckerContentProvider implements ITreeContentProvider {

    private final ReportListView reportListView;

    public TreeCheckerContentProvider(ReportListView reportListView) {
        this.reportListView = reportListView;
    }

    @Override
    public Object getParent(Object child) {
        if (child instanceof String) {
            String checker = (String) child;
            for (String s : this.reportListView.getReportList().get().getCheckers()) {
                if (checker.startsWith(s + ".")) {
                    return s;
                }
            }
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
            Set<String> tops = new HashSet<>();
            for (String s : this.reportListView.getReportList().get().getCheckers()) {
                tops.add(s.split("\\.")[0]);
            }
            return tops.toArray();
        }

        if (parentElement instanceof String) {

            String parent = (String) parentElement;
            String[] splittedParent = parent.split("\\.");
            int parentLevel = splittedParent.length;
            Set<Object> tops = new HashSet<>();
            for (String s : this.reportListView.getReportList().get().getCheckers()) {
                String[] splitted = s.split("\\.");
                if (splitted.length > parentLevel && splitted[parentLevel - 1].equals(splittedParent[parentLevel - 1])
                        && !s.equals(parent)) {
                    tops.add(parent + "." + splitted[parentLevel]);
                }
            }

            tops.addAll(this.reportListView.getReportList().get().getReportsFor((String)
                    parentElement));

            return tops.toArray();
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
            return ArrayUtils.toArray();
        }
        
        if(parentElement instanceof BugPathItem) {
        	//no child!
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
