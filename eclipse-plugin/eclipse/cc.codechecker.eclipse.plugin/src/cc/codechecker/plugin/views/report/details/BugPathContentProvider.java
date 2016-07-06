package cc.codechecker.plugin.views.report.details;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.bug.path.ProblemInfo;

import java.util.ArrayList;

public class BugPathContentProvider implements IStructuredContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Optional) {
            @SuppressWarnings("unchecked") Optional<ProblemInfo> bp = (Optional<ProblemInfo>)
                    inputElement;
            if (bp.isPresent()) {
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

}
