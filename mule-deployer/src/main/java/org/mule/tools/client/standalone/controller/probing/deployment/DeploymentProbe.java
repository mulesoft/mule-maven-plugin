/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.standalone.controller.probing.deployment;

import org.mule.tools.client.standalone.controller.MuleProcessController;
import org.mule.tools.client.standalone.controller.probing.Probe;

public abstract class DeploymentProbe {

  protected boolean check;
  protected MuleProcessController mule;
  protected String artifactName;

  public DeploymentProbe() {}

  /**
   * Check if the current status of artifact is deployed.
   * @param mule A controller for the runtime instance.
   * @param artifactName The artifact which status is being checked.
   */
  public abstract Probe isDeployed(MuleProcessController mule, String artifactName);

  /**
   * Check if the current status of artifact is not deployed.
   * @param mule A controller for the runtime instance.
   * @param artifactName The artifact which status is being checked.
   */
  public abstract Probe notDeployed(MuleProcessController mule, String artifactName);


  protected DeploymentProbe(MuleProcessController mule, String artifactName, Boolean check) {
    this.mule = mule;
    this.artifactName = artifactName;
    this.check = check;
  }
}
