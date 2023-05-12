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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.packager.resources.content.ResourcesContent;
import org.mule.tools.api.packager.sources.ContentGenerator;
import org.mule.tools.api.packager.sources.ContentGeneratorFactory;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.validation.resolver.MulePluginResolver;
import org.mule.tools.api.util.MavenProjectBuilder;
import org.mule.tools.maven.utils.MuleApplicationModelLoader;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.mule.tools.maven.utils.MuleApplicationModelLoader.MULE_ARTIFACT_JSON_FILE_NAME;

/**
 * Base Mojo
 */
public abstract class AbstractMuleMojo extends AbstractGenericMojo {

  @Component
  protected ProjectBuilder projectBuilder;

  @Component
  protected RepositorySystem repositorySystem;

  @Parameter(property = "project.build.directory", required = true)
  protected File outputDirectory;

  @Parameter(defaultValue = "${skipValidation}")
  protected boolean skipValidation = false;

  @Parameter(property = "runtimeVersion")
  public String runtimeVersion;

  protected ContentGenerator contentGenerator;

  protected static ResourcesContent resourcesContent;


  public void execute() throws MojoExecutionException, MojoFailureException {
    if (!hasExecutedBefore()) {
      initMojo();
      doExecute();
    } else {
      getLog().debug("Skipping execution because it has already been run");
    }
  }

  public ContentGenerator getContentGenerator() {
    if (contentGenerator == null) {
      contentGenerator = ContentGeneratorFactory.create(getProjectInformation(), project.getModel().getParent());
    }
    return contentGenerator;
  }

  protected MulePluginResolver getResolver(Project project) {
    MavenProjectBuilder builder = new MavenProjectBuilder(getLog(), session, projectBuilder, repositorySystem, localRepository,
                                                          remoteArtifactRepositories);
    return new MulePluginResolver(builder, project);
  }

  protected MuleApplicationModelLoader getMuleApplicationModelLoader() throws MojoExecutionException {
    return new MuleApplicationModelLoader(getMuleApplicationModel(), getLog()).withRuntimeVersion(runtimeVersion);
  }

  protected MuleApplicationModel getMuleApplicationModel() throws MojoExecutionException {
    File muleApplicationJsonPath = getMuleApplicationJsonPath();
    try {
      return new MuleApplicationModelJsonSerializer().deserialize(readFileToString(muleApplicationJsonPath, defaultCharset()));
    } catch (IOException e) {
      String message = "Fail to read mule application file from " + muleApplicationJsonPath;
      getLog().error(message, e);
      throw new MojoExecutionException(message, e);
    }
  }

  protected File getMuleApplicationJsonPath() {
    return Paths.get(project.getBasedir().getPath()).resolve(MULE_ARTIFACT_JSON_FILE_NAME)
        .toFile();
  }
}
