/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing.deployment;

import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.Probe;

/**
 * Checks if a Mule application is successfully deployed.
 *
 */
public class ApplicationDeploymentProbe extends DeploymentProbe implements Probe {

  public ApplicationDeploymentProbe() {}

  protected ApplicationDeploymentProbe(MuleProcessController mule, String domainName, Boolean check) {
    super(mule, domainName, check);
  }

  @Override
  public Probe isDeployed(MuleProcessController mule, String artifactName) {
    return new ApplicationDeploymentProbe(mule, artifactName, true);
  }

  @Override
  public Probe notDeployed(MuleProcessController mule, String artifactName) {
    return new ApplicationDeploymentProbe(mule, artifactName, false);
  }

  public boolean isSatisfied() {
    return check == mule.isDeployed(artifactName);
  }

  public String describeFailure() {
    return "Application [" + artifactName + "] is " + (check ? "not" : "") + " deployed.";
  }
}
