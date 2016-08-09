package cc.codechecker.plugin.config;

import cc.codechecker.api.thrift.CodecheckerActionInitializer;
import cc.codechecker.api.action.analyze.AnalyzeRequest;
import cc.codechecker.api.action.result.ResultFilter;
import cc.codechecker.api.action.run.list.ListRunsRequest;
import cc.codechecker.api.job.RunListJob;
import cc.codechecker.api.job.analyze.AnalyzeJob;
import cc.codechecker.api.job.report.list.SearchJob;
import cc.codechecker.api.job.report.list.SearchRequest;
import cc.codechecker.api.runtime.CodecheckerServerThread;
import cc.codechecker.api.runtime.OnCheckedCallback;
import cc.codechecker.plugin.markers.MarkerListener;
import cc.codechecker.plugin.views.console.ConsoleFactory;
import cc.codechecker.plugin.views.report.list.ReportListView;
import cc.codechecker.plugin.views.report.list.ReportListViewCustom;
import cc.codechecker.plugin.views.report.list.ReportListViewListener;
import cc.codechecker.plugin.views.report.list.ReportListViewProject;
import cc.ecl.action.ActionImplementationRegistry;
import cc.ecl.action.PerServerActionRunner;
import cc.ecl.action.PerServerSimpleActionRunner;
import cc.ecl.action.thrift.ThriftCommunicationInterface;
import cc.ecl.action.thrift.ThriftTransportFactory;
import cc.ecl.job.JobListener;
import cc.ecl.job.JobRunner;
import cc.ecl.job.SimpleJobRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Instant;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class CodeCheckerContext {

    //Logger
    private static final Logger logger = LogManager.getLogger(CodeCheckerContext.class);

    static CodeCheckerContext instance;
    JobRunner jobRunner;
    IEditorPart activeEditorPart;
    private HashMap<IProject, CodecheckerServerThread> servers = new HashMap<>();
    private IProject activeProject = null;

    private CodeCheckerContext() {
        PerServerActionRunner<ThriftCommunicationInterface> actionRunner;
        actionRunner = new PerServerSimpleActionRunner<ThriftCommunicationInterface>(new
                ThriftTransportFactory(), (new CodecheckerActionInitializer()).initialize(new
                        ActionImplementationRegistry<ThriftCommunicationInterface>()));
        jobRunner = new SimpleJobRunner(actionRunner);
    }

    private void refreshProject(IWorkbenchPage[] pages, IProject project, boolean considerProjectChange) {
        for(IWorkbenchPage page : pages) {
            for (IViewReference vp : page.getViewReferences()) {
                if (vp.getId().equals(ReportListViewProject.ID)) {
                    ReportListViewProject rlvp = (ReportListViewProject) vp.getView(true);
                    if (!considerProjectChange || this.activeProject != project) {
                        rlvp.onEditorChanged(project);
                    }
                }
            }
        }
    }

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

    private void refreshCustom(IWorkbenchPage[] pages, IProject project, String secondaryId,
            boolean considerProjectChange) {
        for(IWorkbenchPage page : pages) {
            for (IViewReference vp : page.getViewReferences()) {
                if (vp.getId().equals(ReportListViewCustom.ID)) {
                    ReportListViewCustom rlvc = (ReportListViewCustom) vp.getView(true);
                    if(secondaryId.equals("")) {
                        if (!considerProjectChange || this.activeProject != project) {
                            rlvc.onEditorChanged(project);
                        }
                    } else {
                        if (rlvc.getViewSite().getSecondaryId() != null && rlvc.getViewSite().getSecondaryId().equals(secondaryId)) {
                            rlvc.onEditorChanged(project);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static CodeCheckerContext getInstance() {
        if (instance == null) {
            instance = new CodeCheckerContext();
        }
        return instance;
    }

    public synchronized CodecheckerServerThread getServerObject(final IProject project) {
        if (!servers.containsKey(project)) {
            CodecheckerServerThread serverObj = new CodecheckerServerThread();
            serverObj.setCallback(new OnCheckedCallback() {

                @Override
                public void built() {
                    cleanCache(project);
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            ConsoleFactory.consoleWrite(project.getName() + " to built CodeChecker Data Transport Complete!");
                            CodeCheckerContext.getInstance().refreshAfterBuild();
                        }
                    });
                }
            });
            CcConfiguration config = new CcConfiguration(project);
            config.updateServer(project, serverObj, "");
            servers.put(project, serverObj);
        }
        return servers.get(project);
    }

    public void cleanCache(IProject project) {
        jobRunner.getActionCacheFilter().removeAll();
        logger.log(Level.DEBUG, "SERVER_GUI_MSG >> CLEARING CACHE");
    }


    public void refreshAfterBuild() {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if(activeWindow == null) {
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> Error activeWindow is null!");
            return;
        }

        IWorkbenchPage[] pages = activeWindow.getPages();

        //All Files Refreshing!
        if(activeWindow.getSelectionService() == null) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> activeWindow getSelectionService is null!");
        } else {
            IStructuredSelection selection = (IStructuredSelection) activeWindow.getSelectionService().getSelection();
            if(selection != null) {
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof IAdaptable) {
                    IProject project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
                    this.refreshProject(pages, project, false);
                    this.refreshCustom(pages, project, "", false);
                    this.activeProject = project;
                }
            }
        }

        //Current File Refreshing!
        IWorkbenchPage activePage = activeWindow.getActivePage();
        if (activePage == null) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> activePage is null!");
            return;
        }

        IEditorPart partRef = activePage.getActiveEditor();

        //partRef is null or partRef NOT instanceof FileEditor!
        if (partRef == null || !(partRef.getEditorInput() instanceof IFileEditorInput)) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> partRef is null or partRef instanceof FileEditor!");
            return;
        }

        activeEditorPart = partRef;
        IFile file = ((IFileEditorInput) partRef.getEditorInput()).getFile();
        IProject project = file.getProject();

        CcConfiguration config = new CcConfiguration(project);
        if (!config.isConfigured()) {
            return;
        }

        String filename = config.convertFilenameToServer(file.getProjectRelativePath().toString());
        this.refreshCurrent(pages, project, filename, false);
    }

    public void refreshChangeEditorPart(IEditorPart partRef) {
        //partRef is not instanceof IFileEditorInput or not change editorPart!
        if (!(partRef.getEditorInput() instanceof IFileEditorInput) || partRef == activeEditorPart) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> partRef is not instanceof IFileEditorInput or not change editorPart!");
            return;
        }

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
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> Error activeWindow is null!");
            return;
        }
        IWorkbenchPage[] pages = activeWindow.getPages();

        this.refreshProject(pages, project, true);
        this.refreshCurrent(pages, project, filename, true);
        this.refreshCustom(pages, project, "", true);
        this.activeProject = project;
    }

    public void refreshChangeProject(IProject project) {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if(activeWindow == null) {
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> Error activeWindow is null!");
            return;
        }

        IWorkbenchPage[] pages = activeWindow.getPages();

        this.refreshProject(pages, project, true);
        this.refreshCustom(pages, project, "", true);
        this.activeProject = project;
    }

    public void refreshAddCustomReportListView(String secondaryId) {
        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

        if(activeWindow == null) {
            logger.log(Level.ERROR, "SERVER_GUI_MSG >> Error activeWindow is null!");
            return;
        }

        IEditorPart partRef = activeWindow.getActivePage().getActiveEditor();
        if(partRef == null) {
            logger.log(Level.DEBUG, "SERVER_GUI_MSG >> partRef is null!");
            return;
        }
        activeEditorPart = partRef;
        IFile file = ((IFileEditorInput) partRef.getEditorInput()).getFile();
        IProject project = file.getProject();

        IWorkbenchPage[] pages = activeWindow.getPages();

        this.refreshCustom(pages, project, secondaryId, true);
        this.activeProject = project;
    }

    public void runReportJob(ReportListView target, ImmutableList<ResultFilter> filters,
            Optional<Long> runId) {
        IProject project = target.getCurrentProject();
        if (project == null) return;
        CcConfiguration config = new CcConfiguration(project);
        if (!config.isConfigured()) return;

        SearchJob rlj = new SearchJob(1, Optional.of(new Instant().plus(500)), new SearchRequest
                (config.getServerUrl(), runId, filters));
        rlj.addListener(new ReportListViewListener(target));
        rlj.addListener(new MarkerListener(project));
        jobRunner.addJob(rlj);
    }

    public void runRunListJob(final ReportListView target) {
        IProject project = target.getCurrentProject();
        if (project == null) return;
        CcConfiguration config = new CcConfiguration(project);
        if (!config.isConfigured()) return;

        RunListJob rlj = new RunListJob(new ListRunsRequest(config.getServerUrl()), 1, Optional
                .of(new Instant().plus(500)));
        rlj.addListener(new JobListener<RunListJob>() {

            @Override
            public void onJobTimeout(RunListJob arg0) {
            }

            @Override
            public void onJobStart(RunListJob arg0) {
            }

            @Override
            public void onJobInternalError(RunListJob arg0, RuntimeException arg1) {
            }

            @Override
            public void onJobComplete(RunListJob rlj) {
                //target.setRunList(rlj.getResult().get());
            }
        });
        jobRunner.addJob(rlj);
    }

    public void runAnalyzeJob(ReportListView target) {
        IProject project = target.getCurrentProject();
        if (project == null) return;
        CcConfiguration config = new CcConfiguration(project);
        if (!config.isConfigured()) return;

        ImmutableList<String> l = ImmutableList.of();
        AnalyzeJob rlj = new AnalyzeJob(1, Optional.of(new Instant().plus(500)), new
                AnalyzeRequest(config.getServerUrl(), l));
        //rlj.addListener(new ReportListViewListener(target));
        jobRunner.addJob(rlj);
    }


    public void stopServers() {
        for (CodecheckerServerThread server : servers.values()) {
            server.stop();
        }
        servers.clear();
    }

}
