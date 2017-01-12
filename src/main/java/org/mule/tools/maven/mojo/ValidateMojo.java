/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.mule.tools.maven.resolver.MulePluginResolver;
import org.mule.tools.maven.util.MulePluginsCompatibilityValidator;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "validate",
    defaultPhase = LifecyclePhase.VALIDATE,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ValidateMojo extends AbstractMuleMojo {

    @Component
    private RepositorySystem repositorySystem;

    @Component
    private ProjectBuilder projectBuilder;

    @Parameter(readonly = true, required = true, defaultValue = "${session}")
    private MavenSession session;

    @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
    private List<ArtifactRepository> remoteArtifactRepositories;

    @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
    private ArtifactRepository localRepository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Validating Mule application...");

        validateMandatoryFolders();
        validateMandatoryDescriptors();
        validateMulePluginDependencies();

        getLog().debug("Validating Mule application done");
    }

    private void validateMandatoryFolders() throws MojoExecutionException {
        if (!muleSourceFolder.exists()) {
            String message = String.format("Invalid Mule project. Missing src/main/mule folder. This folder is mandatory");
            throw new MojoExecutionException(message);
        }
    }

    private void validateMandatoryDescriptors() throws MojoExecutionException {
        File muleConfigFile = Paths.get(projectBaseFolder.toString(), MULE_CONFIG_XML).toFile();
        File muleAppPropertiesFile = Paths.get(projectBaseFolder.toString(), MULE_APP_PROPERTIES).toFile();
        File muleDeployPropertiesFile = Paths.get(projectBaseFolder.toString(), MULE_DEPLOY_PROPERTIES).toFile();

        if (!muleAppPropertiesFile.exists()) {
            String message =
                String.format("Invalid Mule project. Missing %s file, it must be present in the root of application",
                              MULE_APP_PROPERTIES);
            throw new MojoExecutionException(message);
        }

        if (!muleDeployPropertiesFile.exists() && !muleConfigFile.exists()) {
            String message =
                String.format("Invalid Mule project. Either %s or %s files must be present in the root of application",
                              MULE_DEPLOY_PROPERTIES, MULE_CONFIG_XML);
            throw new MojoExecutionException(message);
        }
    }

    private void validateMulePluginDependencies() throws MojoExecutionException {
        MulePluginResolver resolver = new MulePluginResolver(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                                             remoteArtifactRepositories);

        new MulePluginsCompatibilityValidator().validate(resolver.resolveMulePlugins(project));
    }
}
