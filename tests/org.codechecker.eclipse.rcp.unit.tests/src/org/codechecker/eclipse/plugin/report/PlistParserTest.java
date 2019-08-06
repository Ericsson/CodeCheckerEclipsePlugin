package org.codechecker.eclipse.plugin.report;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.eclipse.core.resources.IProject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class PlistParserTest {
    private PlistParser parser;
    private IProject proj = mock(IProject.class);

    @Before
    public void initParser(){
        parser = new PlistParser(proj);
    }

    @Test
    public void ParserTest1() {

        Path file = null;
        try {
            file = Utils.loadFileFromBundle("org.codechecker.eclipse.rcp.unit.tests",
                    Utils.RES + "plists/test_plist_1.plist");
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        assertThat(file.toFile().exists(), is(equalTo(true)));

        SearchList sl = new SearchList();
        parser.parsePlist(file.toFile(), sl);
        assertThat(sl.getCheckers(), hasItem("alpha.core.SizeofPtr"));
    }
}
