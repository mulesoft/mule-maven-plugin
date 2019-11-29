/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.BundleScope;
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

  @Override
  public List<BundleDependency> getBundleDependencies() {
    return mavenProject.getArtifacts().stream().map(this::toBundleDependency).collect(Collectors.toList());
  }

  private BundleDependency toBundleDependency(Artifact artifact) {
    BundleDescriptor.Builder descriptorBuilder = new BundleDescriptor.Builder();
    BundleDescriptor descriptor = descriptorBuilder.setArtifactId(artifact.getArtifactId())
        .setGroupId(artifact.getGroupId())
        .setVersion(artifact.getVersion())
        .setBaseVersion(artifact.getBaseVersion())
        .setClassifier(artifact.getClassifier())
        .setType(artifact.getType()).build();
    BundleDependency.Builder dependencyBuilder = new BundleDependency.Builder();
    return dependencyBuilder.setBundleUri(artifact.getFile().toURI())
        .setDescriptor(descriptor)
        .setScope(BundleScope.valueOf(artifact.getScope().toUpperCase()))
        .build();
  }
}
