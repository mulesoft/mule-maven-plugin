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
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler.CLASS_LOADER_MODEL_VERSION;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifact;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifactCoordinates;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifacts;

public class RamlClassloaderModelResolver extends ClassloaderModelResolver {

  private static final String RAML_CLASSIFIER = "raml";
  protected Map<BundleDependency, List<BundleDependency>> muleDependenciesDependencies;
  protected Set<BundleDescriptor> visited;

  public RamlClassloaderModelResolver(List<BundleDependency> appDependencies, AetherMavenClient muleMavenPluginClient) {
    super(appDependencies, muleMavenPluginClient, RAML_CLASSIFIER);
  }


  @Override
  protected List<BundleDependency> resolveConflicts(List<BundleDependency> newDependencies,
                                                    List<BundleDependency> alreadyResolved) {
    return newDependencies;
  }

  @Override
  public Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> dependencies) {
    visited = new HashSet<>();
    return resolveRamlDependencies(dependencies);
  }


  /**
   * Resolve each of the ramls dependencies.
   *
   * @param ramls the list of ramls that are going to have their dependencies resolved.
   */
  protected Map<BundleDependency, List<BundleDependency>> resolveRamlDependencies(List<BundleDependency> ramls) {
    checkArgument(ramls != null, "List of bundle dependencies should not be null");
    muleDependenciesDependencies = new HashMap<>();
    for (BundleDependency raml : ramls) {
      if (unvisited(raml)) {
        collectDependencies(raml);
      }
    }
    return muleDependenciesDependencies;
  }


  protected void collectDependencies(BundleDependency raml) {
    Deque<BundleDependency> queue = new ArrayDeque<>();
    queue.add(raml);
    while (!queue.isEmpty()) {
      BundleDependency ramlDependency = queue.poll();

      if (alreadyVisited(ramlDependency)) {
        continue;
      }

      List<BundleDependency> ramlDependencies = getDependencies(ramlDependency);
      muleDependenciesDependencies.put(ramlDependency, new ArrayList<>(ramlDependencies));

      for (BundleDependency dependency : ramlDependencies) {
        if (shouldVisit(dependency)) {
          queue.offer(dependency);
        }
      }

      markVisited(ramlDependency);
    }
  }

  protected boolean shouldVisit(BundleDependency dependency) {
    Optional<String> classifier = dependency.getDescriptor().getClassifier();

    return classifier.isPresent()
        && (StringUtils.equals(classifier.get(), "raml") || StringUtils.equals(classifier.get(), "raml-fragment"))
        && !muleDependenciesDependencies.containsKey(dependency);
  }

  protected void markVisited(BundleDependency ramlDependency) {
    visited.add(ramlDependency.getDescriptor());
  }

  protected List<BundleDependency> getDependencies(BundleDependency ramlDependency) {
    return muleMavenPluginClient.resolveBundleDescriptorDependencies(false, false, ramlDependency.getDescriptor());
  }

  protected boolean alreadyVisited(BundleDependency raml) {
    return visited.contains(raml.getDescriptor());
  }

  protected boolean unvisited(BundleDependency raml) {
    return !alreadyVisited(raml);
  }

}
