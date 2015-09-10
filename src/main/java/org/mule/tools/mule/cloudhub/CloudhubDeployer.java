/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.cloudhub;

import org.mule.tools.mule.AbstractDeployer;
import org.mule.tools.mule.ApiException;
import org.mule.tools.mule.DeploymentException;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class CloudhubDeployer extends AbstractDeployer
{
    private final CloudhubApi cloudhubApi;

    private final String region;
    private final String muleVersion;
    private final Integer workers;
    private final String workerType;

    public CloudhubDeployer(String username, String password, String environment, String applicationName,
                            File application,
                            String region, String muleVersion, Integer workers, String workerType, Log log)
    {
        super(applicationName, application, log);
        this.cloudhubApi = new CloudhubApi(log, username, password, environment);
        this.region = region;
        this.muleVersion = muleVersion;
        this.workers = workers;
        this.workerType = workerType;
    }

    @Override
    public void deploy() throws DeploymentException
    {
        cloudhubApi.init();

        info("Deploying application " + getApplicationName() + " to Cloudhub");

        if (!getApplicationFile().exists())
        {
            throw new DeploymentException("Application file " + getApplicationFile() + " does not exist.");
        }

        try
        {
            boolean domainAvailable = cloudhubApi.isNameAvailable(getApplicationName());

            if (domainAvailable)
            {
                info("Creating application " + getApplicationName());
                cloudhubApi.createApplication(getApplicationName(), region, muleVersion, workers, workerType);
            }
            else
            {
                Application app = findApplicationFromCurrentUser(getApplicationName());

                if (app != null)
                {
                    info("Application " + getApplicationName() + " already exists, redeploying");

                    String updateRegion = (region == null) ? app.region : region;
                    String updateMuleVersion = (muleVersion == null) ? app.muleVersion : muleVersion;
                    Integer updateWorkers = (workers == null) ? app.workers : workers;
                    String updateWorkerType = (workerType == null) ? app.workerType : workerType;

                    cloudhubApi.updateApplication(getApplicationName(), updateRegion, updateMuleVersion, updateWorkers, updateWorkerType);
                }
                else
                {
                    error("Domain " + getApplicationName() + " is not available. Aborting.");
                    throw new DeploymentException("Domain " + getApplicationName() + " is not available. Aborting.");
                }
            }

            info("Uploading application contents " + getApplicationName());
            cloudhubApi.uploadFile(getApplicationName(), getApplicationFile());

            info("Starting application " + getApplicationName());
            cloudhubApi.startApplication(getApplicationName());
        }
        catch (ApiException e)
        {
            error("Failed: " + e.getMessage());
            throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
        }
    }

    private Application findApplicationFromCurrentUser(String appName)
    {
        for (Application app : cloudhubApi.getApplications())
        {
            if (appName.equals(app.domain))
            {
                return app;
            }
        }
        return null;
    }

}
