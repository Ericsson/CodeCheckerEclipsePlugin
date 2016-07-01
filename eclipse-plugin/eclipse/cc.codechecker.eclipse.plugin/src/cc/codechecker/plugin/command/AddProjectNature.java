package cc.codechecker.plugin.command;

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

import cc.codechecker.plugin.CodeCheckerNature;

public class AddProjectNature extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO Auto-generated method stub
        System.out.println("Adding project nature...");

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IStructuredSelection selection = (IStructuredSelection) window.getSelectionService()
                    .getSelection();
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IAdaptable) {
                IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject
                        .class);
                if (project == null) {
                    System.out.println("Not a project :(");
                    return null;
                }
                IPath path = project.getFullPath();
                System.out.println(path);

                try {
                    if (project.hasNature(CodeCheckerNature.NATURE_ID)) {
                        System.out.println("Project already has nature");
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

                    System.out.println("Project nature added!");

                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        return null;
    }

}
