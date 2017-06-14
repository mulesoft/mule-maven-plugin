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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.maven.mojo.model.SharedLibraryDependency;
import org.mule.tools.api.packager.Validator;

public class ValidateMojoTest extends AbstractMuleMojoTest {

  private static final String VALIDATE_SHARED_LIBRARIES_MESSAGE =
      "The mule application does not contain the following shared libraries: ";

  private ValidateMojo mojo = new ValidateMojo();

  @Before
  public void before() {}

  @Test
  public void validateGoalSucceedTest() throws MojoFailureException, MojoExecutionException, IOException {
    mojo = mock(ValidateMojo.class);
    mojo.project = projectMock;

    Log logMock = mock(Log.class);
    doReturn(logMock).when(mojo).getLog();

    Validator validatorMock = mock(Validator.class);
    doReturn(validatorMock).when(mojo).getValidator();

    doNothing().when(mojo).validateSharedLibraries();
    doNothing().when(mojo).validateMulePluginDependencies();

    doCallRealMethod().when(mojo).execute();
    mojo.execute();

    verify(mojo, times(1)).getValidator();
    verify(mojo, times(1)).validateSharedLibraries();
    verify(mojo, times(1)).validateMulePluginDependencies();
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
}
