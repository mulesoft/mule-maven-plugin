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
import org.mule.util.FilenameUtils;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class CloudhubDeployer extends AbstractDeployer
{
    private final CloudhubApi cloudhubApi;

    private final boolean redeploy;
    private final String region;
    private final String muleVersion;
    private final int workers;
    private final String workerType;

    private final Log log;

    public CloudhubDeployer(String username, String password, String environment, File applications, boolean redeploy,
                            String region, String muleVersion, int workers, String workerType, Log log)
    {
        super(applications);
        this.cloudhubApi = new CloudhubApi(username, password, environment);
        this.redeploy = redeploy;
        this.region = region;
        this.muleVersion = muleVersion;
        this.workers = workers;
        this.workerType = workerType;
        this.log = log;
    }

    @Override
    protected void init()
    {
        cloudhubApi.init();
    }

    @Override
    protected String deployApplication(File file) throws DeploymentException
    {
        String appName = FilenameUtils.getBaseName(file.getName());

        log.info("Deploying application " + appName + " to Cloudhub");

        try
        {
            boolean domainAvailable = cloudhubApi.isNameAvailable(appName);

            if (domainAvailable)
            {
                log.info("Creating application " + appName);
                cloudhubApi.createApplication(appName, region, muleVersion, workers, workerType);
            }
            else
            {
                if (redeploy)
                {
                    log.info("Application " + appName + " already exists, redeploying");
                    cloudhubApi.updateApplication(appName, region, muleVersion, workers, workerType);
                }
                else
                {
                    log.error("Application " + appName + " already exists, but redeploy=false. Aborting.");
                    throw new DeploymentException("Application " + appName + " already exists");
                }
            }

            log.info("Uploading application contents " + appName);
            cloudhubApi.uploadFile(appName, file);

            log.info("Starting application " + appName);
            cloudhubApi.startApplication(appName);
        }
        catch (ApiException e)
        {
            log.error("Failed: " + e.getMessage());
            throw e;
        }

        return appName;
    }

    @Override
    protected void undeployApplication(String id)
    {
        cloudhubApi.deleteApplication(id);
    }

}
