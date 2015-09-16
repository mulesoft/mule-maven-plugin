/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import java.io.File;

import org.apache.maven.plugin.logging.Log;


public abstract class AbstractDeployer
{
    private final String applicationName;
    private final File applicationFile;
    private final Log log;

    public AbstractDeployer(String applicationName, File applicationFile, Log log)
    {
        this.applicationName = applicationName;
        this.applicationFile = applicationFile;
        this.log = log;
    }

    /**
     * Deploys the application.
     */
    public abstract void deploy() throws DeploymentException;

    /**
     * Logs an info message in the plugin.
     */
    protected void info(String message)
    {
        log.info(message);
    }

    /**
     * Logs an error message in the plugin.
     */
    protected void error(String message)
    {
        log.error(message);
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public File getApplicationFile()
    {
        return applicationFile;
    }

}
