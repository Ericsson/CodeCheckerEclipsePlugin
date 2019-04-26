package org.codechecker.eclipse.plugin.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

import org.codechecker.eclipse.plugin.views.report.list.ReportListView;
import org.codechecker.eclipse.plugin.views.report.list.ReportListViewProject;

public class CodeCheckerPerspectiveFactory implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {
        defineActions(layout);
        defineLayout(layout);
    }

    private void defineLayout(IPageLayout layout) {
        // TODO Auto-generated method stub

    }

    private void defineActions(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.26,
                editorArea);
        left.addView(IPageLayout.ID_PROJECT_EXPLORER);

        IFolderLayout middleLeft = layout.createFolder("middleLeft", IPageLayout.BOTTOM, (float)
                0.33, "left");
        middleLeft.addView(ReportListView.ID);
        middleLeft.addView(ReportListViewProject.ID);
        
        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) 0.8,
                editorArea);
        right.addView(IConsoleConstants.ID_CONSOLE_VIEW);
        right.addView(IPageLayout.ID_OUTLINE);
        right.addView(IPageLayout.ID_TASK_LIST);

        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.8,
                editorArea);
        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
    }

}
