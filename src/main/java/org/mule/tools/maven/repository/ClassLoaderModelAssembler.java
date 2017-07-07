/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import static java.lang.String.format;
import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;
import static org.mule.maven.client.internal.util.MavenUtils.getPomModelFromFile;
import static org.mule.tools.api.classloader.model.util.DependencyUtils.*;
import static org.mule.tools.api.classloader.model.util.DependencyUtils.toDependencies;
import static org.mule.tools.api.classloader.model.util.DependencyUtils.toDependency;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.eclipse.aether.graph.DependencyFilter;
import org.mule.maven.client.api.PomFileSupplierFactory;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.classloader.model.Dependency;


public class ClassLoaderModelAssembler {

  private static final String POM_TYPE = "pom";
  private static final String CLASS_LOADER_MODEL_VERSION = "1.0.0";
  private final AetherMavenClient muleMavenPluginClient;
  private ClassLoaderModel model;
  protected DependencyFilter mulePluginFilter = (node, parents) -> node != null && node.getArtifact() != null
      && !MULE_PLUGIN_CLASSIFIER.equals(node.getArtifact().getClassifier());
  protected DependencyFilter notMulePluginFilter = (node, parents) -> node != null && node.getArtifact() != null
      && MULE_PLUGIN_CLASSIFIER.equals(node.getArtifact().getClassifier());

  public ClassLoaderModelAssembler(AetherMavenClient muleMavenPluginClient) {
    this.muleMavenPluginClient = muleMavenPluginClient;
  }

  public ClassLoaderModel getClassLoaderModel(File pomFile, File targetFolder) {
    BundleDescriptor projectBundleDescriptor = getProjectBundleDescriptor(pomFile);

    model = new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, getArtifactCoordinates(projectBundleDescriptor));

    List<BundleDependency> nonMulePluginDependencies =
        resolveNonMulePluginDependencies(targetFolder, projectBundleDescriptor);
    model.setDependencies(getDependencies(nonMulePluginDependencies));

    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies =
        resolveMulePluginDependencies(targetFolder, projectBundleDescriptor);
    model.setMulePlugins(mulePluginDependencies.keySet().stream().collect(Collectors
        .toMap(this::getDependency, dependency -> this.getDependencies(mulePluginDependencies.get(dependency)))));

    return model;
  }

  private Dependency getDependency(BundleDependency bundleDependency) {
    return toDependency(bundleDependency);
  }

  private List<Dependency> getDependencies(List<BundleDependency> bundleDependencies) {
    return toDependencies(bundleDependencies);
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
   * Resolve mule plugins that are direct and transitive dependencies of the application and also each of the mule plugins own
   * dependencies.
   * 
   * @param targetFolder target folder of application that is going to be packaged, which need to contain at this stage the pom
   *        file in the folder that is going to be resolved by {@link PomFileSupplierFactory}
   * @param bundleDescriptor bundleDescriptor of application to be packaged
   */
  private Map<BundleDependency, List<BundleDependency>> resolveMulePluginDependencies(File targetFolder,
                                                                                      BundleDescriptor bundleDescriptor) {
    List<BundleDependency> muleDependencies =
        muleMavenPluginClient.resolveBundleDescriptorDependenciesWithWorkspaceReader(targetFolder, false, false, bundleDescriptor,
                                                                                     notMulePluginFilter,
                                                                                     notMulePluginFilter);
    Map<BundleDependency, List<BundleDependency>> muleDependenciesDependencies = new HashMap<>();
    for (BundleDependency muleDependency : muleDependencies) {
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
