package org.codechecker.eclipse.plugin.report;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.codechecker.eclipse.plugin.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.xml.sax.SAXException;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.codechecker.eclipse.plugin.report.BugPathItem.Position;

/**
 * Class for parsing the analysis result plist files.
 */
public class PlistParser {
    IProject project;

    public PlistParser(IProject project) {
        this.project = project;
    }

    /**
     * Parses exactly one .plist file.
     * @param pathToFile  The file to be parsed.
     */
    public SearchList parsePlist(File file, SearchList sl) {
        List<ReportInfo> riList = new ArrayList<>();

        NSDictionary dict;
        try {
            dict = (NSDictionary) PropertyListParser.parse(file);
            NSObject[] sourceFiles = ((NSArray) dict.objectForKey("files")).getArray();
            NSObject[] diagnostics = ((NSArray) dict.objectForKey("diagnostics")).getArray();

            for (NSObject diagnostic : diagnostics) {
                NSDictionary diag = (NSDictionary) diagnostic;
                String checkerName = ((NSString) diag.get("check_name")).getContent();
                String description = ((NSString) diag.get("description")).getContent();

                NSObject[] path = ((NSArray) diag.objectForKey("path")).getArray();

                List<BugPathItem> bugPathItemList = new ArrayList<>();

                for (NSObject bp : path) {
                    NSDictionary bugPath = (NSDictionary) bp;

                    // We are only interested in bug events
                    if (((NSString) bugPath.get("kind")).getContent().equals("event")) {
                        String message = ((NSString) bugPath.get("message")).getContent();
                        NSDictionary location = (NSDictionary) bugPath.get("location");
                        Integer fileIndex = ((NSNumber) location.get("file")).intValue();
                        Integer line = ((NSNumber) location.get("line")).intValue();
                        Integer col = ((NSNumber) location.get("col")).intValue();
                        String filePath = ((NSString) sourceFiles[fileIndex]).getContent();

                        BugPathItem bItem = new BugPathItem(new Position(line, col), new Position(line, col), message,
                                filePath);
                        bugPathItemList.add(bItem);
                    }
                }

                ProblemInfo pInfo = new ProblemInfo(ImmutableList.copyOf(bugPathItemList));

                riList.add(new ReportInfo(checkerName, "testHash", Iterables.getLast(bugPathItemList).getFile(),
                            description, 1, false, "testFile", Iterables.getLast(bugPathItemList), Optional.of(pInfo)));
            }

            ImmutableList<ReportInfo> uriList = ImmutableList.copyOf(riList);
            sl.addReports(uriList);

        } catch (ParserConfigurationException | ParseException | SAXException | PropertyListFormatException
                | IOException e) {
            // TODO Auto-generated catch block
            Logger.log(IStatus.ERROR, "Cannot Parse File :" + e.getMessage() + " in file: " + file.getName());
            //e.printStackTrace();
                }

        return sl;
    }

    // TODO javadoc!, and return parse results for storing them
    public SearchList processResultsForProject() {
        // TODO Get This
        String ws = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/.codechecker/"
            + project.getName() + "/results";
        Logger.log(IStatus.INFO, "Parsing plists in :" + ws);
        File file = new File(ws);
        SearchList sl = new SearchList();
        if (file.exists()) {
            for (File f : file.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name.toLowerCase().endsWith(".plist"))
                        return true;
                    return false;
                }
            })) {
                //Logger.log(IStatus.INFO, "Parsing plist :" + f);
                parsePlist(f, sl);
            }
        }
        return sl;
    }
}
