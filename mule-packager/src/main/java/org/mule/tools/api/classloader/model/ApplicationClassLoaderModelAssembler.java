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

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver;
import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.ClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.RamlClassloaderModelResolver;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;

import org.apache.maven.model.Model;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mule.maven.client.internal.util.MavenUtils.getPomModelFromFile;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toApplicationModelArtifacts;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.updateArtifactsSharedState;
import static org.mule.tools.api.classloader.model.util.PluginUtils.toPluginDependencies;

public class ApplicationClassLoaderModelAssembler {

  public static final String CLASS_LOADER_MODEL_VERSION = "1.1.0";
  public static final String CLASS_LOADER_MODEL_VERSION_120 = "1.2.0";

  private ApplicationClassloaderModel applicationClassLoaderModel;

  private ApplicationDependencyResolver applicationDependencyResolver;
  private ClassloaderModelResolver mulePluginClassLoaderModelResolver;
  private ClassloaderModelResolver ramlClassLoaderModelResolver;
  private AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver;

  @Deprecated
  public ApplicationClassLoaderModelAssembler(AetherMavenClient aetherMavenClient, File temporaryFolder) {
    this.applicationDependencyResolver = new ApplicationDependencyResolver(aetherMavenClient);
    this.mulePluginClassLoaderModelResolver = new MulePluginClassloaderModelResolver(aetherMavenClient);
    this.ramlClassLoaderModelResolver = new RamlClassloaderModelResolver(aetherMavenClient);
    this.additionalPluginDependenciesResolver =
        new AdditionalPluginDependenciesResolver(aetherMavenClient, emptyList(), temporaryFolder);
  }

  public ApplicationClassLoaderModelAssembler(ApplicationDependencyResolver applicationDependencyResolver,
                                              ClassloaderModelResolver mulePluginClassLoaderModelResolver,
                                              ClassloaderModelResolver ramlClassLoaderModelResolver,
                                              AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver) {
    this.applicationDependencyResolver = applicationDependencyResolver;
    this.mulePluginClassLoaderModelResolver = mulePluginClassLoaderModelResolver;
    this.ramlClassLoaderModelResolver = ramlClassLoaderModelResolver;
    this.additionalPluginDependenciesResolver = additionalPluginDependenciesResolver;
  }

  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile, boolean legacyMode)
      throws IllegalStateException {

    Model pomModel = getPomFile(pomFile);

    ArtifactCoordinates appCoordinates = getApplicationArtifactCoordinates(pomModel);

    AppClassLoaderModel appModel = new AppClassLoaderModel(legacyMode ? CLASS_LOADER_MODEL_VERSION
        : CLASS_LOADER_MODEL_VERSION_120, appCoordinates);

    List<BundleDependency> appDependencies = applicationDependencyResolver.resolveApplicationDependencies(pomFile);

    List<Artifact> dependencies =
        updateArtifactsSharedState(appDependencies, toApplicationModelArtifacts(appDependencies), pomModel);
    appModel.setDependencies(dependencies);

    if (legacyMode) {
      applicationClassLoaderModel = new LegacyApplicationClassloaderModel(appModel);
    } else {
      applicationClassLoaderModel = new ApplicationClassloaderModel(appModel);
    }

    Collection<ClassLoaderModel> pluginsClassLoaderModels = mulePluginClassLoaderModelResolver.resolve(appDependencies);
    applicationClassLoaderModel.mergeDependencies(pluginsClassLoaderModels);

    appModel.setAdditionalPluginDependencies(toPluginDependencies(additionalPluginDependenciesResolver
        .resolveDependencies(appDependencies, pluginsClassLoaderModels)));

    applicationClassLoaderModel.addDirectDependencies(ramlClassLoaderModelResolver.getDependencies());

    return applicationClassLoaderModel;
  }

  protected Model getPomFile(File pomFile) {
    return getPomModelFromFile(pomFile);
  }

  public ArtifactCoordinates getApplicationArtifactCoordinates(Model pomModel) {
    return ArtifactUtils.getApplicationArtifactCoordinates(pomModel);
  }

}
