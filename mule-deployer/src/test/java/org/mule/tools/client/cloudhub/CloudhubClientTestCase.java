/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
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

import org.mule.tools.client.exception.ClientException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.tools.model.anypoint.CloudHubDeployment;

@Ignore
public class CloudhubClientTestCase {

  private static final String URI = "https://anypoint.mulesoft.com";
  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");
  private static final String ENVIRONMENT = "Production";
  private static final String REGION = "us-east-1";
  private static final String MULE_VERSION = "4.0.0-FD";
  private static final int WORKERS = 1;
  private static final String WORKER_TYPE = "Medium";

  private static final String APP_NAME = "test-app-12345";
  private static final File APP = new File("/tmp/echo-test4.zip");
  private CloudhubClient cloudhubClient;
  private Map<String, String> properties = new HashMap();
  private CloudHubDeployment cloudHubDeployment;

  @Before
  public void setup() {
    cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUri(URI);
    cloudHubDeployment.setUsername(USERNAME);
    cloudHubDeployment.setPassword(PASSWORD);
    cloudHubDeployment.setEnvironment(ENVIRONMENT);
    cloudHubDeployment.setBusinessGroup("");
    cloudhubClient = new CloudhubClient(cloudHubDeployment, null);
    cloudhubClient.init();
  }

  @After
  public void removeTestApplication() {
    Application application = cloudhubClient.getApplication(APP_NAME);
    if (application != null) {
      cloudhubClient.deleteApplication(application.domain);
    }
  }

  @Test
  public void createApplicationValidParameters() {
    verifyAppDoesntExist(APP_NAME);

    Application application = cloudhubClient.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    assertThat(application.domain, equalTo(APP_NAME));

    verifyAppExists(APP_NAME);
  }

  @Test
  public void createApplicationThatAlreadyExists() throws Exception {
    verifyAppDoesntExist(APP_NAME);
    cloudhubClient.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);

    try {
      cloudhubClient.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
      fail();
    } catch (ClientException e) {
      assertThat(e.getStatusCode(), equalTo(409));
      assertThat(e.getReasonPhrase(), equalTo("Conflict"));
    }
  }

  @Ignore
  @Test
  public void uploadFile() {
    verifyAppDoesntExist(APP_NAME);
    cloudhubClient.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);
    cloudhubClient.uploadFile(APP_NAME, APP);
  }

  @Ignore
  @Test
  public void startApplication() {
    verifyAppDoesntExist(APP_NAME);
    cloudhubClient.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);
    cloudhubClient.uploadFile(APP_NAME, APP);
    cloudhubClient.startApplication(APP_NAME);
  }

  @Ignore
  @Test
  public void deleteApplication() {
    cloudhubClient.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);
    cloudhubClient.deleteApplication(APP_NAME);
    verifyAppDoesntExist(APP_NAME);
  }

  private void verifyAppDoesntExist(String appName) {
    assertThat(cloudhubClient.getApplication(appName), nullValue());
  }

  private void verifyAppExists(String appName) {
    assertThat(cloudhubClient.getApplication(appName), notNullValue());
  }

}
