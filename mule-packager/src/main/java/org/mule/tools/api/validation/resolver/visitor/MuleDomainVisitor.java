/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
