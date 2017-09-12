/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.utils;

import org.apache.maven.project.MavenProject;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.util.Project;

import java.util.List;
import java.util.stream.Collectors;

public class DependencyProject implements Project {

  private MavenProject mavenProject;

  public DependencyProject(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  @Override
  public List<ArtifactCoordinates> getDependencies() {
    return mavenProject.getDependencies().stream().map(ArtifactUtils::toArtifactCoordinates).collect(Collectors.toList());
  }
}
