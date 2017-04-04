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

public class GenerateSourcesMojoTest extends MojoTest {
    private static final String GENERATE_SOURCES = "generate-sources";
    private static final String EXPECTED_STRUCTURE_RELATIVE_PATH = "/expected-generate-sources-structure";
    private static final String EXPECTED_SMART_CONNECTOR_STRUCTURE_RELATIVE_PATH = "/expected-smart-connector-dependency-structure";

    public GenerateSourcesMojoTest() {
        super("empty-generate-sources-project");
    }

    @Before
    public void before() throws IOException, VerificationException {
        initializeContext();
        clearResources();
    }

    @After
    public void after() {
        verifier.resetStreams();
    }

    @Test
    public void testGenerateSources() throws IOException, VerificationException {
        verifier.executeGoal(GENERATE_SOURCES);

        File expectedStructure = ResourceExtractor.simpleExtractResources(getClass(), EXPECTED_STRUCTURE_RELATIVE_PATH);

        assertThat("The directory structure is different from the expected", targetFolder, hasSameTreeStructure(expectedStructure));

        verifier.verifyErrorFreeLog();
    }
}
