/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.resolver.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.muleclassloader.model.ArtifactCoordinates;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
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

  @BeforeEach
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
    assertThat(filter.hasClassifier(CLASSIFIER_A).test(artifactCoordinates_a)).describedAs("Predicate should return true")
        .isTrue();
  }

  @Test
  public void hasClassifierFalseTest() {
    assertThat(filter.hasClassifier(CLASSIFIER_B).test(artifactCoordinates_a)).describedAs("Predicate should return false")
        .isFalse();
  }

  @Test
  public void hasClassifierNullTest() {
    assertThat(filter.hasClassifier(null).test(artifactCoordinates_a)).describedAs("Predicate should return false").isFalse();
  }

  @Test
  public void hasTypeTrueTest() {
    assertThat(filter.hasType(TYPE_A).test(artifactCoordinates_a)).describedAs("Predicate should return true").isTrue();
  }

  @Test
  public void hasTypeFalseTest() {
    assertThat(filter.hasClassifier(TYPE_B).test(artifactCoordinates_a)).describedAs("Predicate should return false").isFalse();
  }

  @Test
  public void hasTypeNullTest() {
    assertThat(filter.hasClassifier(null).test(artifactCoordinates_a)).describedAs("Predicate should return false").isFalse();
  }

  @Test
  public void hasScopeTrueTest() {
    assertThat(filter.hasScope(SCOPE_A).test(artifactCoordinates_a)).describedAs("Predicate should return true").isTrue();
  }

  @Test
  public void hasScopeFalseTest() {
    assertThat(filter.hasScope(SCOPE_B).test(artifactCoordinates_a)).describedAs("Predicate should return false").isFalse();
  }

  @Test
  public void hasScopeNullTest() {
    assertThat(filter.hasScope(null).test(artifactCoordinates_a)).describedAs("Predicate should return false").isFalse();
  }

  @Test
  public void filterOneJarTest() {
    filter = new DependenciesFilter(CLASSIFIER_A, SCOPE_A);
    artifactCoordinates_a.setType(JAR_TYPE);
    assertThat(filter.filter(nodeMock).size()).describedAs("nodeMockDependencies should have 1 element").isEqualTo(1);
  }

  @Test
  public void filterNoJarTypeTest() {
    filter = new DependenciesFilter(CLASSIFIER_A, SCOPE_A);
    artifactCoordinates_a.setType(TYPE_A);
    assertThat(filter.filter(nodeMock).isEmpty()).describedAs("nodeMockDependencies should be empty").isTrue();
  }
}
