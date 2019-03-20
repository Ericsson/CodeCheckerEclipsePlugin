package cc.codechecker.plugin.init;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.ExternalLogger;
import cc.codechecker.plugin.Logger;
import cc.codechecker.plugin.config.CcConfiguration;
import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.report.job.AnalyzeJob;
import cc.codechecker.plugin.report.job.JobDoneChangeListener;
import cc.codechecker.plugin.report.job.PlistParseJob;
import cc.codechecker.plugin.runtime.SLogger;

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
        CcConfiguration.initGlobalConfig();
        
        try { // TODO: find a better solution...
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Logger.log(IStatus.ERROR, e.getMessage());
            Logger.log(IStatus.INFO, Logger.getStackTrace(e));
        }

        Logger.log(IStatus.INFO, "adding addResourceChangeListener ");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(),
                IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE | IResourceDelta.OPEN);
        
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
        if (project == null)
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
        
        CcConfiguration config = new CcConfiguration(project);
        CodeCheckerContext.getInstance().setConfig(project, config);

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
            switch (event.getType()) {
                case IResourceChangeEvent.PRE_BUILD: {
                    Logger.log(BUILD, "PreBuild");
                    break;
                }
                case IResourceChangeEvent.POST_BUILD: {
                    // On Eclipse FULL_BUILD and INCREMENTAL build, recheck the
                    // project
                    // on clean build drop the checker database
                    try {
                        if (event.getBuildKind() != IncrementalProjectBuilder.AUTO_BUILD) {
                            Logger.log(IStatus.INFO, "Build called. type:" + event.getBuildKind());
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
                                            cleanedProjects.add(project);
                                        }
                                    }
                                    return true;
                                }
                            });
                            // re-analyze built projects
                            for (IProject p : builtProjects) {
                                onProjectBuilt(p);
                            }
                            // drop cleaned projects
                            for (IProject p : cleanedProjects) {
                                onProjectCleaned(p);
                            }
                        } else if (event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD) {
                            Logger.log(IStatus.INFO, "CLEAN_BUILD called. Resetting bug database");
    
                        }
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        Logger.log(IStatus.ERROR, e.getMessage());
                        Logger.log(IStatus.INFO, Logger.getStackTrace(e));
                    }
                    break;
                }
                default:
                    break;
            }
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
         *
         * @param project The project that got built.
         */
        private void onProjectBuilt(final IProject project) {
            // resources like IProject can act as ISchedulingRule,
            // or in this case a mutex for  stopping the parse job
            // running until the analisis finished.
            // https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2FresAdv_batching.htm
            AnalyzeJob analyzeJob = new AnalyzeJob(project);
            PlistParseJob plistParseJob = new PlistParseJob(project);
            analyzeJob.setRule(project);
            plistParseJob.setRule(project);
            plistParseJob.addJobChangeListener(new JobDoneChangeListener() {
                
                @Override
                public void done(IJobChangeEvent event) {
                    CodeCheckerContext.getInstance().refresAsync(project);
                }
            });
    
            analyzeJob.schedule();
            plistParseJob.schedule();
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
                // TODO Auto-generated catch block
                Logger.log(IStatus.ERROR, "" + e);
                Logger.log(IStatus.INFO, "" + e.getStackTrace());
            }        
            //TODO UPLIFT Clean REPORTS??? 
        }
    }
}
