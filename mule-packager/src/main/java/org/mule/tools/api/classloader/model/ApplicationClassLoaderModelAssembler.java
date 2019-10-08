/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model;

import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static org.mule.maven.client.internal.util.MavenUtils.getPomModelFromFile;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toApplicationModelArtifacts;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.updateArtifactsSharedState;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.updatePackagesResources;
import static org.mule.tools.api.classloader.model.util.PluginUtils.toPluginDependencies;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver;
import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.ClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;
import org.mule.tools.api.util.JarExplorer;
import org.mule.tools.api.util.JarInfo;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.maven.model.Model;

public class ApplicationClassLoaderModelAssembler {

  public static final String CLASS_LOADER_MODEL_VERSION = "1.2.0";
  public static final String CLASSES = "classes";

  private ApplicationClassloaderModel applicationClassLoaderModel;

  private ApplicationDependencyResolver applicationDependencyResolver;
  private ClassloaderModelResolver mulePluginClassLoaderModelResolver;
  private AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver;
  private JarExplorer jarExplorer;

  @Deprecated
  public ApplicationClassLoaderModelAssembler(AetherMavenClient aetherMavenClient, File temporaryFolder) {
    this.applicationDependencyResolver = new ApplicationDependencyResolver(aetherMavenClient);
    this.mulePluginClassLoaderModelResolver = new MulePluginClassloaderModelResolver(aetherMavenClient);
    this.additionalPluginDependenciesResolver =
        new AdditionalPluginDependenciesResolver(aetherMavenClient, emptyList(), temporaryFolder);
  }

  public ApplicationClassLoaderModelAssembler(ApplicationDependencyResolver applicationDependencyResolver,
                                              ClassloaderModelResolver mulePluginClassLoaderModelResolver,
                                              AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver,
                                              JarExplorer jarExplorer) {
    this.applicationDependencyResolver = applicationDependencyResolver;
    this.mulePluginClassLoaderModelResolver = mulePluginClassLoaderModelResolver;
    this.additionalPluginDependenciesResolver = additionalPluginDependenciesResolver;
    this.jarExplorer = jarExplorer;
  }

  @Deprecated
  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile, ApplicationGAVModel appGAVModel)
      throws IllegalStateException {
    return getApplicationClassLoaderModel(pomFile, null, appGAVModel, false);
  }

  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile, File outputDirectory,
                                                                    ApplicationGAVModel appGAVModel,
                                                                    boolean includeTestDependencies)
      throws IllegalStateException {

    Model pomModel = getPomFile(pomFile);

    ArtifactCoordinates appCoordinates = getApplicationArtifactCoordinates(pomModel, appGAVModel);

    AppClassLoaderModel appModel = new AppClassLoaderModel(CLASS_LOADER_MODEL_VERSION, appCoordinates);

    if (outputDirectory != null && get(outputDirectory.getAbsolutePath(), CLASSES).toFile().exists()) {
      JarInfo jarInfo = jarExplorer.explore(get(outputDirectory.getAbsolutePath(), CLASSES).toFile().toURI());
      appModel.setPackages(jarInfo.getPackages().toArray(new String[jarInfo.getPackages().size()]));
      appModel.setResources(jarInfo.getResources().toArray(new String[jarInfo.getResources().size()]));
    }

    List<BundleDependency> appDependencies =
        applicationDependencyResolver.resolveApplicationDependencies(pomFile, includeTestDependencies);

    List<Artifact> dependencies =
        updateArtifactsSharedState(appDependencies, updatePackagesResources(toApplicationModelArtifacts(appDependencies)),
                                   pomModel);
    appModel.setDependencies(dependencies);

    applicationClassLoaderModel = new ApplicationClassloaderModel(appModel);

    Collection<ClassLoaderModel> pluginsClassLoaderModels = mulePluginClassLoaderModelResolver.resolve(appDependencies);
    applicationClassLoaderModel.addAllMulePluginClassloaderModels(pluginsClassLoaderModels);

    appModel.setAdditionalPluginDependencies(toPluginDependencies(additionalPluginDependenciesResolver
        .resolveDependencies(appDependencies, pluginsClassLoaderModels)));

    return applicationClassLoaderModel;
  }

  protected Model getPomFile(File pomFile) {
    return getPomModelFromFile(pomFile);
  }

  public ArtifactCoordinates getApplicationArtifactCoordinates(Model pomModel, ApplicationGAVModel appGAVModel) {
    return ArtifactUtils.getApplicationArtifactCoordinates(pomModel, appGAVModel);
  }

}
