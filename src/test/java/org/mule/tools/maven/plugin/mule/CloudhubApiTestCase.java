/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.mule.tools.maven.plugin.mule.cloudhub.Application;
import org.mule.tools.maven.plugin.mule.cloudhub.CloudhubApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CloudhubApiTestCase {

  private static final String URI = "https://anypoint.mulesoft.com";
  private static final String USERNAME = System.getProperty("username");
  private static final String PASSWORD = System.getProperty("password");
  private static final String ENVIRONMENT = "Production";
  private static final String REGION = "us-east-1";
  private static final String MULE_VERSION = "3.6.1";
  private static final int WORKERS = 1;
  private static final String WORKER_TYPE = "Medium";

  private static final String APP_NAME = "test-app-12345";
  private static final File APP = new File("/tmp/echo-test4.zip");
  private CloudhubApi cloudhubApi;
  private Map<String, String> properties = new HashMap();

  @Before
  public void setup() {
    cloudhubApi = new CloudhubApi(URI, null, USERNAME, PASSWORD, ENVIRONMENT, "");
    cloudhubApi.init();
  }

  @After
  public void removeTestApplication() {
    Application application = cloudhubApi.getApplication(APP_NAME);
    if (application != null) {
      cloudhubApi.deleteApplication(application.domain);
    }
  }

  @Test
  public void createApplicationValidParameters() {
    verifyAppDoesntExist(APP_NAME);

    Application application = cloudhubApi.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    assertThat(application.domain, equalTo(APP_NAME));

    verifyAppExists(APP_NAME);
  }

  @Test
  public void createApplicationThatAlreadyExists() throws Exception {
    verifyAppDoesntExist(APP_NAME);
    cloudhubApi.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);

    try {
      cloudhubApi.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
      fail();
    } catch (ApiException e) {
      assertThat(e.getStatusCode(), equalTo(409));
      assertThat(e.getReasonPhrase(), equalTo("Conflict"));
    }
  }

  @Test
  public void uploadFile() {
    verifyAppDoesntExist(APP_NAME);
    cloudhubApi.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);
    cloudhubApi.uploadFile(APP_NAME, APP);
  }

  @Test
  public void startApplication() {
    verifyAppDoesntExist(APP_NAME);
    cloudhubApi.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);
    cloudhubApi.uploadFile(APP_NAME, APP);
    cloudhubApi.startApplication(APP_NAME);
  }

  @Test
  public void deleteApplication() {
    //verifyAppDoesntExist(APP_NAME);
    cloudhubApi.createApplication(APP_NAME, REGION, MULE_VERSION, WORKERS, WORKER_TYPE, properties);
    verifyAppExists(APP_NAME);
    cloudhubApi.deleteApplication(APP_NAME);
    verifyAppDoesntExist(APP_NAME);
  }

  private void verifyAppDoesntExist(String appName) {
    assertThat(cloudhubApi.getApplication(appName), nullValue());
  }

  private void verifyAppExists(String appName) {
    assertThat(cloudhubApi.getApplication(appName), notNullValue());
  }

}
