package cc.codechecker.plugin.views.report.details.action;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.plugin.config.project.CcConfiguration;
import cc.codechecker.plugin.views.report.details.BugPathListView;

public class JumpToBugItem implements IDoubleClickListener {

    private final BugPathListView bugPathListView;

    public JumpToBugItem(BugPathListView bugPathListView) {
        this.bugPathListView = bugPathListView;
    }

    @Override
    public void doubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (selection.isEmpty()) return;
        BugPathItem bpi = (BugPathItem) selection.getFirstElement();

        jumpToBugPosition(bpi);
    }

    public void jumpToBugPosition(BugPathItem bpi) {
        IProject prj = bugPathListView.getProject();
        CcConfiguration config = new CcConfiguration(prj);

        String relName = config.convertFilenameFromServer(bpi.getFile());

        System.out.println("FOLLOW> " + relName);
        IFile fileinfo = prj.getFile(relName);

        if (fileinfo != null && fileinfo.exists()) {
            System.out.println("FOLLOW> fileinfo found");
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(IMarker.LINE_NUMBER, new Integer((int) bpi.getStartPosition().getLine()));
            map.put(IDE.EDITOR_ID_ATTR, "org.eclipse.ui.DefaultTextEditor");
            IMarker marker;
            IEditorPart ieditorpart = page.getActiveEditor();
            IEditorInput ieditorinput = ieditorpart.getEditorInput();
            String ieditorinputname = ieditorinput.getName();
            System.out.println("FOLLOW> IEditorInput Name: " + ieditorinputname);
            try {
                marker = fileinfo.createMarker(IMarker.TEXT);
                marker.setAttributes(map);
                //IDE.openEditor(page, marker);
                IDE.gotoMarker(ieditorpart, marker);
                System.out.println("FOLLOW> opened editor");
                marker.delete();
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

}
