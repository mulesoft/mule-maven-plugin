/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.resolver.visitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static util.ResolverTestHelper.buildVisitorSpies;
import static util.ResolverTestHelper.createProjectDependencyNodeSpy;

public class MulePluginResolverVisitorTest {

  private MulePluginResolverVisitor visitor;
  private ProjectDependencyNode nodeSpy;
  private List<DependencyNodeVisitor> visitorSpies;
  private static final int NUMBER_VISITORS = 10;
  private Set<ArtifactCoordinates> collectDependencies;

  @BeforeEach
  public void setUp() throws ValidationException {
    nodeSpy = createProjectDependencyNodeSpy();
    collectDependencies = newHashSet();
    visitorSpies = buildVisitorSpies(nodeSpy, collectDependencies, NUMBER_VISITORS);
    visitor = new MulePluginResolverVisitor(visitorSpies);
  }

  @Test
  public void visitTest() throws ValidationException {
    visitor.visit(nodeSpy);

    for (DependencyNodeVisitor visitorSpy : visitorSpies) {
      verify(visitorSpy, times(1)).visit(nodeSpy);
    }
  }

  @Test
  public void collectDependenciesTest() {
    assertThat(visitor.getCollectedDependencies()).describedAs("Collected dependencies is not the expected")
        .isEqualTo(collectDependencies);

    for (DependencyNodeVisitor visitorSpy : visitorSpies) {
      verify(visitorSpy, times(1)).getCollectedDependencies();
    }
  }
}
