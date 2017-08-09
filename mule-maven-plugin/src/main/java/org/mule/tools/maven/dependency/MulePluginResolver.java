/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.utils.MavenProjectBuilder;

public class MulePluginResolver {

  private static final String MULE_PLUGIN = "mule-plugin";
  private static final String TYPE = "jar";
  private static final String COMPILE_SCOPE = "compile";
  private static final String PROVIDED_SCOPE = "provided";
  private final MavenProjectBuilder builder;

  public MulePluginResolver(MavenProjectBuilder builder) {
    this.builder = builder;
  }

  public List<Dependency> resolveMulePlugins(MavenProject project) throws MojoExecutionException {
    return getAllMulePluginDependencies(project, COMPILE_SCOPE);
  }

  protected List<Dependency> getAllMulePluginDependencies(MavenProject project, String scope) throws MojoExecutionException {
    List<Dependency> mulePluginDependencies = resolveMulePluginsOfScope(project, scope);
    List<Dependency> effectiveMulePluginDependencies = new ArrayList<>();
    effectiveMulePluginDependencies.addAll(mulePluginDependencies);
    for (Dependency dependency : mulePluginDependencies) {
      effectiveMulePluginDependencies.addAll(getAllMulePluginDependencies(builder.buildMavenProject(dependency), PROVIDED_SCOPE));
    }
    return effectiveMulePluginDependencies;
  }

  protected List<Dependency> resolveMulePluginsOfScope(MavenProject project, String scope) {
    return project.getDependencies().stream()
        .filter(dependencyWith(scope))
        .collect(Collectors.toList());
  }

  protected Predicate<Dependency> dependencyWith(String scope) {
    if (scope != null) {
      return dependency -> TYPE.equals(dependency.getType()) && scope.equals(dependency.getScope())
          && MULE_PLUGIN.equals(dependency.getClassifier());
    }
    return dependency -> false;
  }
}
