/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
