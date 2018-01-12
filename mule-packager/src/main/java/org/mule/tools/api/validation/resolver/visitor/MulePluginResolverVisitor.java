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

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

/**
 * This class is a composition of the visitors that visit the current node. As a mule plugin can be found as a dependency of
 * either domains or other mule plugins, this class ensures that both types of direct dependencies are processed independently.
 */
public class MulePluginResolverVisitor implements DependencyNodeVisitor {

  /**
   * The visitors that this visitor applies to each node
   */
  private final List<DependencyNodeVisitor> visitors;

  private static final List<DependencyNodeVisitor> DEFAULT_VISITORS =
      newArrayList(new MulePluginVisitor("compile"), new MuleDomainVisitor());

  public MulePluginResolverVisitor(List<DependencyNodeVisitor> visitors) {
    this.visitors = visitors;
  }

  public MulePluginResolverVisitor() {
    this.visitors = DEFAULT_VISITORS;
  }

  /**
   * Used to visit the project root. Visits each domain and mule plugin direct dependencies and starts the search through its
   * descendants
   * 
   * @param node The node to be visited, it is supposed to be the project's root
   * @throws ValidationException
   */
  @Override
  public void visit(ProjectDependencyNode node) throws ValidationException {
    for (DependencyNodeVisitor visitor : visitors) {
      visitor.visit(node);
    }
  }

  /**
   * Retrieves all the the collected mule plugin dependencies, which were collected by all the visitors that compose this class.
   * 
   * @return A collection of all the direct and transitive mule plugin dependencies.
   */
  @Override
  public Set<ArtifactCoordinates> getCollectedDependencies() {
    return visitors.stream().map(DependencyNodeVisitor::getCollectedDependencies).flatMap(Set::stream)
        .collect(Collectors.toSet());
  }
}
