package org.codechecker.eclipse.plugin.init;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.codechecker.eclipse.plugin.CodeCheckerNature;
import org.codechecker.eclipse.plugin.ExternalLogger;
import org.codechecker.eclipse.plugin.Logger;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.config.global.CcGlobalConfiguration;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.codechecker.eclipse.plugin.report.job.AnalyzeJob;
import org.codechecker.eclipse.plugin.report.job.JobDoneChangeListener;
import org.codechecker.eclipse.plugin.report.job.PlistParseJob;
import org.codechecker.eclipse.plugin.runtime.SLogger;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

/**
 * This eclipse job is responsible for registering the Build listener.
 *
 */
public class StartupJob extends Job {

    private static final int WAIT_TIME = 2000;  // in millisecond

    EditorPartListener partListener;
    ProjectExplorerSelectionListener projectExplorerSelectionlistener;

    /**
     * Job constructor.
     */
    public StartupJob() {
        super("CodeChecker Startup Job");
        partListener = new EditorPartListener();
        projectExplorerSelectionlistener = new ProjectExplorerSelectionListener();
        SLogger.setLogger(new ExternalLogger()); //setting up the eclipse logger for the external service
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {        
        if (PlatformUI.isWorkbenchRunning()) {            
            runInUIThread(monitor);
        } else {
            schedule();
        }
        return Status.OK_STATUS;
    }

    /**
     * Runs this job on UI thread.
     * @param monitor ProgressBar
     * @return Return Status.
     */
    public IStatus runInUIThread(IProgressMonitor monitor) {
        CcGlobalConfiguration.getInstance();
        
        try { // TODO: find a better solution...
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Logger.log(IStatus.ERROR, e.getMessage());
            Logger.log(IStatus.INFO, Logger.getStackTrace(e));
        }

        Logger.log(IStatus.INFO, "adding addResourceChangeListener ");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(),
                IResourceChangeEvent.POST_BUILD |
                IResourceDelta.OPEN | IResourceChangeEvent.PRE_DELETE);
        
        ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        commandService.addExecutionListener(new ISaveCommandExecutionListener());

        // check all open projects
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            projectOpened(project);
        }
        Logger.log(IStatus.INFO, "CodeChecker reports had been parsed.");

        // check all open windows
        IWorkbench wb = PlatformUI.getWorkbench();
        for (IWorkbenchWindow win : wb.getWorkbenchWindows()) {
            addListenerToWorkbenchWindow(win);
        }
        return Status.OK_STATUS;
    }

    /**
     * 
     * @param project The project that got opened.
     */
    private void projectOpened(IProject project) {
        // We should return if the project is inaccessible.
        if (project == null || !project.isAccessible())
            return;
        try {
            // If CodecheCker nature is not set or the project is non-CDT we can't parse anything.
            if (!project.hasNature(CodeCheckerNature.NATURE_ID) || 
                    CoreModel.getDefault().getProjectDescription(project, true)==null) {
                return;
            }
        } catch (CoreException e) {
            Logger.log(IStatus.ERROR, e.getMessage());
            Logger.log(IStatus.INFO, Logger.getStackTrace(e));
        }
        Logger.log(IStatus.INFO, "CodeChecker nature found!");
        
        CodeCheckerProject cCProject = new CodeCheckerProject(project);
        CodeCheckerContext.getInstance().addCodeCheckerProject(cCProject);

        PlistParseJob plistParseJob = new PlistParseJob(project);
        plistParseJob.addJobChangeListener(new JobDoneChangeListener() {
            @Override
            public void done(IJobChangeEvent event) {
                CodeCheckerContext.getInstance().refresAsync(project);
            }
        });
        plistParseJob.schedule();
    }

    /**
     * The added listener will be a PostSelectionListener.
     * @param win The {@link IWorkbenchWindow} that gets listened.
     */
    private void addListenerToWorkbenchWindow(IWorkbenchWindow win) {
        ISelectionService ss = win.getSelectionService();
        ss.addPostSelectionListener(IPageLayout.ID_PROJECT_EXPLORER, projectExplorerSelectionlistener);
        win.getActivePage().addPartListener(partListener);
    }

    /**
     * Inner class for implementing {@link IResourceChangeListener}.
     *
     */
    private final class ResourceChangeListener implements IResourceChangeListener {
        @Override
        public void resourceChanged(final IResourceChangeEvent event) {
            Logger.log(IStatus.INFO, Integer.toString(event.getType()));
            switch (event.getType()) {
                // before delete unregister project.
                case IResourceChangeEvent.PRE_DELETE: {
                    //In PRE_DELETE the resource will always be a project according to the docs.
                    IProject preDeleteProj = event.getResource().getProject();
                    Logger.log(IStatus.INFO, "Project " + preDeleteProj.getName() + " is about to be deleted.");
                    try {
                        if (preDeleteProj != null && preDeleteProj.hasNature(CodeCheckerNature.NATURE_ID)) {
                            CodeCheckerContext.getInstance().removeCcProject(preDeleteProj).cleanUp();
                        }
                    } catch (CoreException e) {
                        Logger.log(IStatus.ERROR, "Stopped managing " + preDeleteProj.getName() + " in context of"
                                + "CodeChecker.");
                        e.printStackTrace();
                    }

                    break;
                }
                case IResourceChangeEvent.PRE_BUILD: {
                    Logger.log(BUILD, "PreBuild");
                    break;
                }
                case IResourceChangeEvent.POST_BUILD: {
                    try {
                        final Set<IProject> builtProjects = new HashSet<>();
                        final Set<IProject> cleanedProjects = new HashSet<>();
                        event.getDelta().accept(new IResourceDeltaVisitor() {
                            public boolean visit(final IResourceDelta delta) throws CoreException {
                                IProject project = delta.getResource().getProject();
                                if (project != null && project.hasNature(CodeCheckerNature.NATURE_ID)) {
                                    if (event.getBuildKind() == IncrementalProjectBuilder.FULL_BUILD
                                            || event.getBuildKind() == IncrementalProjectBuilder.INCREMENTAL_BUILD) {
                                        builtProjects.add(project);
                                    } else if (event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
                                        Logger.log(IStatus.INFO, "Clean was called");
                                        cleanedProjects.add(project);
                                    }
                                }
                                return true;
                            }
                        });
                        for (IProject p : builtProjects) {
                            Logger.log(IStatus.INFO, "build callback was called");
                            onProjectBuilt(p);
                        }
                        for (IProject p : cleanedProjects) {
                            Logger.log(IStatus.INFO, "clean callback was called");
                            onProjectCleaned(p);
                        }
                    } catch (CoreException e) {
                        // The project was deleted recently. There could be resource events on deleted
                        // projects.
                        Logger.log(IStatus.ERROR, e.getMessage());
                        // Logger.log(IStatus.INFO, Logger.getStackTrace(e));
                    }
                    break;
                }
                default:
                    break;
            }
            if (event == null || event.getDelta() == null)
                return;
            try {
                event.getDelta().accept(new IResourceDeltaVisitor() {
                    public boolean visit(final IResourceDelta delta) throws CoreException {
                        IResource resource = delta.getResource();
                        if (((resource.getType() & IResource.PROJECT) != 0) && resource.getProject().isOpen()
                                && delta.getKind() == IResourceDelta.CHANGED
                                && ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {
                            Logger.log(IStatus.INFO,
                                    "Project visit called for project:" + resource.getProject().getName());
                            IProject project = (IProject) resource;
                            projectOpened(project);
                        }
                        return true;
                    }
                });
            } catch (CoreException e) {
                Logger.log(IStatus.ERROR, e.getMessage());
                Logger.log(IStatus.INFO, Logger.getStackTrace(e));
            }
        }
        
        /**
         * @param project
         *            The project built. We can't be sure whether the build is
         *            full or incremental.
         */
        private void onProjectBuilt(final IProject project) {
            CodeCheckerProject cCProject = CodeCheckerContext.getInstance().getCcProject(project);
            try {
                cCProject.copyLogFile();
                cCProject.mergeCompilationLog();
            } catch (IOException e) {
                Logger.log(IStatus.ERROR, "Couldn't copy logfile!");
            }

        }

        /**
         *
         * @param project The project that got cleaned.
         */
        private void onProjectCleaned(IProject project) {
            if (project == null)
                return;
            Logger.log(IStatus.INFO,
                    " " + project.getName() + " onProjectCleaned called.");
            try {
                if (!project.hasNature(CodeCheckerNature.NATURE_ID)) {
                    return;
                }
            } catch (CoreException e) {
                Logger.log(IStatus.ERROR, "" + e);
                Logger.log(IStatus.INFO, "" + e.getStackTrace());
            }        
            // Delete the old logfile
            CodeCheckerContext.getInstance().getCcProject(project).deleteMasterLogFile();
        }
    }

    private static class ISaveCommandExecutionListener implements IExecutionListener {
        private static AnalyzeJob analyzeJob;
        private static PlistParseJob plistParseJob;
        
        @Override
        public void notHandled(String commandId, NotHandledException exception) {
            Logger.log(IStatus.INFO, "notHandled not Implemented");
        }

        @Override
        public void postExecuteFailure(String commandId, ExecutionException exception) {
            Logger.log(IStatus.INFO, "postExecuteFailure not Implemented");
        }

        @Override
        public void postExecuteSuccess(String commandId, Object returnValue) {
            if (IWorkbenchCommandConstants.FILE_SAVE.equals(commandId)) {
                Logger.log(IStatus.INFO, "There was a save event!");
                analyzeJob.schedule();
                plistParseJob.schedule();
            }
        }

        @Override
        public void preExecute(String commandId, ExecutionEvent event) {
            if (IWorkbenchCommandConstants.FILE_SAVE.equals(commandId)) {
                Logger.log(IStatus.INFO, "There will be a save event!");
                Logger.log(IStatus.INFO, ResourceUtil.getFile(HandlerUtil.getActiveEditorInput(event)).getLocation()
                        .toFile().toPath().toAbsolutePath().toString());

                IFile savedFile = ResourceUtil.getFile(HandlerUtil.getActiveEditorInput(event));
                IProject project = savedFile.getProject();
                try {
                    if (project == null || !project.hasNature(CodeCheckerNature.NATURE_ID))
                        return;
                } catch (CoreException e) {
                    e.printStackTrace();
                }

                analyzeJob = new AnalyzeJob(project, savedFile.getLocation().toFile().toPath());
                plistParseJob = new PlistParseJob(project);
                analyzeJob.setRule(project);
                plistParseJob.setRule(project);
                plistParseJob.addJobChangeListener(new JobDoneChangeListener() {
                    @Override
                    public void done(IJobChangeEvent event) {
                        CodeCheckerContext.getInstance().refresAsync(project);
                    }
                });
            }
        }
    }
}
