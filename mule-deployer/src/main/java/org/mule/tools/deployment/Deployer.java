/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
