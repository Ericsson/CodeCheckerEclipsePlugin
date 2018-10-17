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
    public void onPartsArrived(Object arg1, ImmutableList<ReportInfo> arg2) {    	
    	// refresh model model also
    	List<ReportInfo> riList = new ArrayList<>();
    	BugPathItem bItem = new BugPathItem(new Position(1, 1), new Position(1, 2), "testBugpathItem_1","/home/vodorok/runtime-EclipseApplication/Test/src/Test.cpp");
    	BugPathItem bItem2 = new BugPathItem(new Position(2, 1), new Position(2, 2), "testBugpathItem_2","/home/vodorok/runtime-EclipseApplication/Test/src/Test.cpp");
    	ProblemInfo pInfo = new ProblemInfo(ImmutableList.of(bItem, bItem2));
    	
    	riList.add(new ReportInfo((String)arg1, "testHash", "/home/vodorok/runtime-EclipseApplication/Test/src/Test.cpp", "testCheckerMsg", 1, false, "testFile", 
    			bItem, Optional.of(pInfo)));

    	SearchList sl = new SearchList();

    	ImmutableList<ReportInfo> uriList = ImmutableList.copyOf(riList);
    	sl.addReports(uriList);
    	
    	target.changeModel(sl);
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                target.refresh(null);
            }
        });
    }

    @Override
    public void onTotalCountAvailable(SearchList arg1, int arg2) {
    }

}
