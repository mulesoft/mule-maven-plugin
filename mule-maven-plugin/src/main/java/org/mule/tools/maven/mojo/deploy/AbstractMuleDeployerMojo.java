/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy;

import groovy.lang.GroovyShell;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.mule.tools.client.standalone.Deployer;
import org.mule.tools.maven.mojo.model.ArtifactDescription;
import org.mule.tools.maven.mojo.model.DeploymentConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractMuleDeployerMojo extends AbstractMojo {

  @Parameter
  protected DeploymentConfiguration deploymentConfiguration;

  @Component
  protected Settings settings;

  @Component
  protected SettingsDecrypter decrypter;

  @Component
  protected MavenProject mavenProject;

  @Component
  protected ArtifactFactory artifactFactory;

  @Component
  protected ArtifactResolver artifactResolver;

  @Parameter(defaultValue = "${localRepository}", readonly = true)
  protected ArtifactRepository localRepository;

  /**
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (StringUtils.isNotEmpty(deploymentConfiguration.getSkip()) && "true".equals(deploymentConfiguration.getSkip())) {
      getLog().info("Skipping execution: skip=" + deploymentConfiguration.getSkip());
    } else {
      getLog().debug("Executing mojo, skip=" + deploymentConfiguration.getSkip());
      doExecute();
    }
  }

  protected abstract void doExecute() throws MojoFailureException, MojoExecutionException;

  protected File getDependency(ArtifactDescription artifactDescription)
      throws MojoExecutionException, MojoFailureException {
    try {
      Artifact artifact = artifactFactory.createArtifact(artifactDescription.getGroupId(),
                                                         artifactDescription.getArtifactId(), artifactDescription.getVersion(),
                                                         null,
                                                         artifactDescription.getType());
      getLog().info("Resolving " + artifact);
      artifactResolver.resolve(artifact, mavenProject.getRemoteArtifactRepositories(), localRepository);
      return artifact.getFile();
    } catch (AbstractArtifactResolutionException e) {
      throw new MojoExecutionException("Couldn't download artifact: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new MojoFailureException("Couldn't download artifact: " + e.getMessage());
    }
  }


  protected void addDomain(Deployer deployer) throws MojoFailureException {
    if (deploymentConfiguration.getDomain() != null && deploymentConfiguration.getDomain().exists()) {
      getLog().debug("Adding domain with configuration: " + deploymentConfiguration.getDomain());
      deployer.addDomain(deploymentConfiguration.getDomain());
    } else {
      getLog().debug("Domain configuration not found: " + deploymentConfiguration.getDomain());
    }
  }

  protected String readFile(String file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    StringBuilder stringBuilder = new StringBuilder();

    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append("\n");
    }

    return stringBuilder.toString();
  }

  protected void executeGroovyScript() throws MojoExecutionException {
    GroovyShell shell = new GroovyShell();
    getLog().info("executing script: " + deploymentConfiguration.getScript().getAbsolutePath());
    shell.setProperty("basedir", mavenProject.getBasedir());

    for (Map.Entry entry : mavenProject.getProperties().entrySet()) {
      shell.setProperty((String) entry.getKey(), entry.getValue());
    }

    getLog().info(mavenProject.getBasedir().getAbsolutePath());
    try {
      shell.evaluate(readFile(deploymentConfiguration.getScript().getAbsolutePath()));
    } catch (IOException e) {
      throw new MojoExecutionException("error executing script: " + deploymentConfiguration.getScript().getAbsolutePath() + "\n"
          + e.getMessage());
    }
  }

  protected void addDependencies(Deployer deployer) throws MojoFailureException, MojoExecutionException {
    List<File> libraries = new ArrayList<File>();
    for (ArtifactDescription artifact : deploymentConfiguration.getArtifactItems()) {
      libraries.add(this.getDependency(artifact));
    }
    deployer.addLibraries(libraries);
  }

  protected Artifact resolveMavenProjectArtifact() throws MojoFailureException {
    Artifact artifact = artifactFactory.createArtifact(mavenProject.getGroupId(), mavenProject.getArtifactId(),
                                                       mavenProject.getVersion(), "", "zip");
    try {
      artifactResolver.resolve(artifact, new ArrayList<ArtifactRepository>(), localRepository);
    } catch (ArtifactResolutionException e) {
      throw new MojoFailureException("Couldn't resolve artifact [" + artifact + "]");
    } catch (ArtifactNotFoundException e) {
      throw new MojoFailureException("Couldn't resolve artifact [" + artifact + "]");
    }

    return artifact;
  }


  protected void initializeApplication() throws MojoFailureException {
    if (deploymentConfiguration.getApplication() == null) {
      Artifact artifact = resolveMavenProjectArtifact();
      deploymentConfiguration.setApplication(resolveMavenProjectArtifact().getFile());
      getLog().info("No application configured. Using project artifact: " + deploymentConfiguration.getApplication());

      if (deploymentConfiguration.getApplicationName() == null) {
        deploymentConfiguration.setApplicationName(artifact.getArtifactId());
      }
    } else {
      // If an application is defined but no application name is provided, use the name of the file instead of
      // the artifact ID (expected behavior in standalone deploymentConfiguration for example).
      if (deploymentConfiguration.getApplicationName() == null) {
        deploymentConfiguration.setApplicationName(deploymentConfiguration.getApplication().getName());
      }
    }
  }


  protected void initializeEnvironment() throws MojoExecutionException {
    if (deploymentConfiguration.getServer() != null) {
      Server serverObject = this.settings.getServer(deploymentConfiguration.getServer());
      if (serverObject == null) {
        getLog().error("Server [" + deploymentConfiguration.getServer() + "] not found in settings file.");
        throw new MojoExecutionException("Server [" + deploymentConfiguration.getServer() + "] not found in settings file.");
      }
      // Decrypting Maven server, in case of plain text passwords returns the same
      serverObject = decrypter.decrypt(new DefaultSettingsDecryptionRequest(serverObject)).getServer();
      if (StringUtils.isNotEmpty(deploymentConfiguration.getUsername())
          || StringUtils.isNotEmpty(deploymentConfiguration.getPassword())) {
        getLog().warn("Both server and credentials are configured. Using plugin configuration credentials.");
      } else {
        deploymentConfiguration.setUsername(serverObject.getUsername());
        deploymentConfiguration.setPassword(serverObject.getPassword());
      }
    }

    String ibmJdkSupport = System.getProperty("ibm.jdk.support");
    if ("true".equals(ibmJdkSupport)) {
      getLog().debug("Attempting to provide support for IBM JDK...");
      try {
        Field methods = HttpURLConnection.class.getDeclaredField("methods");
        methods.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);

        modifiers.setInt(methods, methods.getModifiers() & ~Modifier.FINAL);

        String[] actualMethods = {"GET", "POST", "HEAD", "OPTIONS", "PUT", "PATCH", "DELETE", "TRACE"};
        methods.set(null, actualMethods);

      } catch (NoSuchFieldException e) {
        getLog().error("Fail to provide support for IBM JDK", e);
      } catch (IllegalAccessException e) {
        getLog().error("Fail to provide support for IBM JDK", e);
      }
    }
  }

  public enum DeploymentType {
    standalone, cluster, cloudhub, arm, agent
  }
}
