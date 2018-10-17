package cc.codechecker.plugin.init;

import java.util.HashSet;

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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.config.CodeCheckerContext;

import cc.codechecker.plugin.Logger;
import cc.codechecker.plugin.ExternalLogger;

import cc.codechecker.plugin.runtime.SLogger;
import cc.codechecker.plugin.runtime.CodecheckerServerThread;

public class StartupJob extends Job {



    EditorPartListener partListener;
    ProjectExplorerSelectionListener projectexplorerselectionlistener;    

    public StartupJob() {
        super("CodeChecker Startup Job");
        partListener = new EditorPartListener();
        projectexplorerselectionlistener = new ProjectExplorerSelectionListener();
        SLogger.setLogger(new ExternalLogger());//setting up the eclips logger for the external service
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
            Logger.log(IStatus.ERROR, " " + e);
            Logger.log(IStatus.INFO, " " + e.getStackTrace());
        }

        Logger.log(IStatus.INFO, "adding addResourceChangeListener ");
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

            @Override
            public void resourceChanged(final IResourceChangeEvent event) {                
                switch (event.getType()) {
                case IResourceChangeEvent.PRE_BUILD: {
                	Logger.log(BUILD, "PreBuild");
                	break;
                }
                case IResourceChangeEvent.POST_BUILD: {
                    //On Eclipse FULL_BUILD and INCREMENTAL build, recheck the project
                    //on clean build drop the checker database
                    //ignore AUTO_BUILD
                    try{
                        if (event.getBuildKind() != IncrementalProjectBuilder.AUTO_BUILD){                        
                            Logger.log(IStatus.INFO, "Build called. type:"+event.getBuildKind());                            
                            final HashSet<IProject> builtProjects = new HashSet<>();
                            final HashSet<IProject> cleanedProjects = new HashSet<>();
                            event.getDelta().accept(new IResourceDeltaVisitor() {
                                public boolean visit(final IResourceDelta delta) throws CoreException {                                    
                                    IProject project=delta.getResource().getProject();                                                                      
                                    if (project!=null && project.hasNature(CodeCheckerNature.NATURE_ID)){
                                        if (event.getBuildKind() == IncrementalProjectBuilder.FULL_BUILD
                                                ||event.getBuildKind() == IncrementalProjectBuilder.INCREMENTAL_BUILD){
                                            builtProjects.add(project);
                                        }
                                        else
                                            if (event.getBuildKind() == IncrementalProjectBuilder.CLEAN_BUILD){
                                                cleanedProjects.add(project);
                                            }
                                    }                                    
                                    return true;
                                }
                            });
                            //re-analyze built projects
                            for (IProject p : builtProjects) {
                                onProjectBuilt(p);
                            }                            
                            //drop cleaned projects
                            for (IProject p : cleanedProjects) {
                                onProjectCleaned(p);
                            }
                        }else
                            if (event.getBuildKind()== IncrementalProjectBuilder.CLEAN_BUILD){
                                Logger.log(IStatus.INFO, "CLEAN_BUILD called. Resetting bug database");

                            }
                    } catch (CoreException e) {
                        // TODO Auto-generated catch block
                        Logger.log(IStatus.ERROR, " " + e);
                        Logger.log(IStatus.INFO, " " + e.getStackTrace());
                    }
                    break;
                }
                }
                try {
                    event.getDelta().accept(new IResourceDeltaVisitor() {
                        public boolean visit(final IResourceDelta delta) throws CoreException {
                            IResource resource = delta.getResource();
                            if (((resource.getType() & IResource.PROJECT) != 0) && resource.getProject().isOpen()
                                    && delta.getKind() == IResourceDelta.CHANGED
                                    && ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {
                                Logger.log(IStatus.INFO, "Project visit called for project:"
                                        + resource.getProject().getName());
                                IProject project = (IProject) resource;
                                projectOpened(project);
                            }
                            return true;
                        }
                    });
                } catch (CoreException e) {
                    Logger.log(IStatus.ERROR, " " + e);
                    Logger.log(IStatus.INFO, " " + e.getStackTrace());
                }
            }

        }, IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE | IResourceDelta.OPEN);

        Logger.log(IStatus.INFO, "Starting CodeChecker Servers.");
        // check all open projects
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            projectOpened(project);
        }
        Logger.log(IStatus.INFO, "CodeChecker servers started");

        // check all open windows
        for (IWorkbenchWindow win : wb.getWorkbenchWindows()) {
            addListenerToWorkbenchWindow(win);
        }
        return Status.OK_STATUS;
    }

    private void onProjectBuilt(IProject project) {
        if (project == null)
            return;
        Logger.log(IStatus.INFO,
                " " + project.getName() + " onProjectBuilt called.");
        try {
            if (!project.hasNature(CodeCheckerNature.NATURE_ID)) {
                return;
            }
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            Logger.log(IStatus.ERROR, "" + e);
            Logger.log(IStatus.INFO, "" + e.getStackTrace());
        }

        CodecheckerServerThread server = CodeCheckerContext.getInstance().getServerObject(project);
        if (project.isOpen()) {
            if (!server.isRunning())
                server.start(); // ensure started!
            server.recheck();
        }        
    }

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
        CodecheckerServerThread server = CodeCheckerContext.getInstance().getServerObject(project);
        Logger.log(IStatus.INFO,
                " " + project.getName() + "stopping server");
        server.stop();
        Logger.log(IStatus.INFO,
                " " + project.getName() + " server stopped.");
        //TODO UPLIFT Clean something???
        //server.cleanDB();
        //FIXME: database needs to be recreated at this point.
        //and the results should be emptied.
        Logger.log(IStatus.INFO,
                " " + project.getName() + " db cleaned;");
        Logger.consoleLog(project.getName() + " checker db cleaned;");
        server.start();
        Logger.log(IStatus.INFO,
                " " + project.getName() + " server started.");        
    }

    private void projectOpened(IProject project) {
        if (project == null)
            return;
        try {
            //if CodecheCker nature is not set or the project is non-CDT we don launch CodeChecker server
            if (!project.hasNature(CodeCheckerNature.NATURE_ID) || 
                    CoreModel.getDefault().getProjectDescription(project, true)==null) {
                return;
            }
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            Logger.log(IStatus.ERROR, "" + e);
            Logger.log(IStatus.INFO, "" + e.getStackTrace());
        }
        Logger.log(IStatus.INFO, "CodeChecker nature found!");
        try {
            CodecheckerServerThread server = CodeCheckerContext.getInstance().getServerObject(project);
            if (project.isOpen()) {
                if (!server.isRunning()){
                    Logger.log(IStatus.INFO, "Starting server+"+project.getName());
                    server.start(); // ensure started!
                    Logger.log(IStatus.INFO, "server started+"+project.getName());
                }
            } else {
                if (server.isRunning())
                    Logger.log(IStatus.INFO, "Stopping server+"+project.getName());
                server.stop();
                Logger.log(IStatus.INFO, "Server stopped:+"+project.getName());
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
            Logger.log(IStatus.ERROR, "" + e);
            Logger.log(IStatus.INFO, "" + e.getStackTrace());
        }
    }

}
