/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.artifact;

import org.mule.tools.client.core.exception.DeploymentException;

/**
 * Deploys mule domains to mule platforms.
 */
public interface DomainDeployer {

  void deployDomain() throws DeploymentException;

  void undeployDomain() throws DeploymentException;
}
