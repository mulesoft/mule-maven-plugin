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

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleScope;
import org.mule.maven.client.internal.AetherMavenClient;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.empty;

public class ApplicationDependencyResolver {

  private final AetherMavenClient muleMavenPluginClient;

  public ApplicationDependencyResolver(AetherMavenClient muleMavenPluginClient) {
    this.muleMavenPluginClient = muleMavenPluginClient;
  }

  /**
   * Resolve the application dependencies, excluding mule domains.
   *
   * @param pomFile pom file
   */
  public List<BundleDependency> resolveApplicationDependencies(File pomFile) {
    List<BundleDependency> resolvedApplicationDependencies =
        muleMavenPluginClient.resolveArtifactDependencies(pomFile, false, true, empty(), empty(), empty())
            .stream()
            .filter(d -> !(d.getScope() == BundleScope.PROVIDED) || (d.getDescriptor().getClassifier().isPresent()
                && d.getDescriptor().getClassifier().get().equals("mule-domain")))
            .collect(Collectors.toList());

    return resolvedApplicationDependencies;
  }

}
