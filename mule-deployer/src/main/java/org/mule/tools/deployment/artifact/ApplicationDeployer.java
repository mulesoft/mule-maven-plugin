/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.artifact;

import org.mule.tools.client.core.exception.DeploymentException;

/**
 * Deploys mule applications to mule platforms.
 */
public interface ApplicationDeployer {

  void deployApplication() throws DeploymentException;

  void undeployApplication() throws DeploymentException;

}
