/**
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;

public class InitializeMojoTest extends MojoTest {
    private static final String PROJECT_BASE_DIR_PROPERTY = "project.basedir";
    private static final String INITIALIZE = "initialize";
    private static final String EXPECTED_STRUCTURE_RELATIVE_PATH = "/expected-validate-structure";
    private static final String PROJECT_BUILD_DIRECTORY_PROPERTY = "project.build.directory";

    public InitializeMojoTest() {
        super("initialize-goal-project");
    }

    @Before
    public void before() throws IOException, VerificationException {
        initializeContext();
        clearResources();
        verifier.setSystemProperty(PROJECT_BASE_DIR_PROPERTY, projectBaseDirectory.getAbsolutePath());
        verifier.setSystemProperty(PROJECT_BUILD_DIRECTORY_PROPERTY, targetFolder.getAbsolutePath());
    }

    @After
    public void after() {
        verifier.resetStreams();
    }
    @Test
    public void testInitializeOnEmptyProject()
            throws Exception {
        verifier.executeGoal(INITIALIZE);

        File expectedStructure = ResourceExtractor.simpleExtractResources(getClass(), EXPECTED_STRUCTURE_RELATIVE_PATH);

        assertThat("The directory structure is different from the expected", targetFolder, hasSameTreeStructure(expectedStructure));

        verifier.verifyErrorFreeLog();
    }
}
