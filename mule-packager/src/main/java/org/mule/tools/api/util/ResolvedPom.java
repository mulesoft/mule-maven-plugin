/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.util.ArtifactUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;

public class ResolvedPom implements Pom {

  private final Model pomModel;

  public ResolvedPom(Model pomModel) {
    checkArgument(pomModel != null, "Pom model should not be null");
    this.pomModel = pomModel;
  }

  @Override
  public void persist(Path pom) throws IOException {

  }

  @Override
  public String getGroupId() {
    return pomModel.getGroupId();
  }

  @Override
  public String getArtifactId() {
    return pomModel.getArtifactId();
  }

  @Override
  public String getVersion() {
    return pomModel.getVersion();
  }

  @Override
  public List<ArtifactCoordinates> getDependencies() {
    return pomModel.getDependencies().stream().map(ArtifactUtils::toArtifactCoordinates).collect(toList());
  }

  @Override
  public List<Path> getResourcesLocation() {
    return pomModel.getBuild().getResources().stream().map(Resource::getDirectory).map(Paths::get).collect(toList());
  }
}
