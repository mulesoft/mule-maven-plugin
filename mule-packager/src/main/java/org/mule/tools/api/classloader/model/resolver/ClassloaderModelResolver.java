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
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.util.FileJarExplorer;
import org.mule.tools.api.util.JarExplorer;
import org.mule.tools.api.util.JarInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler.CLASS_LOADER_MODEL_VERSION;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifactCoordinates;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toArtifacts;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.updatePackagesResources;

public abstract class ClassloaderModelResolver {

  protected final AetherMavenClient muleMavenPluginClient;
  private final String classifier;
  protected Map<BundleDependency, List<BundleDependency>> dependenciesMap;

  private JarExplorer jarExplorer = new FileJarExplorer();

  public ClassloaderModelResolver(AetherMavenClient muleMavenPluginClient,
                                  String classifier) {
    this.muleMavenPluginClient = muleMavenPluginClient;
    this.classifier = classifier;
    dependenciesMap = new HashMap<>();
  }

  public final Collection<ClassLoaderModel> resolve(List<BundleDependency> appDependencies) {
    List<ClassLoaderModel> classloaderModels = new ArrayList<>();

    List<BundleDependency> dependencies = appDependencies.stream()
        .filter(dep -> dep.getDescriptor().getClassifier().isPresent())
        .filter(dep -> dep.getDescriptor().getClassifier().get().equals(classifier))
        .collect(Collectors.toList());

    dependenciesMap = resolveDependencies(dependencies);

    // all classloader models are resolved here
    for (Map.Entry<BundleDependency, List<BundleDependency>> dependencyListEntry : dependenciesMap.entrySet()) {
      ClassLoaderModel dependencyClassloaderModel =
          new ClassLoaderModel(CLASS_LOADER_MODEL_VERSION, toArtifactCoordinates(dependencyListEntry.getKey().getDescriptor()));

      JarInfo jarInfo = jarExplorer.explore(dependencyListEntry.getKey().getBundleUri());
      dependencyClassloaderModel.setPackages(jarInfo.getPackages().toArray(new String[jarInfo.getPackages().size()]));
      dependencyClassloaderModel.setResources(jarInfo.getResources().toArray(new String[jarInfo.getResources().size()]));

      List<BundleDependency> dependencyDependencies =
          resolveConflicts(dependencyListEntry.getValue(), dependencies);
      dependencyClassloaderModel.setDependencies(updatePackagesResources(toArtifacts(dependencyDependencies)));
      classloaderModels.add(dependencyClassloaderModel);
    }
    return classloaderModels;
  }

  protected abstract List<BundleDependency> resolveConflicts(List<BundleDependency> newDependencies,
                                                             List<BundleDependency> alreadyResolved);

  public abstract Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> mulePlugins);

  public List<Artifact> getDependencies() {
    return toArtifacts(dependenciesMap.keySet());
  }

}
