/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.cloudhub;

import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;

public class CloudHubDeploymentVerification implements DeploymentVerification {

  private DefaultDeploymentVerification verification;

  static final String FAILED_STATUS = "FAIL";
  static final String STARTED_STATUS = "STARTED";
  static final String DEPLOYMENT_IN_PROGRESS = "DEPLOYING";

  public CloudHubDeploymentVerification(CloudHubClient client) {
    this.verification = new DefaultDeploymentVerification(new CloudHubDeploymentVerificationStrategy(client));
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    verification.assertDeployment(deployment);
  }
}
