/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultArtifactTest {

  private static final String SCOPE_COMPILE = "compile";
  private static final String RUNTIME_SCOPE = "runtime";
  private static final String PROJECT_ID = "groupId:artifactId:1.0.0";
  private static final String DEPENDENCY1_ID = "groupId1:artifactId1:1.0.0";
  private static final String DEPENDENCY2_ID = "groupId2:artifactId2:1.0.0";
  private DefaultArtifact defaultArtifact;
  private org.apache.maven.artifact.Artifact artifactMock;

  @Before
  public void setUp() throws Exception {
    artifactMock = mock(org.apache.maven.artifact.Artifact.class);
    defaultArtifact = new DefaultArtifact(artifactMock);
  }

  @Test
  public void getDependencyTrail() throws Exception {
    List dependencyTrail = unmodifiableList(new ArrayList());
    when(artifactMock.getDependencyTrail()).thenReturn(dependencyTrail);
    assertThat("Dependency trail is not the expected", defaultArtifact.getDependencyTrail(), equalTo(dependencyTrail));
    verify(artifactMock).getDependencyTrail();
  }

  @Test
  public void isOptionalTrue() throws Exception {
    when(artifactMock.isOptional()).thenReturn(true);
    assertThat("Method should have returned true", defaultArtifact.isOptional());
    verify(artifactMock).isOptional();
  }

  @Test
  public void isOptionalFalse() throws Exception {
    when(artifactMock.isOptional()).thenReturn(false);
    assertThat("Method should have returned false", !defaultArtifact.isOptional());
    verify(artifactMock).isOptional();
  }

  @Test
  public void isCompileScopeTrue() throws Exception {
    when(artifactMock.getScope()).thenReturn(SCOPE_COMPILE);
    assertThat("Method should have returned true", defaultArtifact.isCompileScope());
    verify(artifactMock).getScope();
  }

  @Test
  public void isCompileScopeFalse() throws Exception {
    when(artifactMock.getScope()).thenReturn(RUNTIME_SCOPE);
    assertThat("Method should have returned false", !defaultArtifact.isCompileScope());
    verify(artifactMock).getScope();
  }

  @Test
  public void isRuntimeScopeTrue() throws Exception {
    when(artifactMock.getScope()).thenReturn(RUNTIME_SCOPE);
    assertThat("Method should have returned true", defaultArtifact.isRuntimeScope());
    verify(artifactMock).getScope();
  }

  @Test
  public void isRuntimeScopeFalse() throws Exception {
    when(artifactMock.getScope()).thenReturn(SCOPE_COMPILE);
    assertThat("Method should have returned false", !defaultArtifact.isRuntimeScope());
    verify(artifactMock).getScope();
  }

  @Test
  public void getOnlyDependenciesTrailNoDependencies() throws Exception {
    List<String> dependencyTrail = new ArrayList<>();
    dependencyTrail.add(PROJECT_ID);
    when(artifactMock.getDependencyTrail()).thenReturn(dependencyTrail);
    assertThat("Result should be empty", defaultArtifact.getOnlyDependenciesTrail().isEmpty());
    verify(artifactMock).getDependencyTrail();
  }

  @Test
  public void getOnlyDependenciesMultipleDependencies() throws Exception {
    List<String> dependencyTrail = new ArrayList<>();
    dependencyTrail.add(PROJECT_ID);
    dependencyTrail.add(DEPENDENCY1_ID);
    dependencyTrail.add(DEPENDENCY2_ID);
    when(artifactMock.getDependencyTrail()).thenReturn(dependencyTrail);
    List<String> result = defaultArtifact.getOnlyDependenciesTrail();
    assertThat("Result should contain 2 dependencies", result.size(), equalTo(2));
    assertThat("Result should contain these dependencies", result, containsInAnyOrder(DEPENDENCY1_ID, DEPENDENCY2_ID));
    verify(artifactMock).getDependencyTrail();
  }

  @Test
  public void getFile() throws Exception {
    File artifactFile = new File(EMPTY);
    when(artifactMock.getFile()).thenReturn(artifactFile);
    assertThat("File is not the expected", defaultArtifact.getFile(), equalTo(artifactFile));
    verify(artifactMock).getFile();
  }

  @Test
  public void getGroupId() throws Exception {
    String groupId = "groupId";
    when(artifactMock.getGroupId()).thenReturn(groupId);
    assertThat("Group id is not the expected", defaultArtifact.getGroupId(), equalTo(groupId));

  }

}
