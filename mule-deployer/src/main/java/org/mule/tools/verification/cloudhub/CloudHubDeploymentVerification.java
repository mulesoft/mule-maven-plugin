/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.cloudhub;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;

public class CloudHubDeploymentVerification implements DeploymentVerification {

  private final CloudHubClient client;

  public static final String STARTED_STATUS = "STARTED";
  private static final String FAILED_STATUS = "FAILED";
  private static final String DEPLOYING_STATUS = "DEPLOYING";

  public CloudHubDeploymentVerification(CloudHubClient client) {
    this.client = client;
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    DeploymentVerification verification =
        new DefaultDeploymentVerification(new CloudHubDeploymentVerificationStrategy(deployment));
    verification.assertDeployment(deployment);
  }

  private class CloudHubDeploymentVerificationStrategy implements DeploymentVerificationStrategy {

    private final Deployment deployment;

    public CloudHubDeploymentVerificationStrategy(Deployment deployment) {
      this.deployment = deployment;
    }

    @Override
    public Boolean isDeployed(Deployment deployment) {
      Application application = client.getApplications(deployment.getApplicationName());
      if (application != null) {
        if (StringUtils.containsIgnoreCase(application.getStatus(), FAILED_STATUS)) {
          throw new IllegalStateException("Deployment failed");
        } else if (StringUtils.equals(STARTED_STATUS, application.getStatus())
            && !StringUtils.equals(DEPLOYING_STATUS, application.getDeploymentUpdateStatus())) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void onTimeout(Deployment deployment) {
      client.stopApplications(deployment.getApplicationName());
    }

    @Override
    public Boolean run() {
      return !isDeployed(deployment);
    }

    @Override
    public String getRetryExhaustedMessage() {
      return "CloudHub deployment has timed out";
    }
  }
}
