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

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ProjectBuildingException;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;
import org.mule.tools.api.validation.resolver.visitor.DependencyNodeVisitor;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.mule.tools.api.validation.VersionUtils.isRange;

/**
 * Represents a project dependency
 */
public class ProjectDependencyNode {

  private final Project project;

  private final ProjectBuilder builder;

  /**
   * Represents a node in the dependency tree.
   * 
   * @param project The project that represents the dependency
   * @param builder A builder to the project. It is usually implemented using build tool classes, such as MavenProjectBuilder
   */
  public ProjectDependencyNode(Project project, ProjectBuilder builder) {
    this.project = project;
    this.builder = builder;
  }

  /**
   * Accepts the visitor, invoking the visit method.
   * 
   * @param visitor A {@code DependencyNodeVisitor} implementation
   * @throws ValidationException
   */
  public void accept(DependencyNodeVisitor visitor) throws ValidationException {
    visitor.visit(this);
  }

  /**
   * @return The project that this node represents
   */
  public Project getProject() {
    return project;
  }

  /**
   * Retrieves a set containing the direct dependency nodes based on a filter.
   * 
   * @param filter The filter that is going to define which dependencies are going constitute the set
   * @return A set containing the direct filtered dependency nodes
   * @throws ValidationException
   */
  public Set<ProjectDependencyNode> getChildren(DependenciesFilter filter)
      throws ValidationException {

    Set<ProjectDependencyNode> children = newHashSet();

    for (ArtifactCoordinates dependency : getDependencies(filter)) {
      if (!isRange(dependency.getVersion())) {
        children.add(buildNode(dependency));
      }
    }

    return children;
  }

  /**
   * Retrieves the direct dependencies that comply with the filter constraint
   *
   * @param filter The filter to the dependencies
   * @return A set of filtered dependencies
   */
  public Set<ArtifactCoordinates> getDependencies(DependenciesFilter filter) {
    return filter.filter(this);
  }

  /**
   * Builds a {@code ProjectDependencyNode} using the default {@code builder}
   *
   * @param dependency The dependency to be built
   * @return A {@code ProjectDependencyNode} containing the dependency project and its builder
   * @throws ValidationException If the dependency cannot be built into a project
   */
  public ProjectDependencyNode buildNode(ArtifactCoordinates dependency) throws ValidationException {
    try {
      Project project = builder.buildProject(dependency);
      return new ProjectDependencyNode(project, builder);
    } catch (ProjectBuildingException e) {
      throw new ValidationException(e);
    }
  }
}
