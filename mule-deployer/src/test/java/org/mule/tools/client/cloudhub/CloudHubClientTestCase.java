/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.model.Deployment;
import org.mule.tools.client.cloudhub.model.DeploymentLogRequest;
import org.mule.tools.client.cloudhub.model.Instance;
import org.mule.tools.client.cloudhub.model.MuleVersion;
import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled("I suspect that there should be a server for the requests.")
class CloudHubClientTestCase {

  private static final String BASE_URI = "https://anypoint.mulesoft.com";
  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");

  private static final String REGION = "us-east-1";
  private static final String MULE_VERSION = "4.2.0";
  private static final String ENVIRONMENT = "Production";

  private static final String BASE_DOMAIN_NAME = "test-app-%s";
  public static final String APPLICATION_FILE_NAME = "rick1-1.0.0-SNAPSHOT-mule-application-light-package.jar";

  private String appName;
  private File applicationFile;
  private Application baseApplication;

  private CloudHubClient cloudHubClient;
  private Map<String, String> properties = new HashMap();
  private CloudHubDeployment cloudHubDeployment;

  @BeforeEach
  void setup() throws URISyntaxException {
    appName = String.format(BASE_DOMAIN_NAME, new Date().getTime());

    applicationFile = new File(getClass().getClassLoader().getResource(APPLICATION_FILE_NAME).toURI().getPath());

    buildBaseApplication();

    buildCloudhubDeployment();

    cloudHubClient = new CloudHubClient(cloudHubDeployment, null);
  }

  private void buildCloudhubDeployment() {
    cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUri(BASE_URI);
    cloudHubDeployment.setUsername(USERNAME);
    cloudHubDeployment.setPassword(PASSWORD);
    cloudHubDeployment.setEnvironment(ENVIRONMENT);
    cloudHubDeployment.setBusinessGroup("");
  }

  private void buildBaseApplication() {
    MuleVersion muleVersion = new MuleVersion();
    muleVersion.setVersion(MULE_VERSION);

    baseApplication = new Application();
    baseApplication.setDomain(appName);
    baseApplication.setMuleVersion(muleVersion);
    baseApplication.setProperties(properties);
    baseApplication.setRegion(REGION);
  }

  @AfterEach
  void removeTestApplication() {
    Application application = cloudHubClient.getApplications(appName);
    if (application != null) {
      cloudHubClient.deleteApplications(application.getDomain());
    }
  }

  @Test
  void createApplicationValidParameters() {
    verifyAppDoesntExist(appName);

    Application application = cloudHubClient.createApplication(baseApplication, applicationFile);
    assertThat(application.getDomain()).isEqualTo(appName);

    verifyAppExists(appName);
  }

  @Test
  void createApplicationThatAlreadyExists() throws Exception {
    verifyAppDoesntExist(appName);

    cloudHubClient.createApplication(baseApplication, applicationFile);

    verifyAppExists(appName);
    try {
      cloudHubClient.createApplication(baseApplication, applicationFile);
      fail();
    } catch (ClientException e) {
      assertThat(e.getStatusCode()).isEqualTo(409);
      assertThat(e.getReasonPhrase()).isEqualTo("Conflict");
    }
  }


  @Test
  void startApplication() {
    verifyAppDoesntExist(appName);

    cloudHubClient.createApplication(baseApplication, applicationFile);

    verifyAppExists(appName);

    cloudHubClient.startApplications(appName);
  }


  @Test
  void deleteApplication() {
    cloudHubClient.createApplication(baseApplication, applicationFile);

    verifyAppExists(appName);

    cloudHubClient.deleteApplications(appName);
  }

  @Test
  void getLogsForDeployment() {
    Application application = cloudHubClient.createApplication(baseApplication, applicationFile);
    cloudHubClient.startApplications(appName);

    Deployment deployment = cloudHubClient.getDeployments(application).stream().findAny()
        .orElseThrow(() -> new AssertionError("No deployments found for application"));
    DeploymentLogRequest logRequest = new DeploymentLogRequest();
    logRequest.setDeploymentId(deployment.getDeploymentId());
    logRequest.setStartTime(deployment.getCreateTime().getTime());

    cloudHubClient.getLogs(application, logRequest);
  }

  @Test
  void getEntireLogsForDeployment() {
    Application application = cloudHubClient.createApplication(baseApplication, applicationFile);
    cloudHubClient.startApplications(appName);

    Deployment deployment = cloudHubClient.getDeployments(application).stream().findAny()
        .orElseThrow(() -> new AssertionError("No deployments found for application"));

    Instance instance = deployment.getInstances().stream().findAny()
        .orElseThrow(() -> new AssertionError("No instances found for deployment"));

    cloudHubClient.getEntireLogs(application, instance.getInstanceId());
  }

  private void verifyAppDoesntExist(String appName) {
    assertThat(cloudHubClient.getApplications(appName)).isNull();
  }

  private void verifyAppExists(String appName) {
    assertThat(cloudHubClient.getApplications(appName)).isNotNull();
  }
}
