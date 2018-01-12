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
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

import java.util.Set;

/**
 * Interface that all visitors shall implement
 */
public interface DependencyNodeVisitor {

  /**
   * Visits a dependency node
   * 
   * @param dependencyNode The node being visited
   * @throws ValidationException
   */
  void visit(ProjectDependencyNode dependencyNode) throws ValidationException;

  /**
   * Retrieves the dependencies collected while visiting the dependency graph
   * 
   * @return The collected dependencies
   */
  Set<ArtifactCoordinates> getCollectedDependencies();
}
