package org.codechecker.eclipse.plugin.init;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;

public class ProjectExplorerSelectionListener implements ISelectionListener {

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof IAdaptable) {
                IResource resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
                if (resource != null) {
                    final IProject project = resource.getProject();
                    try {
                        if (project != null && project.hasNature(CodeCheckerNature.NATURE_ID))
                            CodeCheckerContext.getInstance().refreshChangeProject(project);
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
