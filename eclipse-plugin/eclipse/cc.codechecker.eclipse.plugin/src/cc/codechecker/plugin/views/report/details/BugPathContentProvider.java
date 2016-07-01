package cc.codechecker.plugin.views.report.details;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.base.Optional;

import cc.codechecker.api.action.bug.path.ProblemInfo;

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
                return bp.get().getItems().toArray();
            }
        }
        return ArrayUtils.toArray();
    }

}
