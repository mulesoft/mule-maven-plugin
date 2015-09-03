/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule;

import org.mule.test.infrastructure.process.MuleProcessController;
import org.mule.tools.mule.arm.ArmApi;
import org.mule.tools.mule.arm.Applications;
import org.mule.tools.mule.arm.Data;
import org.mule.tools.mule.agent.AgentApi;
import org.mule.tools.mule.cloudhub.CloudhubApi;
import org.mule.util.FilenameUtils;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Undeploys all the applications on a Mule ESB Standalone server, regardless of whether it was started using start or deploy goals.
 *
 * @author <a href="mailto:asequeira@gmail.com">Ale Sequeira</a>
 * @see org.mule.tools.mule.DeployMojo
 * @since 1.0
 */
@Mojo(name = "undeploy", requiresProject = true)
public class UndeployMojo extends AbstractMuleMojo
{
    /**
     * Absolute path for Mule ESB Standalone server (i.e. $MULE_HOME).
     *
     * @since 1.0
     */
    @Parameter(property = "mule.home", required = false)
    private File muleHome;

    /**
     * Number of cluster nodes.
     *
     * @since 2.0
     */
    @Parameter(defaultValue = "2", readonly = true, required = true)
    private Integer size;

    /**
     * Version of the Mule ESB Enterprise distribution.
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "mule.version")
    private String muleVersion;

    /**
     * Anypoint Platform username.
     *
     * @since 2.0
     */
    @Parameter(required = false, readonly = true, property = "anypoint.username")
    private String username;

    /**
     * Anypoint Platform password.
     *
     * @since 2.0
     */
    @Parameter(required = false, readonly = true, property = "anypoint.password")
    private String password;

    /**
     * Deployment information.
     *
     * @since 1.0
     */
    @Parameter(required = true, readonly = true)
    private Deployment deployment;

    /**
     * Application to be undeployed.
     *
     * @since 1.0
     */
    @Parameter(property = "mule.application")
    private File application;

    /**
     * Anypoint Platform URI, can be configured to use with On Premise platform..
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "anypoint.uri", defaultValue = "https://anypoint.mulesoft.com")
    protected String uri;

    /**
     * Anypoint environment ID.
     *
     * @since 2.0
     */
    @Parameter(required = false, readonly = true, property = "anypoint.environment")
    protected String environment;

    /**
     * Trust store path then connecting through HTTPS.
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "trustStorePath")
    protected String trustStorePath;

    /**
     * Trust store password then connecting through HTTPS.
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "trustStorePassword")
    protected String trustStorePassword;

    /**
     * Trust store type then connecting through HTTPS.
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "trustStoreType", defaultValue = "jks")
    protected String trustStoreType;

    private String applicationName;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
        switch (deployment.getType())
        {
            case standalone:
                standalone();
                break;
            case cluster:
                cluster();
                break;
            case arm:
                arm();
                break;
            case cloudhub:
                cloudhub();
                break;
            case agent:
                agent();
                break;
            default:
                throw new MojoFailureException("Unsupported deployment type: " + deployment.getType());
        }
    }

    private void cloudhub() throws MojoFailureException
    {
        CloudhubApi cloudhubApi = new CloudhubApi(username, password, environment);
        cloudhubApi.init();
        initializeApplication();
        String appName = FilenameUtils.getBaseName(application.getName());
        getLog().info("Undeploying and deleting application " + appName);
        cloudhubApi.deleteApplication(appName);
    }

    private void arm() throws MojoFailureException
    {
        ArmApi arm = new ArmApi(uri, username, password, environment);
        arm.init();
        initializeApplication();
        Data app = this.findApplication(arm, applicationName);
        arm.undeployApplication(app.id);
    }

    private void agent() throws MojoFailureException
    {
        AgentApi agentApi = new AgentApi(uri, trustStorePath, trustStorePassword, trustStoreType);
        initializeApplication();
        getLog().info("Undeploying application " + applicationName);
        agentApi.undeployApplication(applicationName);
    }

    private Data findApplication(ArmApi arm, String applicationName) throws MojoFailureException
    {
        Applications apps = arm.getApplications();
        for (int i = 0; i < apps.data.length; i++)
        {
            if (apps.data[i].artifact.name.equals(applicationName))
            {
                return apps.data[i];
            }
        }
        throw new MojoFailureException("Couldn't find appliation [" + applicationName + "]");
    }

    private void cluster() throws MojoFailureException, MojoExecutionException
    {
        MuleProcessController[] controllers = new MuleProcessController[size];
        File[] muleHomes = new File[size];
        for (int i = 0; i < size; i++)
        {
            File parentDir = new File(mavenProject.getBuild().getDirectory(), "mule" + i);
            muleHomes[i] = new File(parentDir, "mule-enterprise-standalone-" + muleVersion);
            controllers[i] = new MuleProcessController(muleHomes[i].getAbsolutePath());
            if (!muleHomes[i].exists())
            {
                throw new MojoFailureException(muleHomes[i].getAbsolutePath() + "directory does not exist.");
            }
        }
        new Undeployer(getLog(), controllers).execute();
    }

    public void standalone() throws MojoFailureException, MojoExecutionException
    {
        if (!muleHome.exists())
        {
            throw new MojoFailureException("MULE_HOME directory does not exist.");
        }
        getLog().info("Using MULE_HOME: " + muleHome);
        new Undeployer(getLog(), new MuleProcessController(muleHome.getAbsolutePath())).execute();

    }

    private void initializeApplication() throws MojoFailureException
    {
        if (application == null)
        {
            Artifact artifact = resolveMavenProjectArtifact();
            applicationName = artifact.getArtifactId();
            application = artifact.getFile();
            getLog().info("No application configured. Using project artifact: " + artifact.getFile());
        }
        else
        {
            applicationName = application.getName();
        }
    }
}
