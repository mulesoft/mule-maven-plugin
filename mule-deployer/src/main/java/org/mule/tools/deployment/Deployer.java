/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment;

import org.mule.tools.client.core.exception.DeploymentException;

/**
 * @since 3.1.0
 */
public interface Deployer {

  /**
   * Deploys an artifact.
   *
   * @throws DeploymentException
   */
  void deploy() throws DeploymentException;

  /**
   * Undeploys an artifact.
   *
   * @throws DeploymentException
   */
  void undeploy() throws DeploymentException;
}
