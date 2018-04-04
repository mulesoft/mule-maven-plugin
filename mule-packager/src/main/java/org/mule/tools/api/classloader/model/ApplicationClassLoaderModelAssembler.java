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

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;
import static org.mule.maven.client.internal.util.MavenUtils.getPomModelFromFile;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifactCoordinates;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifacts;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.validation.VersionUtils.getMajor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.mule.maven.client.api.PomFileSupplierFactory;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.BundleScope;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.validation.VersionUtils;

public class ApplicationClassLoaderModelAssembler {

  private static final String POM_TYPE = "pom";
  private static final String CLASS_LOADER_MODEL_VERSION = "1.0.0";
  private static final String PACKAGE_TYPE = "jar";
  private static final String PROVIDED = "provided";
  private static final URI EMPTY_RESOURCE = URI.create("");
  private final AetherMavenClient muleMavenPluginClient;
  private ApplicationClassloaderModel applicationClassLoaderModel;

  public ApplicationClassLoaderModelAssembler(AetherMavenClient muleMavenPluginClient) {
    this.muleMavenPluginClient = muleMavenPluginClient;
  }

  public ApplicationClassloaderModel getApplicationClassLoaderModel(File pomFile, File targetFolder)
      throws IllegalStateException {
    ArtifactCoordinates appCoordinates = getApplicationArtifactCoordinates(pomFile);

    ClassLoaderModel appModel = new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, appCoordinates);

    BundleDescriptor pomBundleDescriptor = getPomProjectBundleDescriptor(pomFile);

    List<BundleDependency> appDependencies =
        resolveApplicationDependencies(targetFolder, pomBundleDescriptor);

    List<BundleDependency> mulePlugins = appDependencies.stream()
        .filter(dep -> dep.getDescriptor().getClassifier().isPresent())
        .filter(dep -> dep.getDescriptor().getClassifier().get().equals(MULE_PLUGIN_CLASSIFIER))
        .collect(Collectors.toList());

    appModel.setDependencies(toApplicationModelArtifacts(appDependencies));

    applicationClassLoaderModel = new ApplicationClassloaderModel(appModel);

    Map<BundleDependency, List<BundleDependency>> mulePluginDependencies = resolveMulePluginDependencies(mulePlugins);

    // all mule plugins classloader models are resolved here
    for (Map.Entry<BundleDependency, List<BundleDependency>> mulePluginEntry : mulePluginDependencies.entrySet()) {
      ClassLoaderModel mulePluginClassloaderModel =
          new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, toArtifactCoordinates(mulePluginEntry.getKey().getDescriptor()));
      List<BundleDependency> mulePluginDependenciesDependencies =
          resolveMulePluginsVersions(mulePluginEntry.getValue(), mulePlugins);
      mulePluginClassloaderModel.setDependencies(toArtifacts(mulePluginDependenciesDependencies));
      applicationClassLoaderModel.addMulePluginClassloaderModel(mulePluginClassloaderModel);
    }

    return applicationClassLoaderModel;
  }

  protected List<BundleDependency> resolveMulePluginsVersions(List<BundleDependency> mulePluginsToResolve,
                                                              List<BundleDependency> definitiveMulePlugins) {
    List<BundleDependency> resolvedPlugins = new ArrayList<>();
    checkArgument(mulePluginsToResolve != null, "List of mule plugins to resolve should not be null");
    checkArgument(definitiveMulePlugins != null, "List of definitive mule plugins should not be null");

    for (BundleDependency mulePluginToResolve : mulePluginsToResolve) {
      Optional<BundleDependency> mulePlugin =
          definitiveMulePlugins.stream().filter(p -> hasSameArtifactIdAndMajor(p, mulePluginToResolve)).findFirst();
      resolvedPlugins.add(mulePlugin.orElse(mulePluginToResolve));
    }
    return resolvedPlugins;
  }

  protected boolean hasSameArtifactIdAndMajor(BundleDependency bundleDependency, BundleDependency otherBundleDependency) {
    BundleDescriptor descriptor = bundleDependency.getDescriptor();
    BundleDescriptor otherDescriptor = otherBundleDependency.getDescriptor();
    return StringUtils.equals(descriptor.getArtifactId(), otherDescriptor.getArtifactId())
        && StringUtils.equals(getMajor(descriptor.getBaseVersion()), getMajor(otherDescriptor.getBaseVersion()));
  }

  private List<Artifact> toApplicationModelArtifacts(List<BundleDependency> appDependencies) {
    List<Artifact> dependencies = toArtifacts(appDependencies);
    dependencies.forEach(this::updateScopeIfDomain);
    return dependencies;
  }

  private void updateScopeIfDomain(Artifact artifact) {
    String classifier = artifact.getArtifactCoordinates().getClassifier();
    if (StringUtils.equals(classifier, MULE_DOMAIN.toString())) {
      artifact.getArtifactCoordinates().setScope(PROVIDED);
      artifact.setUri(EMPTY_RESOURCE);
    }
  }


  protected ArtifactCoordinates getApplicationArtifactCoordinates(File pomFile) {
    ArtifactCoordinates appCoordinates = toArtifactCoordinates(getPomProjectBundleDescriptor(pomFile));
    appCoordinates.setType(PACKAGE_TYPE);
    appCoordinates.setClassifier(getPomModelFromFile(pomFile).getPackaging());
    return appCoordinates;
  }

  protected BundleDescriptor getPomProjectBundleDescriptor(File pomFile) {
    Model pomModel = getPomModelFromFile(pomFile);
    return getBundleDescriptor(pomModel);
  }

  /**
   * Resolve the application dependencies, excluding mule domains.
   *
   * @param targetFolder            target folder of application that is going to be packaged, which need to contain at this stage the pom
   *                                file in the folder that is going to be resolved by {@link PomFileSupplierFactory}
   * @param projectBundleDescriptor bundleDescriptor of application to be packaged
   */
  private List<BundleDependency> resolveApplicationDependencies(File targetFolder, BundleDescriptor projectBundleDescriptor) {
    List<BundleDependency> resolvedApplicationDependencies =
        muleMavenPluginClient.resolveBundleDescriptorDependenciesWithWorkspaceReader(targetFolder, false, true,
                                                                                     projectBundleDescriptor)
            .stream()
            .filter(d -> !(d.getScope() == BundleScope.PROVIDED) || (d.getDescriptor().getClassifier().isPresent()
                && d.getDescriptor().getClassifier().get().equals("mule-domain")))
            .collect(Collectors.toList());

    return resolvedApplicationDependencies;
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
