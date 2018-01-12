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
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static util.ResolverTestHelper.MULE_PLUGIN_CLASSIFIER;
import static util.ResolverTestHelper.buildGavToDependenciesMap;
import static util.ResolverTestHelper.stubBuildNodeMethod;

public class ProjectDependencyNodeTest {

  private ProjectDependencyNode nodeSpy;
  private ProjectBuilder builderMock;
  private Project projectMock;
  private Map<ArtifactCoordinates, ProjectDependencyNode> dependenciesMock;
  private int NUMBER_DEPENDENCIES = 10;
  private DependenciesFilter filterMock;

  @Before
  public void setUp() throws ValidationException {
    projectMock = mock(Project.class);
    builderMock = mock(ProjectBuilder.class);
    nodeSpy = spy(new ProjectDependencyNode(projectMock, builderMock));
    dependenciesMock = buildGavToDependenciesMap(NUMBER_DEPENDENCIES, MULE_PLUGIN_CLASSIFIER);
    doReturn((dependenciesMock.keySet())).when(nodeSpy).getDependencies(any());
    stubBuildNodeMethod(nodeSpy, dependenciesMock);
    filterMock = mock(DependenciesFilter.class);
  }

  @Test
  public void getChildrenTest() throws ValidationException {
    assertThat("List of artifact coordinates is not the expected", nodeSpy.getChildren(filterMock),
               equalTo(newHashSet(dependenciesMock.values())));
  }
}
