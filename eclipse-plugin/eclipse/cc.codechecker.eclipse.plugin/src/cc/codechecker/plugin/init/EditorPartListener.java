package cc.codechecker.plugin.init;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import cc.codechecker.plugin.config.CodeCheckerContext;

public class EditorPartListener implements IPartListener {

    @Override
    public void partActivated(IWorkbenchPart partRef) {
        if (!(partRef instanceof IEditorPart)) {
            System.out.println("Not an editor: " + partRef.getClass().getName());
            return;
        }
        System.out.println("Editor changed");
        CodeCheckerContext.getInstance().setActiveEditorPart((IEditorPart) partRef);
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
