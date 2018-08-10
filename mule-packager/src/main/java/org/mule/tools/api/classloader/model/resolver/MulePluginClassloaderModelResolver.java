/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.resolver;

import org.apache.commons.lang3.StringUtils;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;
import static org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler.CLASS_LOADER_MODEL_VERSION;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifactCoordinates;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifacts;
import static org.mule.tools.api.validation.VersionUtils.getMajor;

public class MulePluginClassloaderModelResolver extends ClassloaderModelResolver {

  public MulePluginClassloaderModelResolver(List<BundleDependency> appDependencies, AetherMavenClient muleMavenPluginClient) {
    super(appDependencies, muleMavenPluginClient, MULE_PLUGIN_CLASSIFIER);
  }

  @Override
  protected List<BundleDependency> resolveConflicts(List<BundleDependency> newDependencies,
                                                    List<BundleDependency> alreadyResolved) {
    return resolveMulePluginsVersions(newDependencies, alreadyResolved);
  }

  /**
   * Resolve each of the mule plugins dependencies.
   *
   * @param mulePlugins the list of mule plugins that are going to have their dependencies resolved.
   */
  @Override
  public Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> mulePlugins) {
    Map<BundleDependency, List<BundleDependency>> muleDependenciesDependencies = new LinkedHashMap<>();
    for (BundleDependency muleDependency : mulePlugins) {
      List<BundleDependency> mulePluginDependencies =
          muleMavenPluginClient.resolveBundleDescriptorDependencies(false, false, muleDependency.getDescriptor());
      muleDependenciesDependencies.put(muleDependency, new ArrayList<>(mulePluginDependencies));
    }
    return muleDependenciesDependencies;
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

}
