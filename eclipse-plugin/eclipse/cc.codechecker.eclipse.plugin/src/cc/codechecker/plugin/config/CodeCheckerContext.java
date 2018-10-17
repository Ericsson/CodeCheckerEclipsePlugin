package cc.codechecker.plugin.config;

import cc.codechecker.plugin.report.PlistParser;
import cc.codechecker.plugin.report.ResultFilter;

import cc.codechecker.plugin.runtime.CodecheckerServerThread;
import cc.codechecker.plugin.runtime.OnCheckCallback;

import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.ReportListViewCustom;
import cc.codechecker.plugin.views.report.list.ReportListViewListener;
import cc.codechecker.plugin.views.report.list.ReportListViewProject;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Instant;

import cc.codechecker.plugin.Logger;
import org.eclipse.core.runtime.IStatus;
/**
 * The Class CodeCheckerContext.
 */
public class CodeCheckerContext {

    /** The instance. */
    static CodeCheckerContext instance;

    /** The job runner. */
    // TODO UPLIFT jobrunner will be a thread.
    //JobRunner jobRunner;

    /** The active editor part. */
    IEditorPart activeEditorPart;

    /** The servers. */
    private HashMap<IProject, CodecheckerServerThread> servers = new HashMap<>();

    /** The active project. */
    private IProject activeProject = null;

    /**
     * Class constructor.
     */
    private CodeCheckerContext() {
    	//actionRunner = new SimpleActionRunner<SimpleCommunicationInterface>(
    		//	new SimpleCommunicationFactory(), (new CodecheckerActionInitializer()).initialize(new
    		//		ActionImplementationRegistry<SimpleCommunicationInterface>()));
    		//jobRunner = new SimpleJobRunner(actionRunner);
    }

    /**
     * The refresher for Project ReportList View.
     * 
     * @param pages the page list for the currently active workbench windows.
     * @param project the project, project the buglist to be refreshed
     * @param noFetch if true, the server will not be asked for new list 
     */
    private void refreshProject(IWorkbenchPage[] pages, IProject project, boolean noFetch) {
        Logger.log(IStatus.INFO, "Refreshing bug list for project:"+project.getName());
        for(IWorkbenchPage page : pages) {
            for (IViewReference vp : page.getViewReferences()) {
                if (vp.getId().equals(ReportListViewProject.ID)) {
                    ReportListViewProject rlvp = (ReportListViewProject) vp.getView(true);
                    if (!noFetch || this.activeProject != project) {
                        rlvp.onEditorChanged(project);
                    }
                }
            }
        }
    }

    /**
     * The refresher for Current ReportList View. 
     *
     * @param pages the page list for the currently active workbench windows
     * @param project the project, the user change his/her view to
     * @param filename the filename
     * @param considerViewerRefresh false if the refresh should always happen despite of no real need to force refresh
     */
    private void refreshCurrent(IWorkbenchPage[] pages, IProject project, String filename,
            boolean considerViewerRefresh) {
        for(IWorkbenchPage page : pages) {
            for (IViewReference vp : page.getViewReferences()) {
                if (vp.getId().equals(ReportListView.ID)) {
                    ReportListView rlv = (ReportListView) vp.getView(true);
                    if (!considerViewerRefresh || rlv.getViewerRefresh()) {
                        rlv.onEditorChanged(project, filename);
                    } else {
                        rlv.setViewerRefresh(true);
                    }
                }
            }
        }
    }

    /**
     * The refresher for Custom ReportList View. If secondary id is empty, 
     * it checks if a refresh really needs to happen and if so updates every 
     * custom view for the current project. If secondary-id is not empty, 
     * it will search for the secondary-id custom view and 
     * updates that particular one.
     *
     * @param pages the page list for the currently active workbench windows.
     * @param project the project, the user change his/her view to
     * @param secondaryId id of the {@link ReportListViewCustom} the refresh
     * @param considerViewerRefresh false if the refresh should always happen despite of no real need to force refresh
     */
    private void refreshCustom(IWorkbenchPage[] pages, IProject project, String secondaryId,
            boolean considerProjectChange) {
        for(IWorkbenchPage page : pages) {
            for (IViewReference vp : page.getViewReferences()) {
                if (vp.getId().equals(ReportListViewCustom.ID)) {
                    ReportListViewCustom rlvc = (ReportListViewCustom) vp.getView(true);
                    if(secondaryId.equals("") && rlvc.getViewSite().getSecondaryId() != null) {
                        if (!considerProjectChange || this.activeProject != project) {
                            rlvc.onEditorChanged(project);
                        }
                    } else if(rlvc.getViewSite().getSecondaryId() != null && 
                            rlvc.getViewSite().getSecondaryId().equals(secondaryId)){
                        rlvc.onEditorChanged(project);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Gets the single instance of CodeCheckerContext.
     *
     * @return CodeCheckerContext
     */
    public static CodeCheckerContext getInstance() {
        if (instance == null) {
            instance = new CodeCheckerContext();
        }
        return instance;
    }

    /**
     * Gets the server object.
     *
     * @param project the project which the user change there view to.
     * @return CodecheckerServerThread
     */
    public synchronized CodecheckerServerThread getServerObject(final IProject project) {
        if (!servers.containsKey(project)) {
            CodecheckerServerThread serverObj = new CodecheckerServerThread();
            serverObj.setCallback(new OnCheckCallback() {

                @Override
                public void analysisFinished(String result) {
                    Logger.consoleLog(project.getName() + "Analysis finished.");
                    cleanCache();
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            CodeCheckerContext.getInstance().refreshAfterBuild(project);
                        }
                    });
                }
                @Override
                public void analysisStarted(String msg) {
                    Logger.consoleLog(project.getName() + " Analysis Started. "+msg);
                }
            });
            CcConfiguration config = new CcConfiguration(project);
            config.updateServer(serverObj);
            servers.put(project, serverObj);
        }
        return servers.get(project);
    }

    /**
     * Clean cache.
     */
    public void cleanCache() {
        //jobRunner.getActionCacheFilter().removeAll();
        Logger.log(IStatus.INFO, " CLEARING CACHE");
    }

    /**
     * Refresh after build.
     *
     * @param project the project, the user change his/her view to
     */
    public void refreshAfterBuild(final IProject project) {
        Logger.log(IStatus.INFO, "refreshAfterBuild");

        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if(activeWindow == null) {
            Logger.log(IStatus.ERROR, " Error activeWindow is null!");
            return;
        }

        IWorkbenchPage[] pages = activeWindow.getPages();
        IWorkbenchPage activePage = activeWindow.getActivePage();
        if (activePage == null) {
            Logger.log(IStatus.INFO, " activePage is null!");
            return;
        }

        IEditorPart partRef = activePage.getActiveEditor();

        //partRef is null or partRef NOT instanceof FileEditor!
        if (partRef == null || !(partRef.getEditorInput() instanceof IFileEditorInput)) {
            this.refreshProject(pages, project, false);
            this.refreshCustom(pages, project, "", false);
            this.activeProject = project;
            Logger.log(IStatus.INFO, " partRef is null or partRef instanceof FileEditor!");
            return;
        }
       
        
        activeEditorPart = partRef;
        IFile file = ((IFileEditorInput) partRef.getEditorInput()).getFile();
                
        if (project!=this.activeProject){
            Logger.log(IStatus.INFO, "New results do not refer to the active project"+this.activeProject.getName());
            return;
        }

        CcConfiguration config = new CcConfiguration(project);
        if (!config.isConfigured()) {
            Logger.log(IStatus.INFO, "Codechecker not configured.");
            return;
        }

        String filename = config.convertFilenameToServer(file.getProjectRelativePath().toString());
        this.refreshCurrent(pages, project, filename, false);
        this.refreshProject(pages, project, false);
        this.refreshCustom(pages, project, "", false);
        this.activeProject = project;
    }

    /**
     * Refresh change editor part.
     *
     * @param partRef the IEditorPart which the user has switched.
     */
    public void refreshChangeEditorPart(IEditorPart partRef) {        
        if (partRef.getEditorInput() instanceof IFileEditorInput){
            //could be FileStoreEditorInput
            //for files which are not part of the
            //current workspace
            activeEditorPart = partRef;
            IFile file = ((IFileEditorInput) partRef.getEditorInput()).getFile();
            IProject project = file.getProject();

            CcConfiguration config = new CcConfiguration(project);
            if (!config.isConfigured()) {
                return;
            }

            String filename = config.convertFilenameToServer(file.getProjectRelativePath().toString());

            IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if(activeWindow == null) {
                Logger.log(IStatus.ERROR, " Error activeWindow is null!");
                return;
            }
            IWorkbenchPage[] pages = activeWindow.getPages();

            this.refreshProject(pages, project, true);
            this.refreshCurrent(pages, project, filename, true);
            this.refreshCustom(pages, project, "", true);
            this.activeProject = project;
        }
    }

    /**
     * Refresh change project.
     *
     * @param project the project, the user change his/her view to
     */
    public void refreshChangeProject(IProject project) {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if(activeWindow == null) {
            Logger.log(IStatus.ERROR, "Error activeWindow is null!");
            return;
        }

        IWorkbenchPage[] pages = activeWindow.getPages();

        this.refreshProject(pages, project, true);
        this.refreshCustom(pages, project, "", true);
        this.activeProject = project;
    }

    /**
     * Refresh add custom report list view.
     *
     * @param secondaryId the ReportListCustomView secondary id
     */
    public void refreshAddCustomReportListView(String secondaryId) {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if(activeWindow == null) {
            Logger.log(IStatus.ERROR, "Error activeWindow is null!");
            return;
        }

        IEditorPart partRef = activeWindow.getActivePage().getActiveEditor();
        if(partRef == null) {
            Logger.log(IStatus.INFO, "partRef is null!");
            return;
        }
        activeEditorPart = partRef;
        IFile file = ((IFileEditorInput) partRef.getEditorInput()).getFile();
        IProject project = file.getProject();

        IWorkbenchPage[] pages = activeWindow.getPages();

        this.refreshCustom(pages, project, secondaryId, true);
        this.activeProject = project;
    }

    /**
     * Run report job.
     *
     * @param target the target
     * @param filters the filters
     * @param runId the run id
     */
    public void runReportJob(ReportListView target, ImmutableList<ResultFilter> filters,
            Optional<Long> runId) {
        IProject project = target.getCurrentProject();
        if (project == null) return;
        CcConfiguration config = new CcConfiguration(project);
        if (!config.isConfigured()) return;
        Logger.log(IStatus.INFO, "Running search to URL:"+config.getServerUrl());

        //SearchJob rlj = new SearchJob(1, Optional.of(new Instant().plus(500)), new SearchRequest
        //        (config.getServerUrl(), runId, filters));
        //rlj.addListener(new ReportListViewListener(target));
        //rlj.addListener(new MarkerListener(project));
        
        //TODO run plistparse here.
        // Create new plist parser.
        PlistParser parser = new PlistParser();
        parser.AddListener(new ReportListViewListener(target));
        // add listeners to it.
        try {
			parser.ProcessResult(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //jobRunner.addJob(rlj);
    }

    /**
     * Stop servers.
     */
    public void stopServers() {
        for (CodecheckerServerThread server : servers.values()) {
            server.stop();
        }
        servers.clear();
    }

}
