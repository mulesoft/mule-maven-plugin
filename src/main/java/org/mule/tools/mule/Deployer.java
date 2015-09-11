/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule;

import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;

import org.mule.test.infrastructure.process.AppDeploymentProbe;
import org.mule.test.infrastructure.process.MuleControllerException;
import org.mule.test.infrastructure.process.MuleProcessController;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

public class Deployer
{

    private MuleProcessController mule;
    private Log log;
    private File application;
    private Prober prober;
    private long timeout;
    private long pollingDelay;
    private String[] arguments;

    public Deployer(MuleProcessController mule,
                    Log log,
                    File application,
                    long timeout,
                    String[] arguments,
                    long pollingDelay)
    {
        this.mule = mule;
        this.log = log;
        this.application = application;
        this.timeout = timeout;
        this.pollingDelay = pollingDelay;
        this.arguments = arguments;
        this.prober = new PollingProber(timeout, pollingDelay);
        log.debug(toString());
    }

    public String toString()
    {
        return String.format("Deployer with [Controller=%s, log=%s, application=%s, timeout=%d, pollingDelay=%d ]",
                             mule, log, application, timeout, pollingDelay
        );
    }

    public void execute() throws MojoFailureException, MojoExecutionException
    {
        try
        {
            startMuleIfStopped();
            deployApplications();
            waitForDeployments();
        }
        catch (MuleControllerException e)
        {
            throw new MojoFailureException("Error deploying application: [" + application + "]: " + e.getMessage());
        }
        catch (RuntimeException e)
        {
            throw new MojoExecutionException("Unexpected error deploying application: [" + application
                                             + "]", e);
        }
    }

    private void waitForDeployments() throws MojoFailureException
    {
        if (!application.exists())
        {
            throw new MojoFailureException("Application does not exists: " + application);
        }
        log.debug("Checking for application [" + application + "] to be deployed.");
        String app = getApplicationName(application);
        try
        {
            prober.check(AppDeploymentProbe.isDeployed(mule, app));
        }
        catch (AssertionError e)
        {
            log.error("Couldn't deploy application [" + app + "] after [" + timeout
                      + "] miliseconds. Check Mule ESB log");
            throw new MojoFailureException("Application deployment timeout.");
        }
    }

    private String getApplicationName(File application)
    {
        String name = application.getName();
        int extensionBeginning = name.lastIndexOf('.');
        return extensionBeginning == -1 ? name : name.substring(0, extensionBeginning);
    }

    private void deployApplications() throws MojoFailureException
    {
        log.info("Deploying application [" + application + "]");
        try
        {
            mule.deploy(application.getAbsolutePath());
        }
        catch (MuleControllerException e)
        {
            log.error("Couldn't deploy application: " + application + ". Check Mule ESB logs");
        }
    }

    private void startMuleIfStopped()
    {
        log.debug("Checking if Mule ESB is running.");
        if (!mule.isRunning())
        {
            try
            {
                log.info("Starting Mule ESB");
                if (arguments == null)
                {
                    mule.start();
                }
                else
                {
                    mule.start(arguments);
                }
            }
            catch (MuleControllerException e)
            {
                log.error("Couldn't start Mule ESB. Check Mule ESB logs");
            }
        }
    }

    public Deployer addLibraries(List<File> libs)
    {
        for (File file : libs)
        {
            mule.addLibrary(file);
            log.debug(String.format("Adding library %s...", file));
        }
        return this;
    }


    public Deployer addDomain(File domain) throws MojoFailureException
    {
        try
        {
            log.debug(String.format("Deploying domain : %s", domain));
            mule.deployDomain(domain.getAbsolutePath());
            return this;
        }
        catch(MuleControllerException e)
        {
            log.error("Couldn't deploy domain: " + domain);
            throw new MojoFailureException("Couldn't deploy domain: " + domain);
        }
    }
}
