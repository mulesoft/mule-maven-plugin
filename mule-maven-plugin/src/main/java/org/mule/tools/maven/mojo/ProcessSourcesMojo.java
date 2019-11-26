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
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import org.mule.tools.api.util.MavenComponents;
import org.mule.tools.api.util.SourcesProcessor;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


@Mojo(name = "process-sources",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ProcessSourcesMojo extends AbstractMuleMojo {

  /**
   * @deprecated Should not be considered as validations for compatible plugins is already done when resolving dependencies.
   */
  @Deprecated
  @Parameter(defaultValue = "${skipPluginCompatibilityValidation}")
  protected boolean skipPluginCompatibilityValidation = false;

  @Parameter(defaultValue = "${prettyPrinting}")
  protected boolean prettyPrinting = false;

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Processing sources...");
    if (skipPluginCompatibilityValidation) {
      getLog()
          .warn(
                "Ignoring skipPluginCompatibilityValidation property as it is deprecated. Compatibility between mule-plugin versions is always done.");
    }

    MavenComponents mavenComponents =
        new MavenComponents()
            .withLog(getLog())
            .withProject(project)
            .withOutputDirectory(outputDirectory)
            .withSession(session)
            .withSharedLibraries(sharedLibraries)
            .withProjectBuilder(projectBuilder)
            .withRepositorySystem(repositorySystem)
            .withLocalRepository(localRepository)
            .withRemoteArtifactRepositories(remoteArtifactRepositories)
            .withClassifier(classifier)
            .withAdditionalPluginDependencies(additionalPluginDependencies)
            .withProjectBaseFolder(projectBaseFolder);

    SourcesProcessor sourcesProcessor = new SourcesProcessor(mavenComponents);

    try {
      sourcesProcessor
          .process(prettyPrinting, lightweightPackage, useLocalRepository, testJar, outputDirectory,
                   getProjectInformation().getBuildDirectory().resolve(META_INF.value()).resolve(MULE_ARTIFACT.value()).toFile());
    } catch (Exception e) {
      String message = format("There was an exception while creating the repository of [%s]", project.toString());
      throw new MojoFailureException(message, e);
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PROCESS_SOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }

}
