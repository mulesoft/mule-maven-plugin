/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.ApplicationGAVModel;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.NotParameterizedClassLoaderModel;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver;
import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.packager.sources.ContentGenerator;
import org.mule.tools.api.packager.sources.ContentGeneratorFactory;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.repository.ArtifactInstaller;
import org.mule.tools.api.repository.MuleMavenPluginClientBuilder;
import org.mule.tools.api.repository.RepositoryGenerator;
import org.mule.tools.api.validation.MulePluginsCompatibilityValidator;
import org.mule.tools.api.validation.resolver.MulePluginResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.execution.MavenExecutionRequest;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * For heavyweight or lightweight that use local repository, generates classloader-model.json and repository folder
 */
public class SourcesProcessor {

  protected final MulePluginsCompatibilityValidator mulePluginsCompatibilityValidator = new MulePluginsCompatibilityValidator();

  protected MavenComponents mavenComponents;

  public SourcesProcessor(MavenComponents mavenComponents) {
    checkArgument(mavenComponents != null, "The mavenComponents must not be null");

    this.mavenComponents = mavenComponents;
  }


  /**
   *
   * @param prettyPrinting if {@code true} the classloader-model.json will be printed with pretty print mode
   * @param lightweightPackage if {@code true} generate a lightweight structure else generate a heavyweight structure
   * @param useLocalRepository if {@code true} generate the repository with all the application dependencies
   * @param testJar if {@code true} repository is going to be generated with test dependencies
   * @param repositoryOutputDirectory destination folder where the repository folder is going to be created
   * @param classloaderOutputDirectory destination folder where the classloader-model.json file is going to be created
   * @throws Exception
   */
  public void process(boolean prettyPrinting, boolean lightweightPackage, boolean useLocalRepository, boolean testJar,
                      File repositoryOutputDirectory, File classloaderOutputDirectory)
      throws Exception {

    boolean isHeavyWeight = !lightweightPackage;
    boolean isLightWeightUsingLocalRepository = lightweightPackage && useLocalRepository;
    if (isLightWeightUsingLocalRepository || isHeavyWeight) {
      ApplicationGAVModel appGAV =
          new ApplicationGAVModel(mavenComponents.getProject().getGroupId(), mavenComponents.getProject().getArtifactId(),
                                  mavenComponents.getProject().getVersion());
      RepositoryGenerator repositoryGenerator =
          new RepositoryGenerator(mavenComponents.getProject().getFile(),
                                  repositoryOutputDirectory,
                                  new ArtifactInstaller(new MavenPackagerLog(mavenComponents.getLog())),
                                  getClassLoaderModelAssembler(), appGAV);
      ClassLoaderModel classLoaderModel =
          repositoryGenerator.generate(lightweightPackage, useLocalRepository, prettyPrinting, testJar);

      for (SharedLibraryDependency sharedLibraryDependency : mavenComponents.getSharedLibraries()) {
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
      ((MuleContentGenerator) getContentGenerator(testJar, lightweightPackage))
          .createApplicationClassLoaderModelJsonFile(classLoaderModel,
                                                     prettyPrinting, classloaderOutputDirectory);
    }
  }

  protected ApplicationClassLoaderModelAssembler getClassLoaderModelAssembler() {
    AetherMavenClient aetherMavenClient = getAetherMavenClient();
    return new ApplicationClassLoaderModelAssembler(new ApplicationDependencyResolver(aetherMavenClient),
                                                    new MulePluginClassloaderModelResolver(aetherMavenClient),
                                                    new AdditionalPluginDependenciesResolver(aetherMavenClient,
                                                                                             mavenComponents
                                                                                                 .getAdditionalPluginDependencies() == null
                                                                                                     ? new ArrayList<>()
                                                                                                     : mavenComponents
                                                                                                         .getAdditionalPluginDependencies(),
                                                                                             new File(mavenComponents
                                                                                                 .getOutputDirectory(),
                                                                                                      "temp")),
                                                    new FileJarExplorer());
  }

  protected final boolean validateMuleRuntimeSharedLibrary(String groupId, String artifactId) {
    if ("org.mule.runtime".equals(groupId)
        || "com.mulesoft.mule.runtime.modules".equals(groupId)) {
      mavenComponents.getLog().warn("Shared library '" + groupId + ":" + artifactId
          + "' is a Mule Runtime dependency. It will not be shared by the app in order to avoid classloading issues. Please consider removing it, or at least not putting it as a sharedLibrary.");
      return true;
    } else {
      return false;
    }
  }

  protected Project getProject(ClassLoaderModel classLoaderModel) {
    Project dependencyProject = new DependencyProject(mavenComponents.getProject());
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

  protected ContentGenerator getContentGenerator(boolean testJar, boolean lightweightPackage) {
    return ContentGeneratorFactory.create(getProjectInformation(testJar, lightweightPackage));
  }

  protected MulePluginResolver getResolver(Project project) {
    MavenProjectBuilder builder =
        new MavenProjectBuilder(mavenComponents.getLog(), mavenComponents.getSession(), mavenComponents.getProjectBuilder(),
                                mavenComponents.getRepositorySystem(), mavenComponents.getLocalRepository(),
                                mavenComponents.getRemoteArtifactRepositories());
    return new MulePluginResolver(builder, project);
  }

  protected ProjectInformation getProjectInformation(boolean testJar, boolean lightweightPackage) {
    return MavenProjectInformation.getProjectInformation(mavenComponents.getSession(), mavenComponents.getProject(),
                                                         mavenComponents.getProjectBaseFolder(), testJar, null,
                                                         getClassifier(testJar, lightweightPackage));
  }

  protected String getClassifier(boolean testJar, boolean lightweightPackage) {
    return getPackagingType().resolveClassifier(mavenComponents.getClassifier(), lightweightPackage, testJar);
  }

  protected PackagingType getPackagingType() {
    return PackagingType.fromString(mavenComponents.getProject().getPackaging());
  }

  protected AetherMavenClient getAetherMavenClient() {
    MavenExecutionRequest request = mavenComponents.getSession().getRequest();
    List<RemoteRepository> remoteRepositories = RepositoryUtils.toRepos(mavenComponents.getRemoteArtifactRepositories());
    AetherMavenClient aetherMavenClient = new MuleMavenPluginClientBuilder(new MavenPackagerLog(mavenComponents.getLog()))
        .withRemoteRepositories(remoteRepositories)
        .withLocalRepository(request.getLocalRepositoryPath())
        .withUserSettings(request.getUserSettingsFile())
        .withGlobalSettings(request.getGlobalSettingsFile())
        .withUserProperties(request.getUserProperties())
        .withActiveProfiles(request.getActiveProfiles())
        .withInactiveProfiles(request.getInactiveProfiles())
        .build();

    return aetherMavenClient;
  }
}
