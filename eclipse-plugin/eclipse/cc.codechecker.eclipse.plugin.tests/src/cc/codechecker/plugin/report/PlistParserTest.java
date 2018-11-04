package cc.codechecker.plugin.report;

import org.junit.Test;
import org.junit.Before;
import com.google.common.base.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;

import cc.codechecker.plugin.report.PlistParser;

public class PlistParserTest {
    private PlistParser parser;

    @Before
    public void initParser(){
        parser = new PlistParser();
    }

    /*@Test
    public void ParserTest1() {
        ClassLoader cl = this.getClass().getClassLoader();
        File file = new File(cl.getResource("plists/test_plist_1.plist").getFile());
        assertThat(file.exists(), is(equalTo(true)));
        SearchList sl = new SearchList();
        //parser.parsePlist(file, sl);
        try {
            parser.parsePlist(file, sl, "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertThat(sl.getTotalReportCount().orNull(), is(equalTo(null)));
    }

    @Test
    public void ParserTest2() {

    }*/

    // com.google.guava;version="26.0-jre",
    ///		 org.apache.commons;version="1.3"
}
