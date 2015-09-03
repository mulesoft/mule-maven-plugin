/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.agent;

import org.mule.tools.mule.AbstractDeployer;
import org.mule.tools.mule.ApiException;
import org.mule.tools.mule.DeploymentException;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class AgentDeployer extends AbstractDeployer
{
    private final AgentApi agentApi;
    private final Log log;
    private final String applicationName;

    public AgentDeployer(Log log, String applicationName, File application, String uri, String trustStorePath, String trustStorePassword, String trustStoreType)
    {
        super(application);
        this.applicationName = applicationName;
        this.agentApi = new AgentApi(uri, trustStorePath, trustStorePassword, trustStoreType);
        this.log = log;
    }

    @Override
    protected String deployApplication(File file) throws DeploymentException
    {
        try
        {
            log.info("Deploying application " + file.getName() + " to Mule Agent");
            agentApi.deployApplication(applicationName, file);
        }
        catch (ApiException e)
        {
            log.error("Failure: " + e.getMessage());
            throw e;
        }
        return file.getName();
    }

    @Override
    protected void undeployApplication(String id)
    {
        log.info("Undeploying application " + id + " from Mule Agent");
        agentApi.undeployApplication(id);
    }
}
