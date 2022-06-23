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

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.fabric.RuntimeFabricClient;
import org.mule.tools.client.fabric.model.ApplicationRequest;
import org.mule.tools.client.fabric.model.DeploymentRequest;
import org.mule.tools.client.fabric.model.Target;
import org.mule.tools.model.anypoint.Cloudhub2Deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilderCh2 extends org.mule.tools.deployment.fabric.RequestBuilder {

  public static final String ID = "id";
  private Cloudhub2Deployment deployment;

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

    Map<String, Object> loggingService = new HashMap<>();
    Map<String, Object> loggingServiceProperties = new HashMap<>();
    loggingServiceProperties.put("artifactName", deployment.getApplicationName());
    loggingServiceProperties.put("scopeLoggingConfigurations", deployment.getScopeLoggingConfigurations());
    loggingService.put("mule.agent.logging.service", loggingServiceProperties);
    ArrayList<Object> configuration = new ArrayList<Object>();
    configuration.add(applicationPropertiesService);
    configuration.add(loggingService);
    applicationRequest.setConfiguration(configuration);
    applicationRequest.setvCores(deployment.getvCores());
    applicationRequest.setIntegrations(deployment.getIntegrations());
    return deploymentRequest;
  }

}
