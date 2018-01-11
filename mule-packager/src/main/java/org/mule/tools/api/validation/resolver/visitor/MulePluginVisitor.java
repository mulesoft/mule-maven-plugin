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
 * A visitor that searches over all of its direct dependencies that are mule plugins, collecting them
 */
public class MulePluginVisitor extends AbstractArtifactVisitor {

  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String DEFAULT_TRANSITIVE_PLUGIN_SCOPE = "provided";

  public MulePluginVisitor() {
    super(MULE_PLUGIN_CLASSIFIER, DEFAULT_TRANSITIVE_PLUGIN_SCOPE, MulePluginVisitor::new);
  }

  public MulePluginVisitor(String scope) {
    super(MULE_PLUGIN_CLASSIFIER, scope, MulePluginVisitor::new);
  }

  /**
   * Adds the set of dependencies to the collected dependencies. They are supposed to be mule plugins.
   *
   * @param node The node from which the dependencies are going to be collected
   */
  @Override
  public void collectDependencies(ProjectDependencyNode node) {
    collectedDependencies.addAll(node.getDependencies(dependenciesFilter));
  }
}
