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
import static org.mockito.Mockito.mock;

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
public class CloudHubClientTestCase {

  private static final String BASE_URI = "https://anypoint.mulesoft.com";
  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");
  private static final String ENVIRONMENT = "Production";
  private static final String REGION = "us-east-1";
  private static final String MULE_VERSION = "4.0.0-FD";
  private static final int WORKERS = 1;
  private static final String WORKER_TYPE = "Medium";

  private static final String APP_NAME = "test-app-12345";
  private static final File APP = new File("/tmp/echo-test4.zip");
  private CloudHubClient cloudHubClient;
  private Map<String, String> properties = new HashMap();
  private CloudHubDeployment cloudHubDeployment;
  private ApplicationMetadata metadataMock = mock(ApplicationMetadata.class);

  @Before
  public void setup() {
    cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUri(BASE_URI);
    cloudHubDeployment.setUsername(USERNAME);
    cloudHubDeployment.setPassword(PASSWORD);
    cloudHubDeployment.setEnvironment(ENVIRONMENT);
    cloudHubDeployment.setBusinessGroup("");
    cloudHubClient = new CloudHubClient(cloudHubDeployment, null);
    cloudHubClient.init();
  }

  @After
  public void removeTestApplication() {
    Application application = cloudHubClient.getApplication(APP_NAME);
    if (application != null) {
      cloudHubClient.deleteApplication(application.domain);
    }
  }

  @Test
  public void createApplicationValidParameters() {
    verifyAppDoesntExist(APP_NAME);

    Application application = cloudHubClient.createApplication(metadataMock);
    assertThat(application.domain, equalTo(APP_NAME));

    verifyAppExists(APP_NAME);
  }

  @Test
  public void createApplicationThatAlreadyExists() throws Exception {
    verifyAppDoesntExist(APP_NAME);
    cloudHubClient.createApplication(metadataMock);
    verifyAppExists(APP_NAME);

    try {
      cloudHubClient.createApplication(metadataMock);
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
    cloudHubClient.createApplication(metadataMock);
    verifyAppExists(APP_NAME);
    cloudHubClient.uploadFile(APP_NAME, APP);
  }

  @Ignore
  @Test
  public void startApplication() {
    verifyAppDoesntExist(APP_NAME);
    cloudHubClient.createApplication(metadataMock);
    verifyAppExists(APP_NAME);
    cloudHubClient.uploadFile(APP_NAME, APP);
    cloudHubClient.startApplication(APP_NAME);
  }

  @Ignore
  @Test
  public void deleteApplication() {
    cloudHubClient.createApplication(metadataMock);
    verifyAppExists(APP_NAME);
    cloudHubClient.deleteApplication(APP_NAME);
    verifyAppDoesntExist(APP_NAME);
  }

  private void verifyAppDoesntExist(String appName) {
    assertThat(cloudHubClient.getApplication(appName), nullValue());
  }

  private void verifyAppExists(String appName) {
    assertThat(cloudHubClient.getApplication(appName), notNullValue());
  }

}
