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

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
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
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


@Mojo(name = "process-sources",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ProcessSourcesMojo extends AbstractMuleMojo {

  @Parameter(defaultValue = "${skipPluginCompatibilityValidation}")
  protected boolean skipPluginCompatibilityValidation = false;
  protected final MulePluginsCompatibilityValidator mulePluginsCompatibilityValidator = new MulePluginsCompatibilityValidator();

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Processing sources...");
    if (!(lightweightPackage && skipPluginCompatibilityValidation)) {
      RepositoryGenerator repositoryGenerator =
          new RepositoryGenerator(session.getCurrentProject().getFile(), outputDirectory,
                                  new ArtifactInstaller(new MavenPackagerLog(getLog())),
                                  getClassLoaderModelAssembler(), legacyMode);
      try {
        ClassLoaderModel classLoaderModel = repositoryGenerator.generate(!(lightweightPackage || useLocalRepository));
        for (SharedLibraryDependency sharedLibraryDependency : sharedLibraries) {
          classLoaderModel.getDependencies().stream()
              .filter(dep -> dep.getArtifactCoordinates().getArtifactId().equals(sharedLibraryDependency.getArtifactId()) &&
                  dep.getArtifactCoordinates().getGroupId().equals(sharedLibraryDependency.getGroupId()))
              .findFirst().ifPresent(dep -> dep.setShared(true));
        }
        Project project = getProject(classLoaderModel);
        mulePluginsCompatibilityValidator.validate(getResolver(project).resolve());
        if (!lightweightPackage || (lightweightPackage && useLocalRepository)) {
          if (lightweightPackage && useLocalRepository) {
            classLoaderModel = new NotParameterizedClasLoaderModel(classLoaderModel);
          }
          ((MuleContentGenerator) getContentGenerator()).createApplicationClassLoaderModelJsonFile(classLoaderModel);
        }
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
            .collect(Collectors.toList());
      }

      @Override
      public List<BundleDependency> getBundleDependencies() {
        return dependencyProject.getBundleDependencies();
      }
    };
  }

  /**
   * Decorator for {@link ClassLoaderModel} that will not resolve the URIs
   * parameterized, used when building a class loader model that should reference to the resolved
   * artifact URIs in the local Maven repository.
   */
  private class NotParameterizedClasLoaderModel extends ClassLoaderModel {

    private final ClassLoaderModel classLoaderModel;

    public NotParameterizedClasLoaderModel(ClassLoaderModel classLoaderModel) {
      super(classLoaderModel.getVersion(), classLoaderModel.getArtifactCoordinates());
      this.classLoaderModel = classLoaderModel;
    }

    @Override
    public String getVersion() {
      return classLoaderModel.getVersion();
    }

    @Override
    public ArtifactCoordinates getArtifactCoordinates() {
      return classLoaderModel.getArtifactCoordinates();
    }

    @Override
    public List<Artifact> getDependencies() {
      return classLoaderModel.getDependencies();
    }

    @Override
    public void setDependencies(List<Artifact> dependencies) {
      classLoaderModel.setDependencies(dependencies);
    }

    @Override
    public List<Artifact> getArtifacts() {
      return classLoaderModel.getArtifacts();
    }

    @Override
    public ClassLoaderModel getParametrizedUriModel() {
      return this.classLoaderModel;
    }
  }
}
