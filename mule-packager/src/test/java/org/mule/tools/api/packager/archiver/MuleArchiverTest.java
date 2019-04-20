/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.archiver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mule.tools.api.packager.structure.FolderNames.*;
import static java.nio.file.Paths.get;

import java.io.File;
import java.nio.file.Path;

import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;
import org.junit.Test;

public class MuleArchiverTest extends AbstractMuleArchiverTest {

  @Before
  public void setUp() {
    archiver = new MuleArchiver();
  }

  @Test
  public void validateArchiverType() {
    assertThat("The archiver type is not as expected", archiver.getArchiver(), instanceOf(ZipArchiver.class));
  }

  @Test
  public void createCompleteAppUsingFolders() throws Exception {
    Path appBasePath = get(REAL_APP_TARGET);

    File destinationFile = new File(targetFileFolder.getRoot(), REAL_APP_TARGET + ".zip");

    archiver.addClasses(getTestResourceFile(appBasePath.resolve(CLASSES.value())), null, null);
    archiver.addToRoot(getTestResourceFile(appBasePath.resolve(MULE.value())), null, null);
    archiver.addLib(getTestResourceFile(appBasePath.resolve(LIB.value())), null, null);
    archiver.addMappings(getTestResourceFile(appBasePath.resolve(MAPPINGS.value())), null, null);
    archiver.addMetaInf(getTestResourceFile(appBasePath.resolve(META_INF.value())), null, null);
    archiver.setDestFile(destinationFile);

    archiver.createArchive();

    assertThat("The destination file should be a file", destinationFile.isDirectory(), is(false));
    File destinationDirectoryForUnzip = uncompressArchivedApp(destinationFile);
    assertCompleteAppContent(destinationDirectoryForUnzip);
  }

  private File uncompressArchivedApp(File destinationFile) {
    final File destinationDirectoryForUnzip = getDestinationDirectoryForUnzip();
    final ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
    zipUnArchiver.setSourceFile(destinationFile);
    zipUnArchiver.setDestDirectory(destinationDirectoryForUnzip);
    zipUnArchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_DISABLED, "someName"));
    zipUnArchiver.extract();
    return destinationDirectoryForUnzip;
  }
}
