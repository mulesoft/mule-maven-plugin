/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.resolver.visitor;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static util.ResolverTestHelper.buildDependencies;
import static util.ResolverTestHelper.createProjectDependencyNodeSpy;

public class MuleDomainVisitorTest {

  private static final int NUMBER_DEPENDENCIES = 10;
  private ProjectDependencyNode nodeSpy;
  private MuleDomainVisitor visitor = new MuleDomainVisitor();

  @Test
  public void collectDependenciesTest() throws ValidationException {
    nodeSpy = createProjectDependencyNodeSpy();
    Set<ArtifactCoordinates> dependencies = buildDependencies(NUMBER_DEPENDENCIES);
    doReturn(dependencies).when(nodeSpy).getDependencies(any());

    visitor.collectDependencies(nodeSpy);

    assertThat(visitor.getCollectedDependencies().isEmpty()).describedAs("Collected dependencies should be empty").isTrue();
  }
}
