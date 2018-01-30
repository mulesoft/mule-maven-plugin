/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
