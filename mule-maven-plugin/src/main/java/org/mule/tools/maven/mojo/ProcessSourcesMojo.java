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
import static java.util.stream.Collectors.toList;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.NotParameterizedClassLoaderModel;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver;
import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.RamlClassloaderModelResolver;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.repository.ArtifactInstaller;
import org.mule.tools.api.repository.RepositoryGenerator;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.validation.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.utils.DependencyProject;
import org.mule.tools.maven.utils.MavenPackagerLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
  protected final MulePluginsCompatibilityValidator mulePluginsCompatibilityValidator = new MulePluginsCompatibilityValidator();

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Processing sources...");
    if (skipPluginCompatibilityValidation) {
      getLog()
          .warn("Ignoring skipPluginCompatibilityValidation property as it is deprecated. Compatibility between mule-plugin versions is always done.");
    }

    boolean isHeavyWeight = !lightweightPackage;
    boolean isLightWeightUsingLocalRepository = lightweightPackage && useLocalRepository;
    if (isLightWeightUsingLocalRepository || isHeavyWeight) {
      try {
        RepositoryGenerator repositoryGenerator =
            new RepositoryGenerator(session.getCurrentProject().getFile(), outputDirectory,
                                    new ArtifactInstaller(new MavenPackagerLog(getLog())),
                                    getClassLoaderModelAssembler());
        ClassLoaderModel classLoaderModel = repositoryGenerator.generate(lightweightPackage, useLocalRepository);
        for (SharedLibraryDependency sharedLibraryDependency : sharedLibraries) {
          classLoaderModel.getDependencies().stream()
              .filter(dep -> dep.getArtifactCoordinates().getArtifactId().equals(sharedLibraryDependency.getArtifactId()) &&
                  dep.getArtifactCoordinates().getGroupId().equals(sharedLibraryDependency.getGroupId()))
              .findFirst().ifPresent(dep -> {
                if (!validateMuleRuntimeSharedLibrary(dep.getArtifactCoordinates().getArtifactId(),
                                                      dep.getArtifactCoordinates().getGroupId())) {
                  dep.setShared(true);
                }
              });
        }
        Project project = getProject(classLoaderModel);
        mulePluginsCompatibilityValidator.validate(getResolver(project).resolve());
        if (isLightWeightUsingLocalRepository) {
          classLoaderModel = new NotParameterizedClassLoaderModel(classLoaderModel);
        }
        ((MuleContentGenerator) getContentGenerator()).createApplicationClassLoaderModelJsonFile(classLoaderModel);
      } catch (Exception e) {
        String message = format("There was an exception while creating the repository of [%s]", project.toString());
        throw new MojoFailureException(message, e);
      }
    }
  }

  protected ApplicationClassLoaderModelAssembler getClassLoaderModelAssembler() {
    AetherMavenClient aetherMavenClient = getAetherMavenClient();
    return new ApplicationClassLoaderModelAssembler(new ApplicationDependencyResolver(aetherMavenClient),
                                                    new MulePluginClassloaderModelResolver(aetherMavenClient),
                                                    new RamlClassloaderModelResolver(aetherMavenClient),
                                                    new AdditionalPluginDependenciesResolver(aetherMavenClient,
                                                                                             additionalPluginDependencies == null
                                                                                                 ? new ArrayList<>()
                                                                                                 : additionalPluginDependencies,
                                                                                             new File(outputDirectory, "temp")));
  }

  protected final boolean validateMuleRuntimeSharedLibrary(String groupId, String artifactId) {
    if ("org.mule.runtime".equals(groupId)
        || "com.mulesoft.mule.runtime.modules".equals(groupId)) {
      getLog().warn("Shared library '" + groupId + ":" + artifactId
          + "' is a Mule Runtime dependency. It will not be shared by the app in order to avoid classloading issues. Please consider removing it, or at least not putting it as a sharedLibrary.");
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PROCESS_SOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }

  public Project getProject(ClassLoaderModel classLoaderModel) {
    Project dependencyProject = new DependencyProject(project);
    return new Project() {

      @Override
      public List<ArtifactCoordinates> getDependencies() {
        return classLoaderModel.getDependencies()
            .stream()
            .map(Artifact::getArtifactCoordinates)
            .collect(toList());
      }

      @Override
      public List<BundleDependency> getBundleDependencies() {
        return dependencyProject.getBundleDependencies();
      }
    };
  }
}
