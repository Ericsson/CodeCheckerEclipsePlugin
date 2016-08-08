package cc.codechecker.plugin.init;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.api.runtime.CodecheckerServerThread;
import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.config.CodeCheckerContext;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class StartupJob extends Job {

    //Logger
    private static final Logger logger = LogManager.getLogger(StartupJob.class);	

    EditorPartListener partListener;
    ProjectExplorerSelectionListener projectexplorerselectionlistener;

    public StartupJob() {
        super("CodeChecker Startup Job");
        partListener = new EditorPartListener();
        projectexplorerselectionlistener = new ProjectExplorerSelectionListener();
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (PlatformUI.isWorkbenchRunning()) {
            runInUIThread(monitor);
        } else {
            schedule(1000);
        }
        return Status.OK_STATUS;
    }

    public IStatus runInUIThread(IProgressMonitor monitor) {

        IWorkbench wb = PlatformUI.getWorkbench();

        try { // TODO: find a better solution...
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }

        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                switch (event.getType()) {
                case IResourceChangeEvent.POST_BUILD: {
                    logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Project was built!");
                    try {
                        final HashSet<IProject> changedProjects = new HashSet<>();
                        if(event.getBuildKind() != IncrementalProjectBuilder.CLEAN_BUILD) {
                            event.getDelta().accept(new IResourceDeltaVisitor() {
                                public boolean visit(final IResourceDelta delta) throws
                                CoreException {
                                    IResource resource = delta.getResource();
                                    changedProjects.add(resource.getProject());
                                    return true;
                                }
                            });
                            for (IProject p : changedProjects) {
                                onProjectBuilt(p);
                            }
                        }
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
                        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
                    }
                    break;
                }
                }
                try {
                    event.getDelta().accept(new IResourceDeltaVisitor() {
                        public boolean visit(final IResourceDelta delta) throws CoreException {
                            IResource resource = delta.getResource();
                            if (((resource.getType() & IResource.PROJECT) != 0) && resource
                                    .getProject().isOpen() && delta.getKind() == IResourceDelta
                                    .CHANGED && ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {

                                IProject project = (IProject) resource;
                                projectOpened(project);
                            }
                            return true;
                        }
                    });
                } catch (CoreException e) {
                    logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
                    logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
                }
            }

        }, IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE | IResourceDelta
                .OPEN);

        // check all open projects

        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            projectOpened(project);
        }

        // check all open windows
        for (IWorkbenchWindow win : wb.getWorkbenchWindows()) {
            addListenerToWorkbenchWindow(win);
        }


        return Status.OK_STATUS;
    }

    private void onProjectBuilt(IProject project) {
        if (project == null) return;
        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Project changed event!");
        try {
            if (!project.hasNature(CodeCheckerNature.NATURE_ID)) {
                return;
            }
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }

        CodecheckerServerThread server = CodeCheckerContext.getInstance().getServerObject(project);
        if (project.isOpen()) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Project built event! - good natured!");
            if (!server.isRunning()) server.start(); // ensure started!
            server.recheck();
        }
    }

    private void projectOpened(IProject project) {
        if (project == null) return;
        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Project changed event!");
        try {
            if (!project.hasNature(CodeCheckerNature.NATURE_ID)) {
                return;
            }
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> Good Natured!");
        try {
            CodecheckerServerThread server = CodeCheckerContext.getInstance().getServerObject
                    (project);
            if (project.isOpen()) {
                if (!server.isRunning()) server.start(); // ensure started!
            } else {
                if (server.isRunning()) server.stop();
            }
        } catch (Exception e) {
        }

    }

    private void addListenerToWorkbenchWindow(IWorkbenchWindow win) {
        try {
            ISelectionService ss = win.getSelectionService();
            ss.addPostSelectionListener(IPageLayout.ID_PROJECT_EXPLORER, projectexplorerselectionlistener);
            win.getActivePage().addPartListener(partListener);
        } catch (Exception e) {
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> " + e);
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> " + e.getStackTrace());
        }
    }

}
