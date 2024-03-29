/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

public interface DeploymentVerification {

  void assertDeployment(Deployment deployment) throws DeploymentException;
}
