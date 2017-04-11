/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule.cloudhub;

import org.mule.tools.maven.plugin.mule.AbstractDeployer;
import org.mule.tools.maven.plugin.mule.ApiException;
import org.mule.tools.maven.plugin.mule.DeploymentException;

import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.File;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.json.JSONObject;

public class CloudhubDeployer extends AbstractDeployer {

  private final CloudhubApi cloudhubApi;

  private final String region;
  private final String muleVersion;
  private final Integer workers;
  private final String workerType;
  private final Map<String, String> properties;

  public CloudhubDeployer(String uri, String username, String password, String environment, String applicationName,
                          File application,
                          String region, String muleVersion, Integer workers, String workerType, Log log,
                          Map<String, String> properties, String businessGroup) {
    super(applicationName, application, log);
    this.cloudhubApi = new CloudhubApi(uri, log, username, password, environment, businessGroup);
    this.region = region;
    this.muleVersion = muleVersion;
    this.workers = workers;
    this.workerType = workerType;
    this.properties = properties;
  }

  @Override
  public void deploy() throws DeploymentException {
    cloudhubApi.init();

    info("Deploying application " + getApplicationName() + " to Cloudhub");

    if (!getApplicationFile().exists()) {
      throw new DeploymentException("Application file " + getApplicationFile() + " does not exist.");
    }

    try {
      boolean domainAvailable = cloudhubApi.isNameAvailable(getApplicationName());

      if (domainAvailable) {
        info("Creating application " + getApplicationName());
        cloudhubApi.createApplication(getApplicationName(), region, muleVersion, workers, workerType, properties);
      } else {
        Application app = findApplicationFromCurrentUser(getApplicationName());

        if (app != null) {
          info("Application " + getApplicationName() + " already exists, redeploying");

          String updateRegion = (region == null) ? app.region : region;
          String updateMuleVersion = (muleVersion == null) ? app.muleVersion : muleVersion;
          Integer updateWorkers = (workers == null) ? app.workers : workers;
          String updateWorkerType = (workerType == null) ? app.workerType : workerType;

          cloudhubApi.updateApplication(getApplicationName(), updateRegion, updateMuleVersion, updateWorkers, updateWorkerType,
                                        properties);
        } else {
          error("Domain " + getApplicationName() + " is not available. Aborting.");
          throw new DeploymentException("Domain " + getApplicationName() + " is not available. Aborting.");
        }
      }

      info("Uploading application contents " + getApplicationName());
      cloudhubApi.uploadFile(getApplicationName(), getApplicationFile());

      info("Starting application " + getApplicationName());
      cloudhubApi.startApplication(getApplicationName());
    } catch (ApiException e) {
      error("Failed: " + e.getMessage());
      throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
    }
  }

  private Application findApplicationFromCurrentUser(String appName) {
    for (Application app : cloudhubApi.getApplications()) {
      if (appName.equals(app.domain)) {
        return app;
      }
    }
    return null;
  }

}
