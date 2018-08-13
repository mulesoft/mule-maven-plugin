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

import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toApplicationModelArtifacts;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;

import java.io.File;
import java.util.List;

import org.mule.tools.api.classloader.model.resolver.ApplicationDependencyResolver;
import org.mule.tools.api.classloader.model.resolver.ClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.MulePluginClassloaderModelResolver;
import org.mule.tools.api.classloader.model.resolver.RamlClassloaderModelResolver;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;

public class ApplicationClassLoaderModelAssembler {

  public static final String CLASS_LOADER_MODEL_VERSION = "1.0.0";

  private final AetherMavenClient muleMavenPluginClient;
  private ApplicationClassloaderModel applicationClassLoaderModel;

  public ApplicationClassLoaderModelAssembler(AetherMavenClient muleMavenPluginClient) {
    this.muleMavenPluginClient = muleMavenPluginClient;
  }

  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile)
      throws IllegalStateException {
    ArtifactCoordinates appCoordinates = getApplicationArtifactCoordinates(pomFile);

    ClassLoaderModel appModel = new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, appCoordinates);

    ApplicationDependencyResolver appDependencyResolver = new ApplicationDependencyResolver(muleMavenPluginClient);
    List<BundleDependency> appDependencies = appDependencyResolver.resolveApplicationDependencies(pomFile);

    appModel.setDependencies(toApplicationModelArtifacts(appDependencies));

    applicationClassLoaderModel = new ApplicationClassloaderModel(appModel);

    ClassloaderModelResolver mulePluginClassloaderModelResolver =
        new MulePluginClassloaderModelResolver(appDependencies, muleMavenPluginClient);

    applicationClassLoaderModel.addAllMulePluginClassloaderModels(mulePluginClassloaderModelResolver.resolve());

    ClassloaderModelResolver ramlClassloaderModelResolver =
        new RamlClassloaderModelResolver(appDependencies, muleMavenPluginClient);
    applicationClassLoaderModel.addAllRamlClassloaderModels(ramlClassloaderModelResolver.resolve());
    applicationClassLoaderModel.addAllRamlToApplicationClassloaderModel(ramlClassloaderModelResolver.getDependencies());

    return applicationClassLoaderModel;
  }

  public ArtifactCoordinates getApplicationArtifactCoordinates(File pomFile) {
    return ArtifactUtils.getApplicationArtifactCoordinates(pomFile);
  }

}
