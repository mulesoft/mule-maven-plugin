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

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.mojo.model.SharedLibraryDependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ValidateMojoTest extends AbstractMuleMojoTest {

  private static final ValidateMojo mojo = new ValidateMojo();

  private static final String VALIDATE_GOAL_DEBUG_MESSAGE =
      "[debug] Validating Mule application...\n[debug] Validating Mule application done\n";
  private static final String VALIDATE_MANDATORY_FOLDERS_MESSAGE =
      "Invalid Mule project. Missing src/main/mule folder. This folder is mandatory";
  private static final String VALIDATE_SHARED_LIBRARIES_MESSAGE =
      "The mule application does not contain the following shared libraries: ";

  @Before
  public void before() throws IOException, MojoExecutionException {
    mojo.mainFolder = muleSourceFolderMock;
    mojo.projectBaseFolder = projectRootFolder.getRoot();
    when(resolverMock.resolveMulePlugins(any())).thenReturn(Collections.emptyList());
  }

  @Test
  public void validateMandatoryFoldersFailsWhenMuleSourceFolderDoesNotExistTest()
      throws MojoFailureException, MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(VALIDATE_MANDATORY_FOLDERS_MESSAGE);
    File sourceFolder = projectRootFolder.newFolder("src");
    sourceFolder.mkdir();
    File mainFolder = new File(sourceFolder, "main");
    mainFolder.mkdir();
    mojo.mainFolder = mainFolder;
    mojo.project = projectMock;
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);
    when(muleSourceFolderMock.exists()).thenReturn(false);

    mojo.execute();
  }

  @Test
  public void validateGoalSucceedTest() throws MojoFailureException, MojoExecutionException, IOException {
    when(muleSourceFolderMock.exists()).thenReturn(true);
    projectRootFolder.newFile(MULE_APPLICATION_JSON);

    ValidateMojo mojo = new ValidateMojoWithMockedResolverAndValidate();
    File sourceFolder = projectRootFolder.newFolder("src");
    sourceFolder.mkdir();
    File mainFolder = new File(sourceFolder, "main");
    mainFolder.mkdir();
    File muleFolder = new File(mainFolder, "mule");
    muleFolder.mkdir();
    mojo.mainFolder = projectRootFolder.newFolder("src/main");
    mojo.mainFolder.mkdirs();
    mojo.projectBaseFolder = projectRootFolder.getRoot();

    when(projectMock.getDependencies()).thenReturn(new ArrayList<>());
    mojo.project = projectMock;
    mojo.sharedLibraries = new ArrayList<>();

    mojo.execute();

    verify(resolverMock, times(1)).resolveMulePlugins(any());
    verify(validatorMock, times(1)).validate(anyList());
    assertThat("Validate goal message was not the expected", VALIDATE_GOAL_DEBUG_MESSAGE, equalTo(outContent.toString()));
  }

  @Test
  public void validateNoSharedLibrariesInDependenciesTest() throws MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);

    when(projectMock.getDependencies()).thenReturn(new ArrayList<>());
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);
    mojo.project = projectMock;

    mojo.sharedLibraries = new ArrayList<>();
    mojo.sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID, ARTIFACT_ID));

    mojo.validateSharedLibraries();

    assertThat("Validate goal message was not the expected", VALIDATE_SHARED_LIBRARIES_MESSAGE + mojo.sharedLibraries.toString(),
               equalTo(outContent.toString()));
  }

  @Test
  public void validateSharedLibrariesInDependenciesTest() throws MojoExecutionException {

    SharedLibraryDependency sharedLibraryDependencyB = new SharedLibraryDependency();
    sharedLibraryDependencyB.setArtifactId(ARTIFACT_ID + "-b");
    sharedLibraryDependencyB.setGroupId(GROUP_ID + "-b");

    mojo.sharedLibraries = new ArrayList<>();
    mojo.sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID + "-a", ARTIFACT_ID + "-a"));
    mojo.sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID + "-b", ARTIFACT_ID + "-b"));

    List<Dependency> projectDependencies = new ArrayList<>();
    projectDependencies.add(buildDependency(GROUP_ID + "-a", ARTIFACT_ID + "-a"));
    projectDependencies.add(buildDependency(GROUP_ID + "-b", ARTIFACT_ID + "-b"));
    projectDependencies.add(buildDependency(GROUP_ID + "-c", ARTIFACT_ID + "-c"));

    when(projectMock.getDependencies()).thenReturn(projectDependencies);
    mojo.project = projectMock;

    mojo.validateSharedLibraries();
  }

  private SharedLibraryDependency buildSharedLibraryDependency(String groupId, String artifactId) {
    SharedLibraryDependency sharedLibraryDependency = new SharedLibraryDependency();
    sharedLibraryDependency.setArtifactId(artifactId);
    sharedLibraryDependency.setGroupId(groupId);
    return sharedLibraryDependency;
  }

  private Dependency buildDependency(String groupId, String artifactId) {
    Dependency dependency = new Dependency();
    dependency.setGroupId(groupId);
    dependency.setArtifactId(artifactId);
    return dependency;
  }

  private class ValidateMojoWithMockedResolverAndValidate extends ValidateMojo {

    @Override
    protected void initializeResolver() {
      this.resolver = resolverMock;
    }

    @Override
    protected void initializeValidator() {
      this.validator = validatorMock;
    }
  }
}
