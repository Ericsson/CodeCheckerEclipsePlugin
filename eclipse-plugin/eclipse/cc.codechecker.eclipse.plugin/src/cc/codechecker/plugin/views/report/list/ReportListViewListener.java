package cc.codechecker.plugin.views.report.list;

import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;

import cc.codechecker.api.action.result.ReportInfo;
import cc.codechecker.api.job.report.list.SearchList;
import cc.codechecker.api.job.report.list.SearchJob;
import cc.codechecker.api.job.report.list.SearchListener;

public class ReportListViewListener implements SearchListener {


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
