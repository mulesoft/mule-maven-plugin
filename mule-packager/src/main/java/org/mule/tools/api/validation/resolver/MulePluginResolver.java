/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation.resolver;


import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;
import org.mule.tools.api.validation.resolver.visitor.DependencyNodeVisitor;
import org.mule.tools.api.validation.resolver.visitor.MulePluginResolverVisitor;

/**
 * Resolve all mule plugin direct and transitive dependencies of a mule project.
 *
 */
public class MulePluginResolver {

  /**
   * The {@code MulePluginResolverVisitor} that is going to collect the mule plugins during the search.
   */
  private final DependencyNodeVisitor visitor;
  private ProjectDependencyNode dependencyRoot;

  /**
   * Creates a new instance with the default visitor {@link MulePluginResolverVisitor}
   * 
   * @param builder A builder for the project
   * @param project The project which mule plugin dependencies are going to be resolved.
   */
  public MulePluginResolver(ProjectBuilder builder, Project project) {
    this(new ProjectDependencyNode(project, builder), new MulePluginResolverVisitor());
  }

  /**
   * @param node A node representing the project from which the mule plugins are going to be resolved
   * @param visitor A visitor for the project dependencies
   */
  public MulePluginResolver(ProjectDependencyNode node, DependencyNodeVisitor visitor) {
    this.dependencyRoot = node;
    this.visitor = visitor;
  }

  /**
   * Retrieves all of the direct and transitive mule plugin project dependencies. This is performed by a
   * {@code MulePluginResolverVisitor} that searches for mule plugins that are direct and transitive dependencies of other mule
   * plugins or mule domains in the dependency graph.
   * 
   * @return A list of mule plugins that are either direct or transitive project dependencies
   * @throws ValidationException in case it cannot find the dependencies of a project node
   */
  public List<ArtifactCoordinates> resolve() throws ValidationException {
    dependencyRoot.accept(visitor);
    return newArrayList(visitor.getCollectedDependencies());
  }

}
