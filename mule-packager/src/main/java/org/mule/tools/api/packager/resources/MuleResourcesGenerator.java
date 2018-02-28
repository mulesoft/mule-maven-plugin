/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.resources;

import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.filter.DependenciesFilter;
import org.mule.tools.api.packager.packaging.Exclusion;
import org.mule.tools.api.packager.packaging.Inclusion;
import org.mule.tools.api.util.Artifact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.mule.tools.api.packager.sources.ContentGenerator.copyFile;
import static org.mule.tools.api.packager.structure.FolderNames.LIB;

public class MuleResourcesGenerator {

  private final DependenciesFilter dependenciesFilter;
  private final ProjectInformation projectInformation;

  public MuleResourcesGenerator(Set<Artifact> projectArtifacts, List<? extends Exclusion> excludes,
                                List<? extends Inclusion> includes,
                                boolean excludeMuleArtifacts, ProjectInformation projectInformation) {
    this.dependenciesFilter = new DependenciesFilter(projectArtifacts, includes, excludes, excludeMuleArtifacts);
    this.projectInformation = projectInformation;
  }

  public void generate(boolean prependGroupId) throws IOException {
    Path destinationPath = projectInformation.getBuildDirectory().resolve(LIB.value());
    for (Artifact artifact : dependenciesFilter.getArtifactsToArchive()) {
      copyArtifact(artifact, destinationPath, prependGroupId);
    }
  }

  private void copyArtifact(Artifact artifact, Path destinationPath, boolean prependGroupId) throws IOException {
    Path originPath = artifact.getFile().toPath();
    String filename = filenameInArchive(artifact, prependGroupId);
    copyFile(originPath, destinationPath, filename);
  }

  private String filenameInArchive(Artifact artifact, boolean prependGroupId) {
    StringBuilder buf = new StringBuilder();
    if (prependGroupId) {
      buf.append(artifact.getGroupId());
      buf.append(".");
    }
    buf.append(artifact.getFile().getName());
    return buf.toString();
  }
}
