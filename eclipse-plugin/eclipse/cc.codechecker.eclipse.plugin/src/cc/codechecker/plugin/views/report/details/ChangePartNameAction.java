package cc.codechecker.plugin.views.report.details;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.views.report.details.action.BugPathAwareAction;

public class ChangePartNameAction extends BugPathAwareAction {

    public ChangePartNameAction(BugPathListView bugPathView) {
        super(bugPathView, "Change view name", IAction.AS_PUSH_BUTTON);
    }

    @Override
    public void run() {
        Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        InputDialog d = new InputDialog(activeShell, "Change BugPath view title", "New title",
                bugPathView.getPartName(), new IInputValidator() {

            @Override
            public String isValid(String newText) {
                if (newText.length() == 0) {
                    return "Title can't be empty!";
                }
                return null;
            }
        });

        if (d.open() == 0) {
            bugPathView.setName(d.getValue());
        }
    }

}
