package cc.codechecker.plugin.views.report.details;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.*;

import com.google.common.base.Optional;

import cc.codechecker.api.action.bug.path.ProblemInfo;
import cc.codechecker.plugin.views.report.details.action.JumpToBugItem;

public class BugPathListView extends ViewPart {

    public static final String ID = "cc.codechecker.plugin.views.BugPathList";

    ListViewer viewer;

    Optional<ProblemInfo> bugPath;

    private IProject project;

    @Override
    public void createPartControl(Composite parent) {
        viewer = new ListViewer(parent);
        viewer.setContentProvider(new BugPathContentProvider());
        viewer.setLabelProvider(new BugPathLabelProvider());

        viewer.addDoubleClickListener(new JumpToBugItem(this));

        contributeToActionBars();
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        /*manager.add(new Action("Back"){});
        manager.add(new Action("Next"){});
		manager.add(new Separator());
		manager.add(new Action("Display additional information"){});
		manager.add(new Separator());
		manager.add(new ChangePartNameAction(this));
		manager.add(new Action("Mark as default target", IAction.AS_CHECK_BOX){});
		manager.add(new Action("Duplicate"){});
		manager.add(new Separator());
		manager.add(new Action("Refresh"){});
		manager.add(new Action("Rerun"){});*/
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public void changeModel(IProject project, Optional<ProblemInfo> optional) {
        this.project = project;
        this.bugPath = optional;
        System.out.println(optional);
        viewer.setInput(optional);
        viewer.refresh();
    }

    public IProject getProject() {
        return project;
    }

    public void setName(String value) {
        setPartName(value);
    }

}
