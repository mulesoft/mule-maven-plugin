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

import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class CloudHubDeploymentVerification implements DeploymentVerification {

  private final CloudHubClient client;
  private DefaultDeploymentVerification verification;

  private static final String FAILED_STATUS = "FAIL";
  public static final String STARTED_STATUS = "STARTED";
  static final String DEPLOYMENT_IN_PROGRESS = "DEPLOYING";

  public CloudHubDeploymentVerification(CloudHubClient client) {
    this.client = client;
    this.verification = new DefaultDeploymentVerification(new CloudHubDeploymentVerificationStrategy());
  }

  @Override
  public void assertDeployment(Deployment deployment) throws DeploymentException {
    verification.assertDeployment(deployment);
  }

  private class CloudHubDeploymentVerificationStrategy implements DeploymentVerificationStrategy {

    @Override
    public Predicate<Deployment> isDeployed() {
      return (deployment) -> {
        Application application = client.getApplications(deployment.getApplicationName());
        if (application != null) {
          if (equalsIgnoreCase(application.getDeploymentUpdateStatus(), DEPLOYMENT_IN_PROGRESS)) {
            return false;
          } else if (containsIgnoreCase(application.getStatus(), FAILED_STATUS)
              || containsIgnoreCase(application.getDeploymentUpdateStatus(), FAILED_STATUS)) {
            throw new IllegalStateException("Deployment failed");
          }
          return equalsIgnoreCase(STARTED_STATUS, application.getStatus());
        }
        return false;
      };
    }

    @Override
    public Consumer<Deployment> onTimeout() {
      return deployment -> client.stopApplications(deployment.getApplicationName());
    }
  }
}
