/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.arm;

import org.mule.tools.mule.AbstractDeployer;
import org.mule.tools.mule.DeploymentException;
import org.mule.tools.mule.TargetType;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class ArmDeployer extends AbstractDeployer
{

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
        armApi.init();
        armApi.deployApplication(getApplicationFile(), getApplicationName(), targetType, target);
    }

}
