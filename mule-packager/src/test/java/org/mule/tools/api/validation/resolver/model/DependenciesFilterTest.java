/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.resolver.model;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.ResolverTestHelper.*;

public class DependenciesFilterTest {

  private DependenciesFilter filter;
  private ArtifactCoordinates artifactCoordinates_a;
  private ArtifactCoordinates artifactCoordinates_b;
  private ProjectDependencyNode nodeMock;
  private ArrayList<ArtifactCoordinates> projectMockDependencies;
  private Project projectMock;

  @Before
  public void setUp() {
    filter = new DependenciesFilter();
    artifactCoordinates_a = createDependency(GROUP_ID, ARTIFACT_ID_A, VERSION, TYPE_A, CLASSIFIER_A, SCOPE_A);
    artifactCoordinates_b = createDependency(GROUP_ID, ARTIFACT_ID_B, VERSION, TYPE_B, CLASSIFIER_B, SCOPE_B);
    projectMock = mock(Project.class);
    nodeMock = new ProjectDependencyNode(projectMock, mock(ProjectBuilder.class));
    projectMockDependencies = newArrayList(artifactCoordinates_a, artifactCoordinates_b);
    when(projectMock.getDirectDependencies()).thenReturn(projectMockDependencies);
  }

  @Test
  public void hasClassifierTrueTest() {
    assertThat("Predicate should return true", filter.hasClassifier(CLASSIFIER_A).test(artifactCoordinates_a), equalTo(true));
  }

  @Test
  public void hasClassifierFalseTest() {
    assertThat("Predicate should return false", filter.hasClassifier(CLASSIFIER_B).test(artifactCoordinates_a), equalTo(false));
  }

  @Test
  public void hasClassifierNullTest() {
    assertThat("Predicate should return false", filter.hasClassifier(null).test(artifactCoordinates_a), equalTo(false));
  }

  @Test
  public void hasTypeTrueTest() {
    assertThat("Predicate should return true", filter.hasType(TYPE_A).test(artifactCoordinates_a), equalTo(true));
  }

  @Test
  public void hasTypeFalseTest() {
    assertThat("Predicate should return false", filter.hasClassifier(TYPE_B).test(artifactCoordinates_a), equalTo(false));
  }

  @Test
  public void hasTypeNullTest() {
    assertThat("Predicate should return false", filter.hasClassifier(null).test(artifactCoordinates_a), equalTo(false));
  }

  @Test
  public void hasScopeTrueTest() {
    assertThat("Predicate should return true", filter.hasScope(SCOPE_A).test(artifactCoordinates_a), equalTo(true));
  }

  @Test
  public void hasScopeFalseTest() {
    assertThat("Predicate should return false", filter.hasScope(SCOPE_B).test(artifactCoordinates_a), equalTo(false));
  }

  @Test
  public void hasScopeNullTest() {
    assertThat("Predicate should return false", filter.hasScope(null).test(artifactCoordinates_a), equalTo(false));
  }

  @Test
  public void filterOneJarTest() {
    filter = new DependenciesFilter(CLASSIFIER_A, SCOPE_A);
    artifactCoordinates_a.setType(JAR_TYPE);
    assertThat("nodeMockDependencies should have 1 element", filter.filter(nodeMock).size(), equalTo(1));
  }

  @Test
  public void filterNoJarTypeTest() {
    filter = new DependenciesFilter(CLASSIFIER_A, SCOPE_A);
    artifactCoordinates_a.setType(TYPE_A);
    assertThat("nodeMockDependencies should be empty", filter.filter(nodeMock).isEmpty(), is(true));
  }
}
