package org.codechecker.eclipse.plugin.runtime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codechecker.eclipse.plugin.config.CcConfiguration;
import org.codechecker.eclipse.plugin.config.CcConfigurationBase;
import org.codechecker.eclipse.plugin.config.Config.ConfigTypes;
import org.codechecker.eclipse.plugin.config.project.CodeCheckerProject;
import org.codechecker.eclipse.rcp.shared.utils.Utils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for CodeCheckEnvironmentChecker class.
 */
public class CodeCheckEnvironmentCheckerTest {

    private static final String CODECHECKER = "CodeChecker";

    /**
     * The list of checkers that the stub Codechecker must return.
     */
    @SuppressWarnings("serial")
    private static final Set<String> CHECKERS = new HashSet<String>() {{
            add("core.uninitialized.Assign");
            add("cplusplus.NewDeleteLeaks");
            add("cplusplus.NewDelete");
            add("cppcoreguidelines-no-malloc");
            add("unix.Malloc");
            add("unix.MallocSizeof");
    }};

    private Map<ConfigTypes,String> config;
    private CodeCheckEnvironmentChecker ccec;

    /**
     * Check simple getCheckerList() commands succeeds.
     */
    @Test
    public void codeCheckerCheckersCommandSucceds() {
        // initialize the environment with CodeChecker installed in a "safe" directory
        initEnvirCheck(CODECHECKER);
        String checkers = ccec.getCheckerList();
        assertThat(CHECKERS.stream().allMatch(str -> checkers.contains(str)), is(true));
    }

    /**
     * Check more complex getCheckerList() commands succeeds.
     */
    @Test
    public void codeCheckerCheckersCommandSuccedsComplex() {
        // initialize the environment with CodeChecker installed in a "unsafe" directory
        // (contains spaces and parenthesis)
        initEnvirCheck("Code Che(c)ker");
        String checkers = ccec.getCheckerList();
        assertThat(CHECKERS.stream().allMatch(str -> checkers.contains(str)), is(true));
    }

    /**
     * Initializes the configuration map and the environment checker.
     * 
     * @param dir
     *            The directory where CodeChecker runnable will be placed
     */
    private void initEnvirCheck(String dir) {
        Path testCCRoot = Utils.prepareCodeChecker(dir);
        config = new HashMap<>();
        config.put(ConfigTypes.CHECKER_PATH, testCCRoot.toString());

        // Use mockito for mocking CodeCheckerProject and CcConfiguration
        CodeCheckerProject cProj = mock(CodeCheckerProject.class);
        CcConfigurationBase configurationBase = mock(CcConfiguration.class);
        // Prepare a sufficent configuration. For getting the checkers, a valid
        // codechecker location is needed.
        @SuppressWarnings("serial")
        Map<ConfigTypes, String> config = new HashMap<ConfigTypes, String>() {{
                put(ConfigTypes.CHECKER_PATH, testCCRoot.toString());
        }};
        // Mock out the calls inside the Mocked classes.
        when(configurationBase.get()).thenReturn(config);
        when(cProj.getCurrentConfig()).thenReturn(configurationBase);
        when(cProj.getLogFileLocation()).thenReturn(Paths.get("dummyPath", "inside"));

        //Initialize the EnvironmentChecker with the prepared instance
        ccec = new CodeCheckEnvironmentChecker(cProj);
    }

}
