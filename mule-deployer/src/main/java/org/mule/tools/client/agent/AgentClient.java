/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent;

import org.mule.tools.client.agent.model.Application;
import org.mule.tools.client.core.AbstractClient;
import org.mule.tools.client.core.exception.ClientException;

import java.io.File;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.utils.DeployerLog;

/**
 * A client to the Mule Agent.
 */
public class AgentClient extends AbstractClient {

  public static final String APPLICATIONS_PATH = "/mule/applications/";
  public static final String DOMAINS_PATH = "/mule/domains/";
  public static final int ACCEPTED = 202;
  private static final String AGENT_INFO_PATH = "/mule/agent";

  private final String uri;

  public AgentClient(DeployerLog log, Deployment deployment) {
    super(log);
    this.uri = ((AgentDeployment) deployment).getUri();
  }

  protected void init() {
    // DO NOTHING
  }

  /**
   * Deploys a mule application to the agent.
   * 
   * @param applicationName The name of application to be deployed
   * @param file The application contents file
   */
  public void deployApplication(String applicationName, File file) {
    deployArtifact(applicationName, file, APPLICATIONS_PATH);
  }

  /**
   * Deploys a mule domain to the agent.
   * 
   * @param domainName The name of domain to be deployed
   * @param file The domain contents file
   */
  public void deployDomain(String domainName, File file) {
    deployArtifact(domainName, file, DOMAINS_PATH);
  }

  /**
   * Deploys a mule artifact to the agent.
   * 
   * @param artifactName The name of artifact to be deployed
   * @param file The artifact contents file
   * @param resourcePath The relative path to the resource
   */
  protected void deployArtifact(String artifactName, File file, String resourcePath) {
    Entity entity = Entity.entity(file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    Response response = put(uri, resourcePath + artifactName, entity);

    if (response.getStatus() != ACCEPTED) {
      throw new ClientException(response, uri + resourcePath + artifactName);
    }
  }

  /**
   * Undeploys a mule application.
   * 
   * @param appName The name of application to be undeployed
   */
  public void undeployApplication(String appName) {
    undeployArtifact(appName, APPLICATIONS_PATH);
  }

  /**
   * Undeploys a mule domain.
   * 
   * @param domainName The name of domain to be undeployed
   */
  public void undeployDomain(String domainName) {
    undeployArtifact(domainName, DOMAINS_PATH);
  }

  /**
   * Undeploys a mule artifact.
   * 
   * @param artifactName The name of artifact to be undeployed
   * @param resourcePath The relative path of the resource
   */
  protected void undeployArtifact(String artifactName, String resourcePath) {
    Response response = delete(uri, resourcePath + artifactName);

    if (response.getStatus() != ACCEPTED) {
      throw new ClientException(response, uri + resourcePath + artifactName);
    }
  }

  /**
   * Retrieves the application information.
   * 
   * @param appName The application name of which the information is being gathered
   * @return An {@link Application} instance
   */
  public Application getApplication(String appName) {
    return get(uri, APPLICATIONS_PATH + appName).readEntity(Application.class);
  }

  /**
   * Retrieves agent information relative to the Agent instance being queried.
   * 
   * @return An {@link AgentInfo} instance
   */
  public AgentInfo getAgentInfo() {
    return get(uri, AGENT_INFO_PATH).readEntity(AgentInfo.class);
  }
}
