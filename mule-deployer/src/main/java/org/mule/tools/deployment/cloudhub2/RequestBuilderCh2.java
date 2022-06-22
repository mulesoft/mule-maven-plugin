/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.ApplicationModify;
import org.mule.tools.client.fabric.model.ApplicationRequest;
import org.mule.tools.client.fabric.model.AssetReference;
import org.mule.tools.client.fabric.model.DeploymentGenericResponse;
import org.mule.tools.client.fabric.model.DeploymentModify;
import org.mule.tools.client.fabric.model.DeploymentRequest;
import org.mule.tools.client.fabric.model.Deployments;
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.model.anypoint.Cloudhub2Deployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.anypoint.RuntimeFabricDeploymentSettings;

import java.util.HashMap;
import java.util.Map;

public class RequestBuilderCh2 extends org.mule.tools.deployment.fabric.RequestBuilder {

  public static final String ID = "id";
  private Cloudhub2Deployment deployment;
  private static final String AGENT_INFO = "agentInfo";
  private static final String NAME = "name";
  private static final String DOMAIN_WILDCARD = "*";

  protected RequestBuilderCh2(Cloudhub2Deployment deployment, RuntimeFabricClient client) {
    this.deployment = deployment;
    this.client = client;
  }

  public DeploymentRequest buildDeploymentRequest() throws DeploymentException {
    ApplicationRequest applicationRequest = buildApplicationRequest();

    Target target = buildTarget();

    DeploymentRequest deploymentRequest = new DeploymentRequest();

    deploymentRequest.setName(deployment.getApplicationName());
    deploymentRequest.setApplication(applicationRequest);
    deploymentRequest.setTarget(target);

    Map<String, Object> applicationPropertiesService = new HashMap<>();
    Map<String, Object> properties = new HashMap<>();
    properties.put("properties", deployment.getProperties());
    properties.put("secureProperties", deployment.getSecureProperties());
    properties.put("applicationName", deployment.getApplicationName());
    applicationPropertiesService.put("mule.agent.application.properties.service", properties);

    applicationRequest.setConfiguration(applicationPropertiesService);
    applicationRequest.setvCores(deployment.getvCores());
    applicationRequest.setIntegrations(deployment.getIntegrations());
    return deploymentRequest;
  }

}
