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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.MUNIT;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.TEST_MULE;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;

public class GenerateTestSourcesMojoTest extends AbstractMuleMojoTest {

  private static final String EXPECTED_EXCEPTION_MESSAGE_FAIL_GENERATE_SOURCES = "Fail to generate sources";
  private GenerateTestSourcesMojo mojo;

  @Before
  public void before() throws IOException {
    testMuleFolder = buildTemporaryFolder.newFolder(TEST_MULE);
    munitFolder = new File(testMuleFolder.getAbsolutePath(), MUNIT);
    munitFolder.mkdir();

    munitSourceFolder = projectRootFolder.getRoot();

    mojo = new GenerateTestSourcesMojo();
    mojo.munitSourceFolder = munitSourceFolder;
    mojo.project = projectMock;
  }

  @Test
  public void createTestMuleFolderContentWithoutTestThrowsExceptionTest()
      throws IOException, MojoFailureException, MojoExecutionException {
    expectedException.expect(MojoFailureException.class);
    expectedException.expectMessage(EXPECTED_EXCEPTION_MESSAGE_FAIL_GENERATE_SOURCES);

    when(buildMock.getDirectory()).thenReturn(projectRootFolder.getRoot().getAbsolutePath());
    when(projectMock.getBuild()).thenReturn(buildMock);

    mojo.execute();
  }

  @Test
  public void createTestMuleFolderContentTest() throws IOException, MojoFailureException, MojoExecutionException {
    when(buildMock.getDirectory()).thenReturn(buildTemporaryFolder.getRoot().getAbsolutePath());
    when(projectMock.getBuild()).thenReturn(buildMock);
    File munitTestFile = projectRootFolder.newFile(MUNIT_TEST_FILE_NAME);

    mojo.execute();

    File[] filesInMunitFolder = munitFolder.listFiles();
    assertThat("The munit folder should contain only one file", filesInMunitFolder.length == 1 && filesInMunitFolder[0].isFile());
    assertThat("The file in the munit folder is not the expected", filesInMunitFolder[0].getName(),
               equalTo(munitTestFile.getName()));
  }
}
