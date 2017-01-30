/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;

public class InitializeMojoTest extends AbstractMuleMojoTest {
    private static final String INITIALIZE_GOAL_DEBUG_MESSAGE = "[debug] Initializing Mule Maven Plugin...\n[debug] Mule Maven Plugin Initialize done\n";
    private static final String EXPECTED_STRUCTURE_RELATIVE_PATH = "/expected-initialize-structure";
    private AbstractMuleMojo mojo;

    @Before
    public void before() throws IOException {
        mojo = new InitializeMojo();
        projectMock = mock(MavenProject.class);
        buildMock = mock(Build.class);
    }

    @Test
    public void initializeWhenTargetFolderIsEmptyTest() throws MojoFailureException, MojoExecutionException, IOException {
        when(buildMock.getDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        mojo.project = projectMock;

        mojo.execute();

        File rootOfExpectedStructure = ResourceExtractor.simpleExtractResources(getClass(), EXPECTED_STRUCTURE_RELATIVE_PATH);
        assertThat("The target folder directory does not have the expected structure", temporaryFolder.getRoot(), hasSameTreeStructure(rootOfExpectedStructure));
        assertThat("Initialize goal message was not the expected", INITIALIZE_GOAL_DEBUG_MESSAGE, equalTo(outContent.toString()));

    }

    @Test
    public void initializeWhenTargetFolderAlreadyHasExpectedStructureTest() throws MojoFailureException, MojoExecutionException, IOException {
        temporaryFolder.newFolder(LIB);
        temporaryFolder.newFolder(META_INF + File.separator + MULE);
        temporaryFolder.newFolder(MULE);
        temporaryFolder.newFolder(PLUGINS);
        temporaryFolder.newFolder(TEST_MULE + File.separator + MUNIT);

        when(buildMock.getDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        mojo.project = projectMock;

        mojo.execute();

        File rootOfExpectedStructure = ResourceExtractor.simpleExtractResources(getClass(), EXPECTED_STRUCTURE_RELATIVE_PATH);
        assertThat("The target folder directory does not have the expected structure", temporaryFolder.getRoot(), hasSameTreeStructure(rootOfExpectedStructure));
        assertThat("Initialize goal message was not the expected", INITIALIZE_GOAL_DEBUG_MESSAGE, equalTo(outContent.toString()));
    }
}
