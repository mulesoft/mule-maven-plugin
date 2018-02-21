/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.nio.file.Path;
import java.util.List;

import org.mule.tools.api.packager.structure.ProjectStructure;
import org.mule.tools.model.Deployment;

/**
 * Represents the basic information of a project.
 */
public class ProjectInformation {

  private String groupId;
  private String artifactId;
  private String packaging;
  private String version;
  private final String classifier;
  private Path projectBaseFolder;
  private Path buildDirectory;
  private boolean isDeployment;
  private List<Deployment> deployments;
  private ProjectStructure projectStructure;


  private ProjectInformation(String groupId, String artifactId, String version, String classifier, String packaging,
                             Path projectBaseFolder,
                             Path buildDirectory, boolean isDeployment, List<Deployment> deployments) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
    this.packaging = packaging;
    this.projectBaseFolder = projectBaseFolder;
    this.buildDirectory = buildDirectory;
    this.isDeployment = isDeployment;
    this.deployments = deployments;
    this.projectStructure = new ProjectStructure(projectBaseFolder, buildDirectory);
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getPackaging() {
    return packaging;
  }

  public Path getProjectBaseFolder() {
    return projectBaseFolder;
  }

  public Path getBuildDirectory() {
    return buildDirectory;
  }

  public boolean isDeployment() {
    return isDeployment;
  }

  public List<Deployment> getDeployments() {
    return deployments;
  }

  public ProjectStructure getProjectStructure() {
    return projectStructure;
  }

  public static class Builder {

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    private Path projectBaseFolder;
    private Path buildDirectory;
    private boolean isDeployment;
    private String classifier;
    private List<Deployment> deployments;

    public Builder withGroupId(String groupId) {
      this.groupId = groupId;
      return this;
    }

    public Builder withArtifactId(String artifactId) {
      this.artifactId = artifactId;
      return this;
    }

    public Builder withVersion(String version) {
      this.version = version;
      return this;
    }

    public Builder withPackaging(String packaging) {
      this.packaging = packaging;
      return this;
    }

    public Builder withProjectBaseFolder(Path projectBaseFolder) {
      this.projectBaseFolder = projectBaseFolder;
      return this;
    }

    public Builder withBuildDirectory(Path buildDirectory) {
      this.buildDirectory = buildDirectory;
      return this;
    }

    public Builder isDeployment(boolean isDeployment) {
      this.isDeployment = isDeployment;
      return this;
    }

    public Builder withClassifier(String classifier) {
      this.classifier = classifier;
      return this;
    }

    public Builder withDeployments(List<Deployment> deployments) {
      this.deployments = deployments;
      return this;
    }

    public ProjectInformation build() {
      checkArgument(isNotBlank(groupId), "Group id should not be null nor blank");
      checkArgument(isNotBlank(artifactId), "Artifact id should not be null nor blank");
      checkArgument(isNotBlank(version), "Version should not be null nor blank");
      checkArgument(isNotBlank(packaging), "Packaging should not be null nor blank");
      checkArgument(projectBaseFolder != null, "Project base folder should not be null");
      checkArgument(buildDirectory != null, "Project build directory should not be null");

      return new ProjectInformation(groupId, artifactId, version, classifier, packaging, projectBaseFolder, buildDirectory,
                                    isDeployment, deployments);
    }


  }

}
