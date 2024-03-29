/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.archiver;

import static java.nio.file.Paths.get;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;

import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

public class MuleArchiverTest extends AbstractMuleArchiverTest {

  @BeforeEach
  public void setUp() {
    archiver = new MuleArchiver();
  }

  @Test
  public void validateArchiverType() {
    assertThat(archiver.getArchiver()).describedAs("The archiver type is not as expected").isInstanceOf(ZipArchiver.class);
  }

  @Test
  public void createCompleteAppUsingFolders() throws Exception {
    Path appBasePath = get(REAL_APP_TARGET);
    Path appMetaInfPath = appBasePath.resolve(META_INF.value());

    File destinationFile = new File(targetFileFolder.toAbsolutePath().toFile(), REAL_APP_TARGET + ".zip");

    archiver.addToRoot(getTestResourceFile(appBasePath.resolve(CLASSES.value())), null, null);
    archiver.addMaven(getTestResourceFile(appMetaInfPath.resolve(MAVEN.value())), null, null);
    archiver.addMuleSrc(getTestResourceFile(appMetaInfPath.resolve(MULE_SRC.value())), null, null);
    archiver.addMuleArtifact(getTestResourceFile(appMetaInfPath.resolve(MULE_ARTIFACT.value())), null, null);

    archiver.setDestFile(destinationFile);

    archiver.createArchive();

    assertThat(destinationFile.isDirectory()).describedAs("The destination file should be a file").isFalse();
    File destinationDirectoryForUnzip = uncompressArchivedApp(destinationFile);
    assertCompleteAppContent(destinationDirectoryForUnzip);
  }

  private File uncompressArchivedApp(File destinationFile) {
    final File destinationDirectoryForUnzip = getDestinationDirectoryForUnzip();
    final ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
    zipUnArchiver.setSourceFile(destinationFile);
    zipUnArchiver.setDestDirectory(destinationDirectoryForUnzip);
    zipUnArchiver.extract();
    return destinationDirectoryForUnzip;
  }
}
