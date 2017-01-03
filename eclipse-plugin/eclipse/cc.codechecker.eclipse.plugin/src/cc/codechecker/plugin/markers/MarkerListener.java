package cc.codechecker.plugin.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;

import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.api.job.report.list.SearchJob;
import cc.codechecker.api.job.report.list.SearchListener;
import cc.codechecker.plugin.config.CcConfiguration;


import cc.codechecker.plugin.Logger;
import org.eclipse.core.runtime.IStatus;
public class MarkerListener implements SearchListener {
	
    CcConfiguration config;
    private IProject project;

    public MarkerListener(IProject project) {
        this.project = project;
        config = new CcConfiguration(project);
    }

    @Override
    public void onJobComplete(SearchJob arg0) {
    }

    @Override
    public void onJobInternalError(SearchJob arg0, RuntimeException arg1) {
    }

    @Override
    public void onJobStart(SearchJob arg0) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onJobTimeout(SearchJob arg0) {
    }

    @Override
    public void onPartsArrived(SearchJob arg0, SearchList arg1, final ImmutableList<ReportInfo>
            list) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                for (ReportInfo ri : list) {
                    String relName = config.convertFilenameFromServer(ri.getLastBugPathItem()
                            .getFile());
                    Logger.log(IStatus.INFO, " Filename : " + relName);
                    IFile fileinfo = project.getFile(relName);

                    if (fileinfo != null && fileinfo.exists()) {
                        // delete previous markers
                        try {
                            fileinfo.deleteMarkers("cc.codechecker.markers.problemmarker", true,
                                    IResource.DEPTH_INFINITE);
                        } catch (CoreException e) {
                            // TODO Auto-generated catch block
                        	Logger.log(IStatus.ERROR, " " + e);
                        	Logger.log(IStatus.INFO, " " + e.getStackTrace());
                        }

                        try {
                            Logger.log(IStatus.INFO, " ReportInfo : " + ri);
                            IMarker marker = fileinfo.createMarker("cc.codechecker.markers" + "" +
                                    ".problemmarker");
                            marker.setAttribute(IMarker.LINE_NUMBER, (int) ri.getLastBugPathItem
                                    ().getStartPosition().getLine());
                            marker.setAttribute(IMarker.CHAR_START, (int) ri.getLastBugPathItem()
                                    .getStartPosition().getColumn());
                            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                            marker.setAttribute(IMarker.MESSAGE, ri.getCheckerMsg());
                            Logger.log(IStatus.INFO, " Marker line: " + marker.getAttribute(IMarker.LINE_NUMBER));
                        } catch (CoreException e) {
                            // TODO Auto-generated catch block
                        	Logger.log(IStatus.ERROR, " " + e);
                        	Logger.log(IStatus.INFO, " " + e.getStackTrace());
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onTotalCountAvailable(SearchJob arg0, SearchList arg1, int arg2) {
    }

}
