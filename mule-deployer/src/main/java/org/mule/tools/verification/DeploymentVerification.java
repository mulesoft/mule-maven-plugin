/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.verification;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

public interface DeploymentVerification {

  void assertDeployment(Deployment deployment) throws DeploymentException;
}
