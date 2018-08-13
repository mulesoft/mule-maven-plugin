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

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.cloudhub.Application;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DefaultDeploymentVerification;
import org.mule.tools.verification.DeploymentVerification;
import org.mule.tools.verification.DeploymentVerificationStrategy;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class CloudHubDeploymentVerification implements DeploymentVerification {

  private final CloudHubClient client;
  private DefaultDeploymentVerification verification;

  private static final String FAILED_STATUS = "FAILED";
  public static final String STARTED_STATUS = "STARTED";

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
        Application application = client.getApplication(deployment.getApplicationName());
        if (application != null) {
          if (StringUtils.equals(FAILED_STATUS, application.status)) {
            throw new IllegalStateException("Deployment failed");
          } else if (StringUtils.equals(STARTED_STATUS, application.status)) {
            return true;
          }
        }
        return false;
      };
    }

    @Override
    public Consumer<Deployment> onTimeout() {
      return deployment -> client.stopApplication(deployment.getApplicationName());
    }
  }
}
