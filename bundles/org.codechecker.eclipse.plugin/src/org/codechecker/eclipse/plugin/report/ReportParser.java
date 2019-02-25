package org.codechecker.eclipse.plugin.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * This class is for filtering the reports in memory.
 *
 */
public class ReportParser implements Runnable {
    private List<SearchListener> listeners = new ArrayList<SearchListener>();
    private SearchList reports;
    private String currentFileName;

    /**
     *
     * @param reports The in memory representation of the reports.
     * @param currentFileName The filename {@link @Nullable}. If null there will be no filtering.
     */
    public ReportParser(SearchList reports, @Nullable String currentFileName) {
        this.reports = reports;
        this.currentFileName = currentFileName;
    }

    /**
     * Processes reports for displaying. If filename is null no report gets filtered.
     */
    public void processReports(){
        //Parse The reports given the current filename
        if (reports == null ) return;
        if (currentFileName.isEmpty() || currentFileName == null)
            for (SearchListener listener : listeners){
                listener.onTotalCountAvailable(reports, 1);
            }
        else {
            //TODO really needs a test for ensuring that all reports get coped over
            SearchList filteredReoports = new SearchList();
            for (String checker : reports.getCheckers()) 
                for (ReportInfo rep : reports.getReportsFor(checker))
                    if (Iterables.getLast(rep.getChildren().orNull().getItems()).getFile().equals(currentFileName)){
                        filteredReoports.addReports(ImmutableList.of(rep));
                        for (SearchListener listener : listeners){
                            listener.onPartsArrived(filteredReoports);
                        }
                    }
            for (SearchListener listener : listeners){
                listener.onTotalCountAvailable(filteredReoports, 1);
            }
        }
    }

    /**
     * Adds a {@link SearchListener} listener to this instance.
     * @param listener The listener to be added.
     */
    public void addListener(SearchListener listener){
        listeners.add(listener);
    }

    @Override
    public void run() {
        processReports();
    }
}
