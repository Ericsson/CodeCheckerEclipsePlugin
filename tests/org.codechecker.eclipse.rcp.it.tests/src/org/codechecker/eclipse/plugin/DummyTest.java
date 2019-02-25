package org.codechecker.eclipse.plugin;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Dummy test for the integration test to be able to run.
 * TODO Remove this test case after the first real test gets added.
 */
public class DummyTest {
    /**
     * Test the save menu.
     */
    @Test
    public void ensureSaveIsDisabledWhenNothingIsDirty() {
        assertThat("True is true", true);
    }
}
