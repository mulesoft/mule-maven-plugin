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

import org.junit.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static util.ResolverTestHelper.buildDependencies;
import static util.ResolverTestHelper.createProjectDependencyNodeSpy;

public class MulePluginVisitorTest {

  private static final int NUMBER_DEPENDENCIES = 10;
  private ProjectDependencyNode nodeSpy;
  private MulePluginVisitor visitor = new MulePluginVisitor();

  @Test
  public void collectDependenciesTest() throws ValidationException {
    nodeSpy = createProjectDependencyNodeSpy();
    Set<ArtifactCoordinates> dependencies = buildDependencies(NUMBER_DEPENDENCIES);
    doReturn(dependencies).when(nodeSpy).getDependencies(any());

    visitor.collectDependencies(nodeSpy);

    assertThat("Collected dependencies is not the expected", visitor.getCollectedDependencies(), equalTo(dependencies));
  }

}
