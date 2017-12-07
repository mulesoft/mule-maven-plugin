/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ProjectBuildingException;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;

public class MulePluginResolver {

  private static final String MULE_PLUGIN = "mule-plugin";
  private static final String TYPE = "jar";
  private static final String COMPILE_SCOPE = "compile";
  private static final String PROVIDED_SCOPE = "provided";
  private final ProjectBuilder builder;

  public MulePluginResolver(ProjectBuilder builder) {
    this.builder = builder;
  }

  public List<ArtifactCoordinates> resolveMulePlugins(Project project) throws ValidationException {
    return getAllMulePluginDependencies(project, COMPILE_SCOPE);
  }

  protected List<ArtifactCoordinates> getAllMulePluginDependencies(Project project, String scope) throws ValidationException {
    List<ArtifactCoordinates> mulePluginDependencies = resolveMulePluginsOfScope(project, scope);
    List<ArtifactCoordinates> effectiveMulePluginDependencies = new ArrayList<>();
    effectiveMulePluginDependencies.addAll(mulePluginDependencies);
    for (ArtifactCoordinates dependency : mulePluginDependencies) {
      try {
        Project dependencyProject = builder.buildProject(dependency);
        effectiveMulePluginDependencies.addAll(getAllMulePluginDependencies(dependencyProject, PROVIDED_SCOPE));
      } catch (ProjectBuildingException e) {
        throw new ValidationException(e);
      }
    }
    return effectiveMulePluginDependencies;
  }

  // TODO we should handle dependencies of mule-domains
  protected List<ArtifactCoordinates> resolveMulePluginsOfScope(Project project, String scope) {
    return project.getDependencies().stream()
        .filter(dependencyWith(scope))
        .collect(Collectors.toList());
  }

  // TODO rename to mulePluginDependenciesOfScope (dependes if we handle domains)
  // TODO this should be private
  protected Predicate<ArtifactCoordinates> dependencyWith(String scope) {
    if (scope != null) {
      return dependency -> TYPE.equals(dependency.getType()) && scope.equals(dependency.getScope())
          && MULE_PLUGIN.equals(dependency.getClassifier());
    }
    return dependency -> false;
  }
}
