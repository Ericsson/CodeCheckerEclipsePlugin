package org.codechecker.eclipse.plugin.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.views.console.ConsoleFactory;


import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class AddProjectNature extends AbstractHandler {
	
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO Auto-generated method stub    	

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IStructuredSelection selection = (IStructuredSelection) window.getSelectionService()
                    .getSelection();
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IAdaptable) {
                IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject
                        .class);
                if (project == null) {
                	Logger.log(IStatus.INFO, "Not a project.");
                    return null;
                }
                IPath path = project.getFullPath();
                Logger.log(IStatus.INFO, "" + path);

                try {
                    if (project.hasNature(CodeCheckerNature.NATURE_ID)) {                    	
                        return null;
                    }

                    IProjectDescription description = project.getDescription();
                    String[] prevNatures = description.getNatureIds();
                    String[] newNatures = new String[prevNatures.length + 1];
                    System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
                    newNatures[prevNatures.length] = CodeCheckerNature.NATURE_ID;
                    description.setNatureIds(newNatures);

                    IProgressMonitor monitor = null;
                    project.setDescription(description, monitor);
                    ConsoleFactory.consoleWrite(project.getName() + ": Sucessfully added CodeChecker Nature");
                    Logger.log(IStatus.INFO, "Project nature added!");

                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                	Logger.log(IStatus.ERROR,  e.toString());
                	Logger.log(IStatus.INFO, e.getStackTrace().toString());
                }

            }
        }

        return null;
    }

}
