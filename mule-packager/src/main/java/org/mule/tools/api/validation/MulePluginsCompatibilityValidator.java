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

import java.util.*;
import java.util.stream.Collectors;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

/**
 * The goal of this class is to check for if a list of Dependencies are compatible amongst them self Compatibility is defined by
 * semantic versioning
 */
public class MulePluginsCompatibilityValidator {

  /**
   * Validates a list of dependencies to check for incompatibilities
   *
   * @param mulePlugins List of mule plugins dependencies
   * @throws org.mule.tools.api.exception.ValidationException if the list of mule plugins contains incompatibilities
   */
  public void validate(List<ArtifactCoordinates> mulePlugins) throws ValidationException {
    for (Map.Entry<String, List<ArtifactCoordinates>> entry : buildDependencyMap(mulePlugins).entrySet()) {
      List<ArtifactCoordinates> dependencies = entry.getValue();
      if (dependencies.size() > 1 && !areMulePluginVersionCompatible(dependencies)) {
        throw new ValidationException(createErrorMessage(entry.getKey(), dependencies));
      }
    }
  }

  protected boolean areMulePluginVersionCompatible(List<ArtifactCoordinates> dependencies) {
    Set<String> majors = dependencies.stream()
        .map(ArtifactCoordinates::getVersion)
        .map(VersionUtils::getMajor)
        .collect(Collectors.toSet());
    return majors.size() <= 1;
  }

  protected String createErrorMessage(String mulePlugin, List<ArtifactCoordinates> dependencies) {
    StringBuilder message = new StringBuilder()
        .append("There are incompatible versions of the same mule plugin in the application dependency graph.")
        .append("This application can not be packaged as it will fail to deploy.")
        .append("Offending mule plugin: ").append(mulePlugin)
        .append("Versions: ");
    dependencies.forEach(d -> message.append(d.getVersion()).append(","));
    return message.toString();
  }

  protected Map<String, List<ArtifactCoordinates>> buildDependencyMap(List<ArtifactCoordinates> dependencyList) {
    Map<String, List<ArtifactCoordinates>> dependencyMap = new HashMap<>();

    for (ArtifactCoordinates plugin : dependencyList) {
      String key = plugin.getGroupId() + ":" + plugin.getArtifactId();

      dependencyMap.computeIfAbsent(key, k -> new ArrayList<>());
      dependencyMap.get(key).add(plugin);
    }

    return dependencyMap;
  }

}
