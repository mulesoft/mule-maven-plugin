/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub2;

import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.utils.DeployerLog;

public class Cloudhub2RuntimeFabricClient extends RuntimeFabricClient {

  private static final String AMC_API = "amc/application-manager/api/v2";
  private static final String AMC_RESOURCES_PATH = AMC_API + "/organizations/%s/environments/%s";
  private static final String AMC_DEPLOYMENTS_PATH = AMC_RESOURCES_PATH + "/deployments";

  public Cloudhub2RuntimeFabricClient(RuntimeFabricDeployment runtimeFabricDeployment, DeployerLog log) {
    super(runtimeFabricDeployment, log);
  }

  @Override
  protected String getDeploymentsPath() {
    return AMC_DEPLOYMENTS_PATH;
  }
}
