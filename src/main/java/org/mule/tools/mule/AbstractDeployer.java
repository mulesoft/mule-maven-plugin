/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule;

import java.io.File;


public abstract class AbstractDeployer
{
    private final File application;

    public AbstractDeployer(File application)
    {
        this.application = application;
    }

    public void execute() throws DeploymentException
    {
        init();
        try
        {
            deployApplication(application);
        }
        catch (Exception e)
        {
            throw new DeploymentException("Couldn't depploy app [" + application.getName() + "]", e);
        }
    }

    /**
     * Initializes the deployer. This method will be called once before executing the deploy for all the apps.
     */
    protected void init()
    {
    }

    /**
     * Deploys an application and returns an identification for it.
     */
    protected abstract String deployApplication(File file) throws DeploymentException;

    /**
     * Undeploys an application given its id.
     */
    protected abstract void undeployApplication(String id);

}
