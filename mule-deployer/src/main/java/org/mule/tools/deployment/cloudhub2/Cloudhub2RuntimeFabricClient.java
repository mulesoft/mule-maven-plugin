/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
