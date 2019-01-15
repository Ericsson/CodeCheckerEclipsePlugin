package cc.codechecker.plugin.config;

import cc.codechecker.plugin.report.PlistParser;
import cc.codechecker.plugin.report.ReportParser;
import cc.codechecker.plugin.report.SearchList;
import cc.codechecker.plugin.runtime.CodecheckerServerThread;
import cc.codechecker.plugin.runtime.OnCheckCallback;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.ReportListViewCustom;
import cc.codechecker.plugin.views.report.list.ReportListViewListener;
import cc.codechecker.plugin.views.report.list.ReportListViewProject;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cc.codechecker.plugin.CodeCheckerNature;
import cc.codechecker.plugin.Logger;
import cc.codechecker.plugin.config.Config.ConfigTypes;

import org.eclipse.core.runtime.CoreException;
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

    private HashMap<IProject, SearchList> reports = new HashMap<>();
    
    private HashMap<IProject, CcConfiguration> configs = new HashMap<>();

    /**
     * Returns a {@link CcConfiguration} object.
     * .
     * @param project The project in question.
     * @return If there is no CcConfiguration stored, then creates a new instance.
     */
    public CcConfiguration getConfigForProject(IProject project) {
    	if (!configs.containsKey(project)) 
    		setConfig(project, new CcConfiguration(project));
    	StringBuilder sb = new StringBuilder();
        CcConfiguration.logConfig(configs.get(project).getProjectConfig(null));
    	return configs.get(project);
	}

	public void setConfig(IProject project, CcConfiguration config) {
		configs.put(project, config);
	}

	/** The active project. */
    private IProject activeProject = null;

    /** For storing in memory*/
    //TODO UPLIFT some haslist projects as key reports as value.

    /**
     * Class constructor.
     */
    private CodeCheckerContext() {}

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
     * @param project the project which the user change their view to.
     * @return CodecheckerServerThread
     */
    public synchronized CodecheckerServerThread getServerObject(final IProject project) {
        if (!servers.containsKey(project)) {
            CodecheckerServerThread serverObj = new CodecheckerServerThread();
            serverObj.setCallback(new OnCheckCallback() {

                @Override
                public void analysisFinished(String result) {
                    Logger.consoleLog(project.getName() + "Analysis finished.");
                    //cleanCache();
                    //Store 
                    parsePlistForProject(project);
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
     * TODO This method could be used for clearing in memory representation of the reports.
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

        //parsePlistForProject(project);

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
            //Nullptr on activeprocejt on pluginstart
            //Logger.log(IStatus.INFO, "New results do not refer to the active project"+this.activeProject.getName());
            return;
        }

        CcConfiguration config = getConfigForProject(project);

        //The actual refresh happens here.
        String filename = config.convertFilenameToServer(file.getProjectRelativePath().toString());
        this.refreshCurrent(pages, project, filename, false);
        this.refreshProject(pages, project, false);
        this.refreshCustom(pages, project, "", false);
        this.activeProject = project;
    }

    public void parsePlistForProject(final IProject project) {
        Logger.log(IStatus.INFO, "Started Plist Parsing for project: "+project.getName());
        final PlistParser parser = new PlistParser(project);
        SearchList sl;
        sl = parser.processResultsForProject();
        reports.put(project, sl);
        Logger.log(IStatus.INFO, "Finished Plist Parsing for project: "+project.getName());
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                CodeCheckerContext.getInstance().refreshAfterBuild(project);
            }
        });
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
            try {
				if (project.hasNature(CodeCheckerNature.NATURE_ID)){
				    CcConfiguration config = getConfigForProject(project);

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
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
     * @param runId the run id
     */
    public void runReportJob(ReportListView target, String currentFileName) {
        IProject project = target.getCurrentProject();
        if (project == null) return;
        Logger.log(IStatus.INFO, "Started Filtering Reports for project: "+project.getName());

        // Dont Parse Here just work from the reports Hasmap

        ReportParser parser = new ReportParser(reports.get(project), currentFileName);
        // add listeners to it.
        parser.AddListener(new ReportListViewListener(target));
        /*Thread t = new Thread(parser);
          t.start();*/
        Display.getDefault().asyncExec(parser);
        Logger.log(IStatus.INFO, "Finished Filtering Reports for project: "+project.getName());
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
