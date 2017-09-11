/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.resources.processor;

import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.packager.resources.content.ResourcesContent;
import org.mule.tools.api.packager.sources.MuleContentGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;
import static org.mule.tools.api.packager.structure.FolderNames.APPLICATIONS;
import static org.mule.tools.api.packager.structure.FolderNames.DOMAIN;

public class DomainBundleProjectResourcesContentProcessor implements ResourcesContentProcessor {

  private Path domainFolderPath;
  private Path applicationsFolderPath;

  public DomainBundleProjectResourcesContentProcessor(Path targetFolder) {
    domainFolderPath = targetFolder.resolve(DOMAIN.value());
    applicationsFolderPath = targetFolder.resolve(APPLICATIONS.value());
  }

  @Override
  public void process(ResourcesContent resourcesContent) throws IOException {
    for (Artifact artifact : resourcesContent.getResources()) {
      copyAsDomainOrApplication(artifact);
    }
  }

  protected void copyAsDomainOrApplication(Artifact artifact) throws IOException {
    Path originPath = Paths.get(artifact.getUri());
    String packagingType = artifact.getArtifactCoordinates().getClassifier();
    Path destinationPath = packagingType.equals(MULE_DOMAIN.toString()) ? domainFolderPath : applicationsFolderPath;
    MuleContentGenerator.copyFile(originPath, destinationPath, originPath.getFileName().toString());
  }
}
