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

public class ArmDeployer extends AbstractDeployer
{

    private final TargetType targetType;
    private final String target;
    private String applicationName;
    private final ArmApi armApi;

    public ArmDeployer(String uri, String username, String password, String environment, TargetType targetType, String target, File application, String applicationName)
    {
        super(application);
        this.targetType = targetType;
        this.target = target;
        this.applicationName = applicationName;
        armApi = new ArmApi(uri, username, password, environment);
    }

    @Override
    protected void init()
    {
        armApi.init();
    }

    @Override
    protected String deployApplication(File file) throws DeploymentException
    {
        int id = armApi.deployApplication(file, applicationName, targetType, target).data.id;
        return String.valueOf(id);
    }

    @Override
    protected void undeployApplication(String id)
    {
        armApi.undeployApplication(Integer.valueOf(id));
    }

}
