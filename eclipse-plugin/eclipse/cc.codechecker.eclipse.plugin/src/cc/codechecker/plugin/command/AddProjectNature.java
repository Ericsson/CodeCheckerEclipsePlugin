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
import cc.codechecker.plugin.views.console.ConsoleFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class AddProjectNature extends AbstractHandler {
	
	//Logger
	private static final Logger logger = LogManager.getLogger(AddProjectNature.class);
	
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO Auto-generated method stub
    	logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Adding project nature.");

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IStructuredSelection selection = (IStructuredSelection) window.getSelectionService()
                    .getSelection();
            Object firstElement = selection.getFirstElement();
            if (firstElement instanceof IAdaptable) {
                IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject
                        .class);
                if (project == null) {
                	logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Not a project.");
                    return null;
                }
                IPath path = project.getFullPath();
                logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + path);

                try {
                    if (project.hasNature(CodeCheckerNature.NATURE_ID)) {
                    	logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Project already has nature.");
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
                    logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Project nature added!");

                } catch (CoreException e) {
                    // TODO Auto-generated catch block
                	logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
                	logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
                }

            }
        }

        return null;
    }

}
