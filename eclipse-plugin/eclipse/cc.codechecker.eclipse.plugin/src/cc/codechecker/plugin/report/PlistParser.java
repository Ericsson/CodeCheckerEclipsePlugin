package cc.codechecker.plugin.report;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import com.dd.plist.PropertyListParser;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSArray;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.NSNumber;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import cc.codechecker.plugin.report.SearchListener;
/**
 * Class for parsing the analysis result plist files.
 */
public class PlistParser {
    private static Logger logger;

    private List<SearchListener> listeners;

    public PlistParser() {
        logger = Logger.getLogger(PlistParser.class.getName());
        listeners = new ArrayList<>();
    } 
    
    public void AddListener(SearchListener listener){
    	listeners.add(listener);
    }

    /** 
     * Parses exactly one .plist file.
     * @param pathToFile The file to be parsed.
     * @throws Exception 
     */ 
    public void ProcessResult(String pathToFile) throws Exception{

            NSDictionary dict = (NSDictionary)PropertyListParser.parse("/home/vodorok/Test.cpp.plist");

            NSObject[] sourceFiles = ((NSArray)dict.objectForKey("files")).getArray();
            
            NSObject[] diagnostics = ((NSArray)dict.objectForKey("diagnostics")).getArray();
            
            for(NSObject diagnostic:diagnostics){
                String checkerName = ((NSString)((NSDictionary) diagnostic).get("check_name")).getContent();
            	for (SearchListener listener : listeners){
            		listener.onPartsArrived(checkerName, null);
            	}
            }
            System.out.println(sourceFiles.toString());
    }
}
