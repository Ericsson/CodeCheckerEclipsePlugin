package org.codechecker.eclipse.plugin.init;

import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

public class ProjectExplorerSelectionListener implements ISelectionListener {

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof IAdaptable) {
                IResource resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
                if (resource != null) {
                    final IProject project = resource.getProject();
                    if (project != null)
                        CodeCheckerContext.getInstance().refreshChangeProject(project);

                }
            }
        }
    }

}
