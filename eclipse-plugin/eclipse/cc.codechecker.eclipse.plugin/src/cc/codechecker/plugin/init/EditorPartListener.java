package cc.codechecker.plugin.init;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class EditorPartListener implements IPartListener {

    @Override
    public void partActivated(IWorkbenchPart partRef) {
        if (!(partRef instanceof IEditorPart)) {
            return;
        }
        Logger.log(IStatus.INFO, "Editor changed : " + partRef.getClass().getName());
        CodeCheckerContext.getInstance().refreshChangeEditorPart((IEditorPart) partRef);
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partOpened(IWorkbenchPart part) {

    }

}
