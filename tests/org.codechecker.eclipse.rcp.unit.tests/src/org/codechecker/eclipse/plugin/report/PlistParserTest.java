package org.codechecker.eclipse.plugin.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.junit.Before;
import org.junit.Test;

import utils.UrlFileLoader;

public class PlistParserTest {
    private PlistParser parser;
    private IProject proj = mock(IProject.class);

    @Before
    public void initParser(){
        parser = new PlistParser(proj);
    }

    @Test
    public void ParserTest1() {

        File file = UrlFileLoader.getFileFromUrl("plists", "test_plist_1", "plist");
        assertThat(file.exists(), is(equalTo(true)));

        SearchList sl = new SearchList();
        parser.parsePlist(file, sl);
        assertThat(sl.getCheckers(), hasItem("alpha.core.SizeofPtr"));
    }

    /*@Test
    public void ParserTest2() {

    }*/
}
