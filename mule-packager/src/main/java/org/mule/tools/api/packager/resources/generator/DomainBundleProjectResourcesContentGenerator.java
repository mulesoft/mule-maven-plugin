/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.resources.generator;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;
import org.mule.tools.api.packager.resources.content.DomainBundleProjectResourcesContent;
import org.mule.tools.api.packager.resources.content.ResourcesContent;

import java.util.List;
import java.util.stream.Collectors;

import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifact;

/**
 * Generates the resources of a mule domain bundle, resolving the applications and domain locations.
 */
public class DomainBundleProjectResourcesContentGenerator implements ResourcesContentGenerator {

  private final AetherMavenClient muleMavenPluginClient;
  private final List<ArtifactCoordinates> projectDependencies;

  public DomainBundleProjectResourcesContentGenerator(AetherMavenClient aetherMavenClient,
                                                      List<ArtifactCoordinates> projectDependencies) {
    this.muleMavenPluginClient = aetherMavenClient;
    this.projectDependencies = projectDependencies;
  }

  @Override
  public ResourcesContent generate() {
    ResourcesContent resourcesContent = new DomainBundleProjectResourcesContent();
    List<BundleDescriptor> dependenciesBundleDescriptors =
        projectDependencies.stream().map(ArtifactUtils::toBundleDescriptor).collect(Collectors.toList());
    for (BundleDescriptor bundleDescriptor : dependenciesBundleDescriptors) {
      BundleDependency dependency = muleMavenPluginClient.resolveBundleDescriptor(bundleDescriptor);
      resourcesContent.add(toArtifact(dependency));
    }
    return resourcesContent;
  }

}
