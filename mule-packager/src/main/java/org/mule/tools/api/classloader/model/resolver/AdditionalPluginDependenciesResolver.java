/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.resolver;

import org.mule.maven.client.api.MavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.runtime.module.artifact.activation.api.deployable.DeployableProjectModel;
import org.mule.runtime.module.artifact.activation.internal.maven.MavenDeployableProjectModelBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toBundleDependency;
import static org.mule.tools.api.classloader.model.util.ArtifactUtils.toBundleDescriptor;

/**
 * Resolves additional plugin libraries for all plugins declared.
 *
 * @since 3.2.0
 */
public class AdditionalPluginDependenciesResolver {

  protected static final String MULE_EXTENSIONS_PLUGIN_GROUP_ID = "org.mule.runtime.plugins";
  protected static final String MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID = "mule-extensions-maven-plugin";
  protected static final String MULE_MAVEN_PLUGIN_GROUP_ID = "org.mule.tools.maven";
  protected static final String MULE_MAVEN_PLUGIN_ARTIFACT_ID = "mule-maven-plugin";
  protected static final String ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT = "additionalPluginDependencies";
  protected static final String ADDITIONAL_DEPENDENCIES_ELEMENT = "additionalDependencies";
  protected static final String GROUP_ID_ELEMENT = "groupId";
  protected static final String ARTIFACT_ID_ELEMENT = "artifactId";
  protected static final String VERSION_ELEMENT = "version";
  protected static final String PLUGIN_ELEMENT = "plugin";
  protected static final String DEPENDENCY_ELEMENT = "dependency";
  private MavenClient mavenClient;
  private List<Plugin> pluginsWithAdditionalDependencies;
  private File temporaryFolder;

  public AdditionalPluginDependenciesResolver(MavenClient mavenClient,
                                              List<Plugin> additionalPluginDependencies,
                                              File temporaryFolder) {
    this.mavenClient = mavenClient;
    this.pluginsWithAdditionalDependencies = new ArrayList<>(additionalPluginDependencies);
    this.temporaryFolder = temporaryFolder;
  }

  public Map<BundleDescriptor, List<BundleDependency>> resolveDependencies(File projectFolder) {
    DeployableProjectModel model =
        new MavenDeployableProjectModelBuilder(projectFolder, mavenClient.getMavenConfiguration(), false, false).build();
    return toPluginDependencies(model.getAdditionalPluginDependencies());
  }

  private Map<BundleDescriptor, List<BundleDependency>> toPluginDependencies(Map<org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor, List<org.mule.runtime.module.artifact.api.descriptor.BundleDependency>> additionalPluginDependencies) {
    return additionalPluginDependencies.entrySet().stream()
        .collect(Collectors.toMap(
                                  entry -> toBundleDescriptor(entry.getKey()),
                                  entry -> entry.getValue().stream()
                                      .map(dep -> toBundleDependency(dep))
                                      .collect(Collectors.toList())));
  }
}
