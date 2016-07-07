package cc.codechecker.plugin.views.report.list;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import cc.codechecker.api.action.BugPathItem;
import cc.codechecker.api.action.bug.path.ProblemInfo;
import cc.codechecker.api.action.run.RunInfo;
import cc.codechecker.api.action.run.list.RunList;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.plugin.config.CodeCheckerContext;
import cc.codechecker.plugin.config.filter.Filter;
import cc.codechecker.plugin.config.filter.FilterConfiguration;
import cc.codechecker.plugin.config.project.CcConfiguration;
import cc.codechecker.plugin.views.report.details.BugPathListView;
import cc.codechecker.plugin.views.report.list.action.AnalyzeAllAction;
import cc.codechecker.plugin.views.report.list.action.LinkToEditorAction;
import cc.codechecker.plugin.views.report.list.action.NewInstanceAction;
import cc.codechecker.plugin.views.report.list.action.ShowFilterConfigurationDialog;
import cc.codechecker.plugin.views.report.list.action.ShowInBugPathViewAction;
import cc.codechecker.plugin.views.report.list.action.bugpath.BugPathMenuProvider;
import cc.codechecker.plugin.views.report.list.action.bugpath.NewBugPathView;
import cc.codechecker.plugin.views.report.list.action.rerun.RefreshAction;
import cc.codechecker.plugin.views.report.list.action.rerun.RerunAllAction;
import cc.codechecker.plugin.views.report.list.action.rerun.RerunSelectedAction;
import cc.codechecker.plugin.views.report.list.action.showas.CheckerGroupAction;
import cc.codechecker.plugin.views.report.list.action.showas.CheckerTreeAction;
import cc.codechecker.plugin.views.report.list.provider.content.TreeCheckerContentProvider;
import cc.codechecker.plugin.views.report.list.provider.label.BasicViewLabelProvider;

public class ReportListView extends ViewPart {

    public static final String ID = "cc.codechecker.plugin.views.ReportList";
    FilterConfiguration activeConfiguration = new FilterConfiguration();

    ;
    Optional<SearchList> reportList = Optional.<SearchList>absent();
    private TreeViewer viewer;
    private LinkToEditorAction linkToEditorAction;
    private ShowInBugPathViewAction defaultShowAction;
    private AnalyzeAllAction analyzeAllAction;
    private Composite parent;
    private String currentFilename;
    private IProject currentProject;
    private ImmutableList<RunInfo> runList;
    
    public ReportListView() {
    }

    public void createPartControl(Composite parent) {

        this.parent = parent;

        parent.setLayout(new GridLayout(1, true));

        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.setContentProvider(new TreeCheckerContentProvider(this));
        viewer.setLabelProvider(new BasicViewLabelProvider(this));
        viewer.setInput(new EmptyModel());
        GridData treeGridData = new GridData();
        treeGridData.grabExcessHorizontalSpace = true;
        treeGridData.grabExcessVerticalSpace = true;
        treeGridData.verticalAlignment = GridData.FILL;
        treeGridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(treeGridData);

        linkToEditorAction = new LinkToEditorAction(this, true);
        defaultShowAction = new ShowInBugPathViewAction(this); // TODO
        analyzeAllAction = new AnalyzeAllAction(this);


        // Create the help context id for the viewer's control
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();

        parent.pack();
    }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                ReportListView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        //manager.add(new RefreshAction(this));
        //manager.add(new RerunAllAction(this));
        //manager.add(new RerunSelectedAction(this));
        //manager.add(new Separator());
        /*
        MenuManager filterConfigMenu = new MenuManager("Load configuration", null);
		filterConfigMenu.add(new Action("Config 1"){});
		filterConfigMenu.add(new Action("Config 2"){});
		filterConfigMenu.add(new Separator());
		filterConfigMenu.add(new Action("Global config 1"){});
		manager.add(filterConfigMenu);*/
        manager.add(new ShowFilterConfigurationDialog(this));
        /*
        manager.add(new Separator());

		manager.add(new NewInstanceAction(this));

		MenuManager bugPathMenu = new MenuManager("Default bug path window", null);
		bugPathMenu.add(new Action(){}); // dummy
		bugPathMenu.setRemoveAllWhenShown(true);
		bugPathMenu.addMenuListener(new BugPathMenuProvider(this));
		manager.add(bugPathMenu);
		*/
        manager.add(linkToEditorAction);

        MenuManager displayTypeMenu = new MenuManager("Show as", null);
        displayTypeMenu.add(new CheckerGroupAction(this, false));
        displayTypeMenu.add(new CheckerTreeAction(this, true));
        manager.add(displayTypeMenu);
    }

    private void fillContextMenu(IMenuManager manager) {
        //manager.add(new RerunSelectedAction(this));
        manager.add(linkToEditorAction);
        manager.add(defaultShowAction);
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(defaultShowAction);
        manager.add(linkToEditorAction);
        manager.add(analyzeAllAction);
        manager.add(new Separator());
    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                final IStructuredSelection selection = (IStructuredSelection) event.getSelection();

                if (selection == null || selection.isEmpty()) {
                    return;
                }

                final Object sel = selection.getFirstElement();
                if(sel instanceof BugPathItem) {
                	BugPathItem bpi = (BugPathItem) sel;
                    jumpToBugPosition(bpi);
                    return;
                }
                final ITreeContentProvider provider =
                        (ITreeContentProvider) viewer.getContentProvider();

                if (!provider.hasChildren(sel)) {
                    if (defaultShowAction.isEnabled()) {
                        defaultShowAction.run();
                    }

                    return;
                }

                if (viewer.getExpandedState(sel)) {
                    viewer.collapseToLevel(sel, AbstractTreeViewer.ALL_LEVELS);
                } else {
                    viewer.expandToLevel(sel, 1);
                }
            }
        });
    }
    
    private void jumpToBugPosition(BugPathItem bpi) {
        IProject prj = currentProject;
        CcConfiguration config = new CcConfiguration(prj);

        String relName = config.convertFilenameFromServer(bpi.getFile());

        System.out.println("FOLLOW> " + relName);
        IFile fileinfo = prj.getFile(relName);

        if (fileinfo != null && fileinfo.exists()) {
            System.out.println("FOLLOW> fileinfo found");
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(IMarker.LINE_NUMBER, new Integer((int) bpi.getStartPosition().getLine()));
            map.put(IDE.EDITOR_ID_ATTR, "org.eclipse.ui.DefaultTextEditor");
            IMarker marker;
			IEditorPart active = null;
            for(IEditorPart ieditorpart : page.getEditors()) {
            	String ieditorinputname = ieditorpart.getEditorInput().getName();
            	System.out.println("FOLLOW> ieditorinputname: " + ieditorinputname);
            	System.out.println("FOLLOW> fileinfo name: " + fileinfo.getName());
            	if(ieditorinputname.equals(fileinfo.getName())) {
            		active = ieditorpart;
            	}
            }
            try {
                marker = fileinfo.createMarker(IMarker.TEXT);
                marker.setAttributes(map);
            	IDE.openEditor(page, fileinfo);
            	IDE.gotoMarker(page.getActiveEditor(), marker);
                System.out.println("FOLLOW> opened editor");
                marker.delete();
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(), "ReportList", message);
    }
    
    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public void changeModel(SearchList root) {
        if (this.reportList.orNull() != root) {
            this.reportList = Optional.of(root);
            viewer.setInput(root);
        }
    }

    public void clearModel() {
        this.reportList = Optional.absent();
        viewer.setInput(new EmptyModel());
    }

    public void refresh(Object parent) {
        viewer.refresh();
    }

    public Optional<SearchList> getReportList() {
        return reportList;
    }

    public void setProviders(LabelProvider labelProvider, ITreeContentProvider contentProvider) {
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(labelProvider);
    }

    public TreeViewer getViewer() {
        return viewer;
    }

    public boolean linkedToEditor() {
        return linkToEditorAction.isChecked();
    }

    private void redoJob() {
        FilterConfiguration sent = activeConfiguration.dup();

        if (linkedToEditor()) {
            if (sent.getFilters().isEmpty()) {
                LinkedList<Filter> filters = new LinkedList<>();
                filters.add(new Filter());
                sent.setFilters(filters);
            }

            for (Filter f : sent.getFilters()) {
                if (f.getFilepath().equals("")) {
                    f.setFilepath(currentFilename);
                }
            }
        }

        Optional<Long> runId = Optional.absent();

        CodeCheckerContext.getInstance().runReportJob(this, sent.convertToResultList(), runId);
    }

    public void onEditorChanged(IProject project, String filename) {
        if (project != currentProject) {
            System.out.println("PreChanging runList!");
            this.currentProject = project;
            //CodeCheckerContext.getInstance().runRunListJob(this);
        }
        if (!linkedToEditor()) {
            this.currentFilename = "";
        }

        this.currentFilename = filename;

        redoJob();
    }

    public FilterConfiguration getActiveConfiguration() {
        return activeConfiguration;
    }

    public void setActiveConfiguration(FilterConfiguration activeConfiguration) {
        this.activeConfiguration = activeConfiguration;

        linkToEditorAction.setChecked(activeConfiguration.isLinkToCurrentEditorByDefalt());

        redoJob();
    }

    public IProject getCurrentProject() {
        return currentProject;
    }

    static class EmptyModel {
    }

}
