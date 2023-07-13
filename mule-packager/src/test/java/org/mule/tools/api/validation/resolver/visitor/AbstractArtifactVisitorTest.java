/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.resolver.visitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static util.ResolverTestHelper.buildDependencies;
import static util.ResolverTestHelper.buildProjectDependencyNodeSpies;
import static util.ResolverTestHelper.getChildVisitorMock;

public class AbstractArtifactVisitorTest {

  private AbstractArtifactVisitor visitor;
  private ProjectDependencyNode nodeMock;
  private Set<ProjectDependencyNode> dependenciesMock;

  @BeforeEach
  public void setUp() {
    visitor = spy(AbstractArtifactVisitor.class);
    nodeMock = spy(new ProjectDependencyNode(mock(Project.class), mock(ProjectBuilder.class)));
  }

  @Test
  public void visitTest() throws ValidationException {
    doNothing().when(visitor).collectDependencies(nodeMock);
    doNothing().when(visitor).visitChildren(nodeMock);

    visitor.visit(nodeMock);

    verify(visitor, times(1)).collectDependencies(nodeMock);
    verify(visitor, times(1)).visitChildren(nodeMock);
  }

  @Test
  public void visitChildrenTest() throws ValidationException {
    dependenciesMock = buildProjectDependencyNodeSpies(10);
    doReturn(dependenciesMock).when(nodeMock).getChildren(any());
    Set<ArtifactCoordinates> dependenciesCollected = buildDependencies(3);
    visitor.setChildVisitor(() -> getChildVisitorMock(dependenciesCollected));

    visitor.visitChildren(nodeMock);

    for (ProjectDependencyNode dependencyNode : dependenciesMock) {
      verify(dependencyNode, times(1)).accept(any());
    }

    assertThat(visitor.getCollectedDependencies()).describedAs("Collected dependencies is not the expected")
        .isEqualTo(dependenciesCollected);
  }
}
