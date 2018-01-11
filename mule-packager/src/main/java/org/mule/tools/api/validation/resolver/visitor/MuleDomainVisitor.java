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

import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;

/**
 * A visitor that searches over all of its direct dependencies that are mule domains
 */
public class MuleDomainVisitor extends AbstractArtifactVisitor {

  private static final String MULE_DOMAIN_CLASSIFIER = "mule-domain";
  private static final String DEFAULT_MULE_DOMAIN_SCOPE = "provided";

  public MuleDomainVisitor() {
    super(MULE_DOMAIN_CLASSIFIER, DEFAULT_MULE_DOMAIN_SCOPE, MulePluginVisitor::new);
  }

  /**
   * Adds the set of dependencies to the collected dependencies. Since they are supposed to be mule domains, they are not
   * collected.
   *
   * @param node
   */
  @Override
  public void collectDependencies(ProjectDependencyNode node) {}

}
