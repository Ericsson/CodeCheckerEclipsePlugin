package org.codechecker.eclipse.plugin.views.report.list;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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


import org.codechecker.eclipse.plugin.report.BugPathItem;
import org.codechecker.eclipse.plugin.report.SearchList;
import org.codechecker.eclipse.plugin.config.CodeCheckerContext;
import org.codechecker.eclipse.plugin.config.filter.Filter;
import org.codechecker.eclipse.plugin.config.filter.FilterConfiguration;
import org.codechecker.eclipse.plugin.views.report.list.action.NewInstanceAction;
import org.codechecker.eclipse.plugin.views.report.list.action.ShowFilterConfigurationDialog;
import org.codechecker.eclipse.plugin.views.report.list.action.showas.CheckerGroupAction;
import org.codechecker.eclipse.plugin.views.report.list.action.showas.CheckerTreeAction;
import org.codechecker.eclipse.plugin.views.report.list.provider.content.TreeCheckerContentProvider;
import org.codechecker.eclipse.plugin.views.report.list.provider.label.BasicViewLabelProvider;
import org.codechecker.eclipse.plugin.Logger;
import org.eclipse.core.runtime.IStatus;

public class ReportListView extends ViewPart {

    public static final String ID = "org.codechecker.eclipse.plugin.views.ReportList";
    FilterConfiguration activeConfiguration = new FilterConfiguration();
    Optional<SearchList> reportList = Optional.<SearchList>absent();
    private TreeViewer viewer;
    private boolean viewerRefresh = true;
    private Composite parent;
    private String currentFilename;
    private IProject currentProject;
    private ShowFilterConfigurationDialog showfilterconfigurationdialog;

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
        this.showfilterconfigurationdialog = new ShowFilterConfigurationDialog(this);
        this.viewerRefresh = true;


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
        manager.add(this.showfilterconfigurationdialog);
        /*
        manager.add(new Separator());
        
        manager.add(new NewInstanceAction(this));
        
        MenuManager bugPathMenu = new MenuManager("Default bug path window", null);
        bugPathMenu.add(new Action(){}); // dummy
        bugPathMenu.setRemoveAllWhenShown(true);
        bugPathMenu.addMenuListener(new BugPathMenuProvider(this));
        manager.add(bugPathMenu);
         */
        manager.add(new NewInstanceAction(new ReportListViewCustom()));
        MenuManager displayTypeMenu = new MenuManager("Show as", null);
        displayTypeMenu.add(new CheckerGroupAction(this, false));
        displayTypeMenu.add(new CheckerTreeAction(this, true));
        manager.add(displayTypeMenu);
    }

    private void fillContextMenu(IMenuManager manager) {
        //manager.add(new RerunSelectedAction(this));
        manager.add(new NewInstanceAction(new ReportListViewCustom()));
        manager.add(new Separator());
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
    	//TODO UPLIFT
        //manager.add(this.showfilterconfigurationdialog);
        manager.add(new NewInstanceAction(new ReportListViewCustom()));
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
        String relName = CodeCheckerContext.getInstance().getCcProject(currentProject)
                .stripProjectPathFromFilePath(bpi.getFile());
        IFile fileinfo = currentProject.getFile(relName);

        if (fileinfo != null && fileinfo.exists()) {
            if(!fileinfo.getName().equals(currentFilename)) {
                this.setViewerRefresh(false);
            } else {
                this.setViewerRefresh(true);
            }

            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(IMarker.LINE_NUMBER, new Integer((int) bpi.getStartPosition().getLine()));
            map.put(IDE.EDITOR_ID_ATTR, "org.eclipse.ui.DefaultTextEditor");
            IMarker marker;
            try {
                marker = fileinfo.createMarker(IMarker.TEXT);
                marker.setAttributes(map);
                IDE.openEditor(page, fileinfo);
                IDE.gotoMarker(page.getActiveEditor(), marker);
                marker.delete();
            } catch (CoreException e) {
                Logger.log(IStatus.ERROR, " " + e);
                Logger.log(IStatus.INFO, " " + e.getStackTrace());
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

    public void changeModel(final SearchList root) {
        if (this.reportList.orNull() != root) {
            this.reportList = Optional.of(root);
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                	viewer.setInput(root);
                }
            });
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

    private void reloadReports(String currentFileName) {
        FilterConfiguration sent = activeConfiguration.dup();

        if (getViewerRefresh()) {
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

        //Optional<Long> runId = Optional.absent();

        CodeCheckerContext.getInstance().runReportJob(this, currentFileName);
    }

    /**
     * Called when switching between edited source files. 
     */
    public void onEditorChanged(IProject project, String filename) {
        Logger.log(IStatus.INFO, "OnEditorchanged:"+project.getName());
        if (project != currentProject) {
            this.currentProject = project;
            //CodeCheckerContext.getInstance().runRunListJob(this);
        }
        if (!getViewerRefresh()) {
            this.currentFilename = "";
        }

        this.currentFilename = filename;

        reloadReports(filename);
    }

    public FilterConfiguration getActiveConfiguration() {
        return activeConfiguration;
    }

    public void setActiveConfiguration(FilterConfiguration activeConfiguration) {
        this.activeConfiguration = activeConfiguration;

        setViewerRefresh(activeConfiguration.isLinkToCurrentEditorByDefalt());

        reloadReports(currentFilename);
    }

    public IProject getCurrentProject() {
        return currentProject;
    }

    public boolean getViewerRefresh() {
        return this.viewerRefresh;
    }

    public void setViewerRefresh(boolean viewerRefresh) {
        this.viewerRefresh = viewerRefresh;
    }

    static class EmptyModel {
    }

}
