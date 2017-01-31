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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class InstallMojoTest extends MojoTest {
    private static final String INSTALL = "install";
    private static final String PROJECT_NAME = "empty-install-project";
    private static final String ORG_ID = "org.apache.maven.plugin.my.unit";
    private static final String NAME = "empty-install-project";
    private static final String VERSION = "1.0-SNAPSHOT";
    private static final String EXT = "zip";


    public InstallMojoTest() {
        super(PROJECT_NAME);
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
        verifier.deleteArtifact(ORG_ID, NAME, VERSION, EXT);
        verifier.assertArtifactNotPresent(ORG_ID, NAME, VERSION, EXT);

        verifier.executeGoal(INSTALL);

        verifier.verifyErrorFreeLog();
        verifier.assertArtifactPresent(ORG_ID, NAME, VERSION, EXT);
    }
}