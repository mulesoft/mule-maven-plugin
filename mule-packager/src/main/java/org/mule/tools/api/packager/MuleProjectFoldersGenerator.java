/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager;

import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.TEST_MULE;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.nio.file.Path;

/**
 * Generates the basic working folder structure to create a mule application package.
 */
public class MuleProjectFoldersGenerator extends AbstractProjectFoldersGenerator {

  public MuleProjectFoldersGenerator(String groupId, String artifactId, PackagingType packagingType) {
    super(groupId, artifactId, packagingType);
  }

  @Override
  public void generate(Path targetFolder) {
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), CLASSES.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), TEST_CLASSES.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), TEST_MULE.value(), MUNIT.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), META_INF.value(), MULE_SRC.value(), getArtifactId());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), META_INF.value(), MAVEN.value(), getGroupId(),
                            getArtifactId());
    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), META_INF.value(), MULE_ARTIFACT.value());

    createFolderIfNecessary(targetFolder.toAbsolutePath().toString(), REPOSITORY.value());
  }
}
