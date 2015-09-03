/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule;

import org.mule.test.infrastructure.process.MuleProcessController;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 *
 */
public class Undeployer
{
    private List<MuleProcessController> mules;
    private Log log;

    public Undeployer(Log log, MuleProcessController... mules)
    {
        this.mules = Arrays.asList(mules);
        this.log = log;
    }

    public void execute() throws MojoFailureException, MojoExecutionException
    {
        for (MuleProcessController m : mules)
        {
            m.undeployAll();
            log.info("Applications undeployed");
            m.stop();
            log.info("Mule Standalone Server stopped");
        }
    }
}
