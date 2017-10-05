package org.mule.tools.api.packager;
/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the basic information of a project.
 */
public class ProjectInformation {

  private String groupId;
  private String artifactId;
  private String version;
  private String packaging;
  private Path projectBaseFolder;
  private Path buildDirectory;
  private boolean isTestProject;

  private ProjectInformation(String groupId, String artifactId, String version, String packaging, Path projectBaseFolder,
                             Path buildDirectory, boolean isTestProject) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.projectBaseFolder = projectBaseFolder;
    this.buildDirectory = buildDirectory;
    this.isTestProject = isTestProject;
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

  public String getPackaging() {
    return packaging;
  }

  public Path getProjectBaseFolder() {
    return projectBaseFolder;
  }

  public Path getBuildDirectory() {
    return buildDirectory;
  }

  public boolean isTestProject() {
    return isTestProject;
  }

  public static class Builder {

    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;
    private Path projectBaseFolder;
    private Path buildDirectory;
    private Boolean isTestProject;

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

    public Builder setTestProject(Boolean isTestProject) {
      this.isTestProject = isTestProject;
      return this;
    }

    public ProjectInformation build() {
      checkArgument(StringUtils.isNotBlank(groupId), "Group id should not be null nor blank");
      checkArgument(StringUtils.isNotBlank(artifactId), "Artifact id should not be null nor blank");
      checkArgument(StringUtils.isNotBlank(version), "Version should not be null nor blank");
      checkArgument(StringUtils.isNotBlank(packaging), "Version should not be null nor blank");
      checkArgument(projectBaseFolder != null, "Project base folder should not be null");
      checkArgument(buildDirectory != null, "Project build directory should not be null");
      checkArgument(isTestProject != null, "Project isTestProject property was not set");

      return new ProjectInformation(groupId, artifactId, version, packaging, projectBaseFolder, buildDirectory, isTestProject);
    }

  }

}
