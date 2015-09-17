/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule.arm;

import static java.lang.System.*;

import org.mule.tools.maven.plugin.mule.ApiException;
import org.mule.tools.maven.plugin.mule.DeploymentException;
import org.mule.tools.maven.plugin.mule.AbstractDeployer;
import org.mule.tools.maven.plugin.mule.TargetType;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class ArmDeployer extends AbstractDeployer
{

    private static final int DEFAULT_UNDEPLOY_TIMEOUT = 60000;
    private static final String TIMEOUT_PROPERTY = getProperty("mule.undeploy.TIMEOUT");
    private static final long TIMEOUT = TIMEOUT_PROPERTY == null ? DEFAULT_UNDEPLOY_TIMEOUT : Long.parseLong(TIMEOUT_PROPERTY);
    private final TargetType targetType;
    private final String target;
    private final ArmApi armApi;

    public ArmDeployer(String uri, String username, String password, String environment, TargetType targetType, String target, File application, String applicationName, Log log)
    {
        super(applicationName, application, log);
        this.targetType = targetType;
        this.target = target;
        armApi = new ArmApi(log, uri, username, password, environment);
    }

    @Override
    public void deploy() throws DeploymentException
    {
        try
        {
            armApi.init();
            info("Deploying application " + getApplicationName());
            Integer applicationId = armApi.findApplication(getApplicationName());
            if (applicationId == null)
            {
                armApi.deployApplication(getApplicationFile(), getApplicationName(), targetType, target);
            }
            else
            {
                info("Found application " + getApplicationName() + ". Undeploying application...");
                long start = currentTimeMillis();
                armApi.undeployApplication(applicationId);
                Application application = armApi.getApplicationStatus(applicationId);
                String status = application.data == null ? "UNDEPLOYED" : application.data.lastReportedStatus;
                while(!"UNDEPLOYED".equals(status))
                {
                    application = armApi.getApplicationStatus(applicationId);
                    status = application.data == null? "UNDEPLOYED" : application.data.lastReportedStatus;
                    if (currentTimeMillis() > start + TIMEOUT)
                    {
                        throw new DeploymentException("Failed to undeploy application");
                    }
                }
                info("Deploying application... ");
                armApi.deployApplication(getApplicationFile(), getApplicationName(), targetType, target);
            }
        }
        catch (ApiException e)
        {
            error("Failed: " + e.getMessage());
            throw new DeploymentException("Failed to deploy application " + getApplicationName(), e);
        }
    }

}
