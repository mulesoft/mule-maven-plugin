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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;
import static org.hamcrest.MatcherAssert.assertThat;


public class PackageMojoTest extends MojoTest {
    private static final String PACKAGE = "package";

    public PackageMojoTest() {
        this.goal = PACKAGE;
    }

    @Before
    public void before() throws IOException, VerificationException {
        clearResources();
    }

    @Test
    public void testPackage() throws IOException, VerificationException {
        verifier.executeGoal(PACKAGE);

        File expectedStructure = getExpectedStructure();

        assertThat("The directory structure is different from the expected", targetFolder, hasSameTreeStructure(expectedStructure));

        verifier.verifyErrorFreeLog();
    }
}
