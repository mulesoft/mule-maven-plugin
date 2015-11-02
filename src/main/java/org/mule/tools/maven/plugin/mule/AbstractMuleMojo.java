/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyShell;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractMuleMojo extends AbstractMojo
{

    @Component
    protected MavenProject mavenProject;

    @Component
    protected ArtifactFactory artifactFactory;

    @Component
    protected ArtifactResolver artifactResolver;

    @Parameter(defaultValue = "${localRepository}", readonly = true)
    protected ArtifactRepository localRepository;

    @Parameter (property = "mule.skip")
    protected String skip;

    @Parameter
    protected File domain;

    @Parameter(property = "script", required = false)
    protected File script;

    @Parameter(property = "mule.timeout", required = false)
    protected int timeout;

    /**
     * Anypoint Platform username.
     *
     * @since 2.0
     */
    @Parameter(required = false, readonly = true, property = "anypoint.username")
    protected String username;

    /**
     * Anypoint Platform password.
     *
     * @since 2.0
     */
    @Parameter(required = false, readonly = true, property = "anypoint.password")
    protected String password;

    /**
     * Deployment information.
     *
     * @since 1.0
     */
    @Parameter(required = true, readonly = true)
    protected DeploymentType deploymentType;

    /**
     * Anypoint Platform URI, can be configured to use with On Premise platform..
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "anypoint.uri", defaultValue = "https://anypoint.mulesoft.com")
    protected String uri;

    /**
     * Anypoint environment name.
     *
     * @since 2.0
     */
    @Parameter(required = false, readonly = true, property = "anypoint.environment")
    protected String environment;

    /**
     * Path to a Mule Standalone server.
     * This parameter and <code>muleDistribution</code> and <code>muleVersion</code> are mutual exclusive.
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "mule.home")
    protected File muleHome;

    /**
     * Version of the Mule ESB Enterprise distribution to download. If you need to use Community version use <code>muleDistribution</code> parameter.
     * This parameter and <code>muleDistribution</code> are mutual exclusive.
     *
     * @since 1.0
     */
    @Parameter(readonly = true, property = "mule.version")
    protected String muleVersion;

    /**
     * Number of cluster nodes.
     *
     * @since 1.0
     */
    @Parameter(defaultValue = "2", readonly = true, required = true)
    protected Integer size;

    /**
     * Business group for deployment, if it is a nested one its format should be first.second.
     *
     * @since 2.1
     */
    @Parameter(defaultValue = "", property = "anypoint.businessGroup")
    protected String businessGroup = "";

    /**
     * Use insecure mode for ARM deployment: do not validate certificates, nor hostname.
     *
     * @since 2.1
     */
    @Parameter(defaultValue = "Medium", readonly = true, property = "arm.insecure")
    protected boolean armInsecure;

    @Parameter
    private List<ArtifactDescription> artifactItems = new ArrayList<ArtifactDescription>();

    /**
     * Application file to be deployed.
     *
     * @since 1.0
     */
    @Parameter(property = "mule.application")
    protected File application;

    /**
     * Name of the application to deploy/undeploy. If not specified, the artifact id will be used as
     * the name. This parameter allows to override this behavior to specify a custom name.
     *
     * @since 2.0
     */
    @Parameter(readonly = true, property = "applicationName")
    protected String applicationName;


    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (StringUtils.isNotEmpty(skip) && "true".equals(skip))
        {
            getLog().info("Skipping execution: skip=" + skip);
        }
        else
        {
            getLog().debug("Executing mojo, skip=" + skip);
            doExecute();
        }
    }

    protected abstract void doExecute() throws MojoFailureException, MojoExecutionException;

    protected File getDependency(ArtifactDescription artifactDescription)
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            Artifact artifact = artifactFactory.createArtifact(artifactDescription.getGroupId(),
                                                               artifactDescription.getArtifactId(), artifactDescription.getVersion(), null,
                                                               artifactDescription.getType());
            getLog().info("Resolving " + artifact);
            artifactResolver.resolve(artifact, mavenProject.getRemoteArtifactRepositories(), localRepository);
            return artifact.getFile();
        }
        catch (AbstractArtifactResolutionException e)
        {
            throw new MojoExecutionException("Couldn't download artifact: " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            throw new MojoFailureException("Couldn't download artifact: " + e.getMessage());
        }
    }


    protected void addDomain(Deployer deployer) throws MojoFailureException
    {
        if (domain != null && domain.exists())
        {
            getLog().debug("Adding domain with configuration: " + domain);
            deployer.addDomain(domain);
        }
        else
        {
            getLog().debug("Domain configuration not found: " + domain);
        }
    }

    protected String readFile( String file ) throws IOException
    {
        BufferedReader reader = new BufferedReader( new FileReader(file));
        String line;
        StringBuilder  stringBuilder = new StringBuilder();

        while( ( line = reader.readLine() ) != null )
        {
            stringBuilder.append( line );
            stringBuilder.append( "\n" );
        }

        return stringBuilder.toString();
    }

    protected void executeGroovyScript() throws MojoExecutionException
    {
        GroovyShell shell = new GroovyShell();
        getLog().info("executing script: " + script.getAbsolutePath());
        shell.setProperty("basedir",mavenProject.getBasedir());

        for (Map.Entry entry: mavenProject.getProperties().entrySet())
        {
            shell.setProperty((String) entry.getKey(), entry.getValue());
        }

        getLog().info(mavenProject.getBasedir().getAbsolutePath());
        try
        {
            shell.evaluate(readFile(script.getAbsolutePath()));
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("error executing script: " + script.getAbsolutePath() + "\n" + e.getMessage() );
        }
    }

    protected void addDependencies(Deployer deployer) throws MojoFailureException, MojoExecutionException
    {
        List<File> libraries = new ArrayList<File>();
        for (ArtifactDescription artifact: artifactItems)
        {
            libraries.add(this.getDependency(artifact));
        }
        deployer.addLibraries(libraries);
    }

    protected Artifact resolveMavenProjectArtifact() throws MojoFailureException
    {
        Artifact artifact = artifactFactory.createArtifact(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(), "", "zip");
        try
        {
            artifactResolver.resolve(artifact, new ArrayList<ArtifactRepository>(), localRepository);
        }
        catch (ArtifactResolutionException e)
        {
            throw new MojoFailureException("Couldn't resolve artifact [" + artifact + "]");
        }
        catch (ArtifactNotFoundException e)
        {
            throw new MojoFailureException("Couldn't resolve artifact [" + artifact + "]");
        }

        return artifact;
    }


    protected void initializeApplication() throws MojoFailureException
    {
        if (application == null)
        {
            Artifact artifact = resolveMavenProjectArtifact();
            application = resolveMavenProjectArtifact().getFile();
            getLog().info("No application configured. Using project artifact: " + application);

            if (applicationName == null)
            {
                applicationName = artifact.getArtifactId();
            }
        }
        else
        {
            // If an application is defined but no application name is provided, use the name of the file instead of
            // the artifact ID (expected behavior in standalone deployment for example).
            if (applicationName == null)
            {
                applicationName = application.getName();
            }
        }
    }

    public enum DeploymentType
    {
        standalone,
        cluster,
        cloudhub,
        arm,
        agent
    }
}
