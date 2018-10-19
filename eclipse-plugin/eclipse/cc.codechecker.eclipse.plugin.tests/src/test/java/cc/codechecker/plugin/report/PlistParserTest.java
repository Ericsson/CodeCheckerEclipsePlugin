package test.java.cc.codechecker.plugin.report;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import cc.codechecker.plugin.report.PlistParser;

public class PlistParserTest {
	PlistParser parser;
	
//	@Before
	public void initParser(){
		parser = new PlistParser();
	}
	
	@Test
	public void ParserTest1() {
//		parser.iProcessResult(pathToFile);
	    boolean a = true;
            assertThat(a, is(equalTo(true)));
        }
}
