package org.codechecker.eclipse.plugin.views.report.list.provider.label;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.codechecker.eclipse.plugin.report.BugPathItem;
import org.codechecker.eclipse.plugin.report.ReportInfo;
import org.codechecker.eclipse.plugin.report.SearchList;
import org.codechecker.eclipse.plugin.views.report.list.ReportListView;

public class BasicViewLabelProvider extends LabelProvider {

    private final ReportListView reportListView;

    public BasicViewLabelProvider(ReportListView reportListView) {
        this.reportListView = reportListView;
    }

    public String getText(Object obj) {
        if (obj instanceof ReportInfo) {
            ReportInfo ri = (ReportInfo) obj;
            Path path = new Path(ri.getCheckedFile());
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            return "#" + ri.getReportId() + ": " + file.getName() + " [" + ri
                    .getLastBugPathItem().getStartPosition().getLine() + ":" + ri
                    .getLastBugPathItem().getStartPosition().getColumn() + "]";
        }
        if(obj instanceof BugPathItem) {
        	BugPathItem bpi = (BugPathItem) obj;
            Path path = new Path(bpi.getFile());
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            
            return file.getName() + " : " + bpi.getStartPosition().getLine() + " : " + bpi.getMessage();
        }
        if (obj instanceof String) {
            // TODO: add counters
            //return ((String)obj) + " (?)";
        }
        return obj.toString();
    }

    public Image getImage(Object obj) {
        String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
        if (obj instanceof String || obj instanceof SearchList || obj instanceof ReportInfo) // TODO: provide better images
            imageKey = ISharedImages.IMG_OBJ_FOLDER;
        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
    }
}
