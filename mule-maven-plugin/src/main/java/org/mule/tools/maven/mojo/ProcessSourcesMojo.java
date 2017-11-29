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

import static java.lang.String.format;

import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.repository.ArtifactInstaller;
import org.mule.tools.api.repository.RepositoryGenerator;
import org.mule.tools.api.validation.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.utils.MavenPackagerLog;


@Mojo(name = "process-sources",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ProcessSourcesMojo extends AbstractMuleMojo {

  @Parameter(defaultValue = "${skipPluginCompatibilityValidation}")
  protected boolean skipPluginCompatibilityValidation = false;
  protected final MulePluginsCompatibilityValidator mulePluginsCompatibilityValidator = new MulePluginsCompatibilityValidator();

  @Override
  public void doExecute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Processing sources...");
    if (!(lightweightPackage && skipPluginCompatibilityValidation)) {
      RepositoryGenerator repositoryGenerator =
          new RepositoryGenerator(project.getFile(), outputDirectory, new ArtifactInstaller(new MavenPackagerLog(getLog())),
                                  getClassLoaderModelAssembler());
      try {
        ClassLoaderModel classLoaderModel = repositoryGenerator.generate();

        mulePluginsCompatibilityValidator.validate(getResolver().resolveMulePlugins(
                                                                                    () -> classLoaderModel.getDependencies()
                                                                                        .stream()
                                                                                        .map(Artifact::getArtifactCoordinates)
                                                                                        .collect(Collectors.toList())));
        if (!lightweightPackage) {
          ((MuleContentGenerator) getContentGenerator()).createApplicationClassLoaderModelJsonFile(classLoaderModel);
        }
      } catch (Exception e) {
        String message = format("There was an exception while creating the repository of [%s]", project.toString());
        throw new MojoFailureException(message, e);
      }
    }
  }


  protected ApplicationClassLoaderModelAssembler getClassLoaderModelAssembler() {
    return new ApplicationClassLoaderModelAssembler(getAetherMavenClient());
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PROCESS_SOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}
