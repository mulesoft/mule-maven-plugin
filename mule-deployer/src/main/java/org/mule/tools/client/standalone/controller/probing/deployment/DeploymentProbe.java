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
