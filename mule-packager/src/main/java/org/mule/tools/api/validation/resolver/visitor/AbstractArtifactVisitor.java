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

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.resolver.model.DependenciesFilter;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.collect.Sets.newHashSet;

/**
 * An artifact visitor. In the packager, the artifact is supposed to be a dependency node of the dependency graph.
 */
public abstract class AbstractArtifactVisitor implements DependencyNodeVisitor {

  /**
   * A supplier for the visitor that are going to visit the visited node children.
   */
  private Supplier<DependencyNodeVisitor> childVisitor;

  /**
   * The visitor collected dependencies.
   */
  protected Set<ArtifactCoordinates> collectedDependencies = newHashSet();

  /**
   * A filter for the direct dependencies of the visited node. It means that all the direct dependencies that are going to be
   * processed and visited are just of one type, and their type is defined by this filter.
   */
  protected DependenciesFilter dependenciesFilter;


  public AbstractArtifactVisitor(String classifier, String scope, Supplier<DependencyNodeVisitor> childVisitor) {
    this.dependenciesFilter = new DependenciesFilter(classifier, scope);
    this.childVisitor = childVisitor;
  }

  protected AbstractArtifactVisitor() {}

  /**
   * Visits a node, processing its direct dependencies (that are of the type defined by the classifier in the constructor) and
   * visiting its children.
   * 
   * @param node The node being visited
   * @throws ValidationException
   */
  @Override
  public void visit(ProjectDependencyNode node) throws ValidationException {
    collectDependencies(node);
    visitChildren(node);
  }

  public abstract void collectDependencies(ProjectDependencyNode node);

  /**
   * Visits each the current node's children with the visitor that was supplied in the constructor
   * 
   * @throws ValidationException
   */
  protected void visitChildren(ProjectDependencyNode node)
      throws ValidationException {
    Set<ProjectDependencyNode> dependencies = node.getChildren(dependenciesFilter);
    for (ProjectDependencyNode dependency : dependencies) {
      DependencyNodeVisitor visitor = childVisitor.get();
      dependency.accept(visitor);
      collectedDependencies.addAll(visitor.getCollectedDependencies());
    }
  }

  @Override
  public Set<ArtifactCoordinates> getCollectedDependencies() {
    return collectedDependencies;
  }

  public void setChildVisitor(Supplier<DependencyNodeVisitor> childVisitor) {
    this.childVisitor = childVisitor;
  }
}
