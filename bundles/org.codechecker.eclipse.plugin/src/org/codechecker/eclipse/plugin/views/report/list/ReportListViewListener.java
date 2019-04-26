package org.codechecker.eclipse.plugin.views.report.list;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.swt.widgets.Display;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.codechecker.eclipse.plugin.report.BugPathItem;
import org.codechecker.eclipse.plugin.report.BugPathItem.Position;
import org.codechecker.eclipse.plugin.report.ProblemInfo;
import org.codechecker.eclipse.plugin.report.ReportInfo;
import org.codechecker.eclipse.plugin.report.SearchList;
import org.codechecker.eclipse.plugin.report.SearchListener;

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
