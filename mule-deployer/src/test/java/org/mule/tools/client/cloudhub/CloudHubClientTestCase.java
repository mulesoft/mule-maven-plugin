/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.model.MuleVersion;
import org.mule.tools.client.core.exception.ClientException;
import org.mule.tools.model.anypoint.CloudHubDeployment;

@Ignore
public class CloudHubClientTestCase {

  private static final String BASE_URI = "https://anypoint.mulesoft.com";
  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");

  private static final String REGION = "us-east-1";
  private static final String MULE_VERSION = "4.0.0";
  private static final String ENVIRONMENT = "Production";

  private static final String BASE_DOMAIN_NAME = "test-app-%s";
  public static final String APPLICATION_FILE_NAME = "rick1-1.0.0-SNAPSHOT-mule-application-light-package.jar";

  private String appName;
  private File applicationFile;
  private Application baseApplication;

  private CloudHubClient cloudHubClient;
  private Map<String, String> properties = new HashMap();
  private CloudHubDeployment cloudHubDeployment;

  @Before
  public void setup() throws URISyntaxException {
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

  @After
  public void removeTestApplication() {
    Application application = cloudHubClient.getApplications(appName);
    if (application != null) {
      cloudHubClient.deleteApplications(application.getDomain());
    }
  }

  @Test
  public void createApplicationValidParameters() {
    verifyAppDoesntExist(appName);

    Application application = cloudHubClient.createApplications(baseApplication, applicationFile);
    assertThat(application.getDomain(), equalTo(appName));

    verifyAppExists(appName);
  }

  @Test
  public void createApplicationThatAlreadyExists() throws Exception {
    verifyAppDoesntExist(appName);

    cloudHubClient.createApplications(baseApplication, applicationFile);

    verifyAppExists(appName);
    try {
      cloudHubClient.createApplications(baseApplication, applicationFile);
      fail();
    } catch (ClientException e) {
      assertThat(e.getStatusCode(), equalTo(409));
      assertThat(e.getReasonPhrase(), equalTo("Conflict"));
    }
  }


  @Test
  public void startApplication() {
    verifyAppDoesntExist(appName);

    cloudHubClient.createApplications(baseApplication, applicationFile);

    verifyAppExists(appName);

    cloudHubClient.startApplications(appName);
  }


  @Test
  public void deleteApplication() {
    cloudHubClient.createApplications(baseApplication, applicationFile);

    verifyAppExists(appName);

    cloudHubClient.deleteApplications(appName);
  }

  private void verifyAppDoesntExist(String appName) {
    assertThat(cloudHubClient.getApplications(appName), nullValue());
  }

  private void verifyAppExists(String appName) {
    assertThat(cloudHubClient.getApplications(appName), notNullValue());
  }

}
