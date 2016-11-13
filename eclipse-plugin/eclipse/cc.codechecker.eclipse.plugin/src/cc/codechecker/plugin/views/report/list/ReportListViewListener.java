package cc.codechecker.plugin.views.report.list;

import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;

import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.api.job.report.list.SearchJob;
import cc.codechecker.api.job.report.list.SearchListener;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

public class ReportListViewListener implements SearchListener {

    //Logger
    private static final Logger logger = LogManager.getLogger(ReportListViewListener.class);

    private final ReportListView target;

    public ReportListViewListener(ReportListView target) {
        this.target = target;
    }

    @Override
    public void onJobComplete(SearchJob arg0) {
    }

    @Override
    public void onJobInternalError(SearchJob arg0, RuntimeException arg1) {
    }

    @Override
    public void onJobStart(final SearchJob job) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                target.changeModel(job.getResult());
            }
        });
    }

    @Override
    public void onJobTimeout(SearchJob arg0) {
    }

    @Override
    public void onPartsArrived(SearchJob arg0, SearchList arg1, ImmutableList<ReportInfo> arg2) {
        logger.log(Level.INFO, "SERVER_GUI_MSG >> Report size: " + arg2.size());
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                target.refresh(null);
            }
        });
    }

    @Override
    public void onTotalCountAvailable(SearchJob arg0, SearchList arg1, int arg2) {
    }

}
