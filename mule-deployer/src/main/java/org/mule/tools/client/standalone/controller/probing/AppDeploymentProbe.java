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

/**
 * Checks if a Mule application is successfully deployed.
 *
 */
public class AppDeploymentProbe implements Probe {

  private boolean check;
  private MuleProcessController mule;
  private String appName;

  public static AppDeploymentProbe isDeployed(MuleProcessController mule, String appName) {
    return new AppDeploymentProbe(mule, appName, true);
  }

  public static AppDeploymentProbe notDeployed(MuleProcessController mule, String appName) {
    return new AppDeploymentProbe(mule, appName, false);
  }

  protected AppDeploymentProbe(MuleProcessController mule, String appName, Boolean check) {
    this.mule = mule;
    this.appName = appName;
    this.check = check;
  }

  public boolean isSatisfied() {
    return check == mule.isDeployed(appName);
  }

  public String describeFailure() {
    return "Application [" + appName + "] is " + (check ? "not" : "") + " deployed.";
  }
}
