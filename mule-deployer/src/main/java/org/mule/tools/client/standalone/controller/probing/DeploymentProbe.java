/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing;

import org.mule.tools.client.standalone.controller.MuleProcessController;

public abstract class DeploymentProbe {

  protected boolean check;
  protected MuleProcessController mule;
  protected String artifactName;

  public DeploymentProbe() {}

  public abstract Probe isDeployed(MuleProcessController mule, String artifactName);

  public abstract Probe notDeployed(MuleProcessController mule, String artifactName);

  protected DeploymentProbe(MuleProcessController mule, String artifactName, Boolean check) {
    this.mule = mule;
    this.artifactName = artifactName;
    this.check = check;
  }
}
