package cc.codechecker.plugin.config;

import cc.codechecker.api.thrift.CodecheckerActionInitializer;
import cc.codechecker.api.action.analyze.AnalyzeRequest;
import cc.codechecker.api.action.result.ResultFilter;
import cc.codechecker.api.action.run.list.ListRunsRequest;
import cc.codechecker.api.job.ProblemInfoJob;
import cc.codechecker.api.job.RunListJob;
import cc.codechecker.api.job.analyze.AnalyzeJob;
import cc.codechecker.api.job.report.list.SearchJob;
import cc.codechecker.api.job.report.list.SearchRequest;
import cc.codechecker.api.runtime.CodeCheckEnvironmentChecker;
import cc.codechecker.api.runtime.CodecheckerServerThread;
import cc.codechecker.api.runtime.OnCheckedCallback;
import cc.codechecker.plugin.config.project.CcConfiguration;
import cc.codechecker.plugin.markers.MarkerListener;
import cc.codechecker.plugin.views.report.list.ReportListView;
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

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.result.ReportInfo;

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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.joda.time.Instant;

public class CodeCheckerContext {

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
                    CodeCheckerContext.getInstance().autoRefresh();
                }
            });
            CcConfiguration config = new CcConfiguration(project);
            config.updateServer(project, serverObj);
            servers.put(project, serverObj);
        }
        return servers.get(project);
    }

    public void autoRefresh() {

        IWorkbench iwork = PlatformUI.getWorkbench();
        IWorkbenchWindow[] windows = iwork.getWorkbenchWindows();
        IWorkbenchPage[] pages = windows[0].getPages();

        System.out.println("Windows Length : " + windows.length);
        System.out.println("Pages length : " + pages.length);

        IWorkbenchPage activePage = pages[0];

        if (activePage == null) return;

        IEditorPart activeEditor = activePage.getActiveEditor();

        if (activeEditor == null) return;

        CodeCheckerContext.getInstance().setActiveEditorPart(activeEditor,true);
    }

    public void cleanCache(IProject project) {
        jobRunner.getActionCacheFilter().removeAll();
        System.out.println("CLEARING CACHE");
    }

    public void setActiveEditorPart(IEditorPart partRef,boolean refresh) {

        if (!(partRef.getEditorInput() instanceof IFileEditorInput)) {
            return;
        }
        if (partRef == activeEditorPart && !refresh) {
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

        System.out.println("Changed to: " + filename);

        IWorkbench iwork = PlatformUI.getWorkbench();
        IWorkbenchWindow[] windows = iwork.getWorkbenchWindows();
        IWorkbenchPage[] pages = windows[0].getPages();

        System.out.println("Windows Length : " + windows.length);
        System.out.println("Pages length : " + pages.length);

        for(IWorkbenchPage page : pages) {
            for (IViewReference vp : page.getViewReferences()) {
                if (vp.getId().equals(ReportListView.ID)) {
                    ReportListView rlv = (ReportListView) vp.getView(true);
                    if (rlv.linkedToEditor()) {
                        rlv.onEditorChanged(project, filename);
                    }
                }
                if(vp.getId().equals(ReportListViewProject.ID)) {
                	ReportListViewProject rlvp = (ReportListViewProject) vp.getView(true);
                	if (rlvp.linkedToEditor() && project != activeProject) {
                		this.activeProject = project;
                		rlvp.onEditorChanged(project, filename);
                    }
                }
            }
        }
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
