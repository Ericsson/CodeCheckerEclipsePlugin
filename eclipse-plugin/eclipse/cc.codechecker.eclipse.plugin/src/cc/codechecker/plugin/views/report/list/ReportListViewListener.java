package cc.codechecker.plugin.views.report.list;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.widgets.Display;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import cc.codechecker.plugin.report.BugPathItem;
import cc.codechecker.plugin.report.BugPathItem.Position;
import cc.codechecker.plugin.report.ProblemInfo;
import cc.codechecker.plugin.report.ReportInfo;
import cc.codechecker.plugin.report.SearchList;
import cc.codechecker.plugin.report.SearchListener;

public class ReportListViewListener implements SearchListener {


    private final ReportListView target;

    public ReportListViewListener(ReportListView target) {
        this.target = target;
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onJobInternalError(RuntimeException arg1) {
    }

    @Override
    public void onStart(final Object job) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
            	// Invalidate target.
            	//target.changeModel(job.getResult());
            }
        });
    }

    @Override
    public void onTimeout() {}

    //TODO UPLIFT use arguments in function.
    @Override
    public void onPartsArrived(SearchList sl) {    
    	// append insted of replace model?
    	target.changeModel(sl);
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                target.refresh(null);
            }
        });
    }

    @Override
    public void onTotalCountAvailable(SearchList sl, int arg2) {
    	target.changeModel(sl);
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                target.refresh(null);
            }
        });
    }

}
