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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.*;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.FileTreeMatcher;

public class InitializeMojoTest extends AbstractMuleMojoTest {

  private static final String INITIALIZE_GOAL_DEBUG_MESSAGE =
      "[debug] Initializing Mule Maven Plugin...\n[debug] Mule Maven Plugin Initialize done\n";
  private static final String EXPECTED_STRUCTURE_RELATIVE_PATH = "/expected-initialize-structure";
  private AbstractMuleMojo mojo;

  @Before
  public void before() throws IOException {
    mojo = new InitializeMojo();
    when(projectMock.getGroupId()).thenReturn(GROUP_ID);
    when(projectMock.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(buildMock.getDirectory()).thenReturn(projectRootFolder.getRoot().getAbsolutePath());
    mojo.project = projectMock;
  }

  @After
  public void after() throws IOException {
    File rootOfExpectedStructure = ResourceExtractor.simpleExtractResources(getClass(), EXPECTED_STRUCTURE_RELATIVE_PATH);
    assertThat("The target folder directory does not have the expected structure", projectRootFolder.getRoot(),
               FileTreeMatcher.hasSameTreeStructure(rootOfExpectedStructure));
    assertThat("Initialize goal message was not the expected", INITIALIZE_GOAL_DEBUG_MESSAGE, equalTo(outContent.toString()));
  }

  @Test
  public void initializeWhenTargetFolderIsEmptyTest() throws MojoFailureException, MojoExecutionException, IOException {
    mojo.execute();
  }

  @Test
  public void initializeWhenTargetFolderAlreadyHasSomeFoldersOfExpectedStructureTest()
      throws MojoFailureException, MojoExecutionException, IOException {
    projectRootFolder.newFolder(META_INF + File.separator + MULE);
    projectRootFolder.newFolder(MULE);
    projectRootFolder.newFolder(REPOSITORY);

    mojo.execute();
  }
}
