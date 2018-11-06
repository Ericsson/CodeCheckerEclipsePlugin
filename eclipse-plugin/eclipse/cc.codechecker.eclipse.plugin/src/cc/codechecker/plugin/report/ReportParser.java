package cc.codechecker.plugin.report;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Class is for parsing the reports folder created by Codechecker.
 * @author vodorok
 *
 */
public class ReportParser implements Runnable {
    private List<SearchListener> listeners = new ArrayList<SearchListener>();
    SearchList reports;
    String currentFileName;

    public ReportParser(SearchList reports, String currentFileName) {
        this.reports = reports;
        this.currentFileName = currentFileName;
    }

    //TODO javadoc!
    private void processReports(){
        //Parse The reports given the current filenam
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

    public void AddListener(SearchListener listener){
        listeners.add(listener);
    }

    @Override
    public void run() {
        processReports();
    }
}
