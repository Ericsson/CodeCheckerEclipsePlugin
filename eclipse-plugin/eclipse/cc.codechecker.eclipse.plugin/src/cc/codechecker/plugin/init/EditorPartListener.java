package cc.codechecker.plugin.init;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.config.project.CcProperties;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class EditorPartListener implements IPartListener {

	//Logger
	private static final Logger logger = LogManager.getLogger(EditorPartListener.class);
	
    @Override
    public void partActivated(IWorkbenchPart partRef) {
        if (!(partRef instanceof IEditorPart)) {
            return;
        }
        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Editor changed : " + partRef.getClass().getName());
        CodeCheckerContext.getInstance().setActiveEditorPart((IEditorPart) partRef, false);
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
