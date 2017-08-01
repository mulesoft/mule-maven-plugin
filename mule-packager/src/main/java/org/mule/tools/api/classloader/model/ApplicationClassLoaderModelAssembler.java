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

import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;
import static org.mule.maven.client.internal.util.MavenUtils.getPomModelFromFile;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.*;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifacts;

import java.io.File;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.eclipse.aether.graph.DependencyFilter;
import org.mule.maven.client.api.PomFileSupplierFactory;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;

public class ApplicationClassLoaderModelAssembler {

  private static final String POM_TYPE = "pom";
  private static final String CLASS_LOADER_MODEL_VERSION = "1.0.0";
  private final AetherMavenClient muleMavenPluginClient;
  private ApplicationClassloaderModel applicationClassLoaderModel;
  protected DependencyFilter mulePluginFilter = (node, parents) -> node != null && node.getArtifact() != null
      && !MULE_PLUGIN_CLASSIFIER.equals(node.getArtifact().getClassifier());
  protected DependencyFilter notMulePluginFilter = (node, parents) -> node != null && node.getArtifact() != null
      && MULE_PLUGIN_CLASSIFIER.equals(node.getArtifact().getClassifier());

  public ApplicationClassLoaderModelAssembler(AetherMavenClient muleMavenPluginClient) {
    this.muleMavenPluginClient = muleMavenPluginClient;
  }

  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile, File targetFolder) {
    BundleDescriptor projectBundleDescriptor = getProjectBundleDescriptor(pomFile);

    ClassLoaderModel appModel = new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, getArtifactCoordinates(projectBundleDescriptor));

    List<BundleDependency> nonMulePlugins =
        resolveNonMulePluginDependencies(targetFolder, projectBundleDescriptor);

    List<BundleDependency> mulePlugins =
        resolveMulePlugins(targetFolder, projectBundleDescriptor);

    List<BundleDependency> appDependencies = new ArrayList<>();

    appDependencies.addAll(nonMulePlugins);
    appDependencies.addAll(mulePlugins);

    appModel.setDependencies(toArtifacts(appDependencies));

    applicationClassLoaderModel = new ApplicationClassloaderModel(appModel);

    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies = resolveMulePluginDependencies(mulePlugins);

    // all mule plugins classloader models are resolved here
    for (Map.Entry<BundleDependency, List<BundleDependency>> mulePluginEntry : mulePluginDependencies.entrySet()) {
      ClassLoaderModel mulePluginClassloaderModel =
          new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, toArtifactCoordinates(mulePluginEntry.getKey().getDescriptor()));
      mulePluginClassloaderModel.setDependencies(toArtifacts(mulePluginEntry.getValue()));
      applicationClassLoaderModel.addMulePluginClassloaderModel(mulePluginClassloaderModel);
    }

    return applicationClassLoaderModel;
  }

  protected ArtifactCoordinates getArtifactCoordinates(BundleDescriptor projectBundleDescriptor) {
    return toArtifactCoordinates(projectBundleDescriptor);
  }

  protected BundleDescriptor getProjectBundleDescriptor(File pomFile) {
    Model pomModel = getPomModelFromFile(pomFile);
    return getBundleDescriptor(pomModel);
  }

  /**
   * Resolve the application dependencies, excluding mule plugins.
   *
   * @param targetFolder target folder of application that is going to be packaged, which need to contain at this stage the pom
   *        file in the folder that is going to be resolved by {@link PomFileSupplierFactory}
   * @param bundleDescriptor bundleDescriptor of application to be packaged
   */
  private List<BundleDependency> resolveNonMulePluginDependencies(File targetFolder, BundleDescriptor bundleDescriptor) {
    return muleMavenPluginClient.resolveBundleDescriptorDependenciesWithWorkspaceReader(targetFolder, false, false,
                                                                                        bundleDescriptor, mulePluginFilter,
                                                                                        mulePluginFilter);
  }

  /**
   * Resolve mule plugins that are direct and transitive dependencies of the application.
   * 
   * @param targetFolder target folder of application that is going to be packaged, which need to contain at this stage the pom
   *        file in the folder that is going to be resolved by {@link PomFileSupplierFactory}
   * @param bundleDescriptor bundleDescriptor of application to be packaged
   */
  private List<BundleDependency> resolveMulePlugins(File targetFolder,
                                                    BundleDescriptor bundleDescriptor) {
    return muleMavenPluginClient.resolveBundleDescriptorDependenciesWithWorkspaceReader(targetFolder, false, false,
                                                                                        bundleDescriptor,
                                                                                        notMulePluginFilter,
                                                                                        notMulePluginFilter);
  }

  /**
   * Resolve each of the mule plugins dependencies.
   *
   * @param mulePlugins the list of mule plugins that are going to have their dependencies resolved.
   */
  private Map<BundleDependency, List<BundleDependency>> resolveMulePluginDependencies(List<BundleDependency> mulePlugins) {
    Map<BundleDependency, List<BundleDependency>> muleDependenciesDependencies = new LinkedHashMap<>();
    for (BundleDependency muleDependency : mulePlugins) {
      List<BundleDependency> mulePluginDependencies =
          muleMavenPluginClient.resolveBundleDescriptorDependencies(false, false, muleDependency.getDescriptor());
      muleDependenciesDependencies
          .put(muleDependency, new ArrayList<>(mulePluginDependencies));
    }
    return muleDependenciesDependencies;
  }



  protected BundleDescriptor getBundleDescriptor(Model pomModel) {
    final String version =
        StringUtils.isNotBlank(pomModel.getVersion()) ? pomModel.getVersion() : pomModel.getParent().getVersion();
    return new BundleDescriptor.Builder()
        .setGroupId(StringUtils.isNotBlank(pomModel.getGroupId()) ? pomModel.getGroupId() : pomModel.getParent().getGroupId())
        .setArtifactId(pomModel.getArtifactId())
        .setVersion(version)
        .setBaseVersion(version)
        .setType(POM_TYPE)
        .build();
  }
}
