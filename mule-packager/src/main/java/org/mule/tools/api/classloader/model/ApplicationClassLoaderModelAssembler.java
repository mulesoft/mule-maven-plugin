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

import static java.util.Collections.emptyList;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toApplicationModelArtifacts;
import static org.mule.tools.api.classloader.model.util.PluginUtils.toPluginDependencies;

import org.mule.maven.client.api.model.BundleDependency;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.resolver.AdditionalPluginDependenciesResolver;
import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.ClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.RamlClassloaderModelResolver;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;

public class ApplicationClassLoaderModelAssembler {

  public static final String CLASS_LOADER_MODEL_VERSION = "1.1.0";

  private ApplicationClassloaderModel applicationClassLoaderModel;

  private ApplicationDependencyResolver applicationDependencyResolver;
  private ClassloaderModelResolver mulePluginClassLoaderModelResolver;
  private ClassloaderModelResolver ramlClassLoaderModelResolver;
  private AdditionalPluginDependenciesResolver additionalPluginDependenciesResolver;



  @Deprecated
  public ApplicationClassLoaderModelAssembler(AetherMavenClient aetherMavenClient) {
    this.applicationDependencyResolver = new ApplicationDependencyResolver(aetherMavenClient);
    this.mulePluginClassLoaderModelResolver = new MulePluginClassloaderModelResolver(aetherMavenClient);
    this.ramlClassLoaderModelResolver = new RamlClassloaderModelResolver(aetherMavenClient);
    this.additionalPluginDependenciesResolver = new AdditionalPluginDependenciesResolver(aetherMavenClient, emptyList());
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

  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile)
      throws IllegalStateException {
    ArtifactCoordinates appCoordinates = getApplicationArtifactCoordinates(pomFile);

    AppClassLoaderModel appModel = new AppClassLoaderModel(CLASS_LOADER_MODEL_VERSION, appCoordinates);

    List<BundleDependency> appDependencies = applicationDependencyResolver.resolveApplicationDependencies(pomFile);

    appModel.setDependencies(toApplicationModelArtifacts(appDependencies));


    applicationClassLoaderModel = new ApplicationClassloaderModel(appModel);

    Collection<ClassLoaderModel> pluginsClassLoaderModels = mulePluginClassLoaderModelResolver.resolve(appDependencies);
    applicationClassLoaderModel.addAllMulePluginClassloaderModels(pluginsClassLoaderModels);

    appModel.setAdditionalPluginDependencies(toPluginDependencies(additionalPluginDependenciesResolver
        .resolveDependencies(appDependencies, pluginsClassLoaderModels)));

    applicationClassLoaderModel.addAllRamlClassloaderModels(ramlClassLoaderModelResolver.resolve(appDependencies));
    applicationClassLoaderModel.addAllRamlToApplicationClassloaderModel(ramlClassLoaderModelResolver.getDependencies());

    return applicationClassLoaderModel;
  }

  public ArtifactCoordinates getApplicationArtifactCoordinates(File pomFile) {
    return ArtifactUtils.getApplicationArtifactCoordinates(pomFile);
  }

}
