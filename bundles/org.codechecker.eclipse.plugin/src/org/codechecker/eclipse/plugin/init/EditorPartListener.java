package org.codechecker.eclipse.plugin.init;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.Logger;
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
