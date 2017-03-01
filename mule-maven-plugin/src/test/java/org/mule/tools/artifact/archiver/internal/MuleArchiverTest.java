/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mule.tools.artifact.archiver.api.PackagerFolders.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MuleArchiverTest {

  public static final String REAL_APP = "real-app-target";

  @Rule
  public TemporaryFolder targetFileFolder = new TemporaryFolder();

  public MuleArchiver muleArchiver;

  @Before
  public void setUp() {
    muleArchiver = new MuleArchiver();

  }

  @Test
  public void createCompleteAppUsingFolders() throws Exception {

    File destinationFile = new File(targetFileFolder.getRoot(), REAL_APP + ".zip");

    muleArchiver.addClasses(getTestResourceFile(REAL_APP + File.separator + CLASSES), null, null);

    muleArchiver.addMaven(getTestResourceFile(REAL_APP + File.separator + META_INF + File.separator + MAVEN), null,
                          null);
    muleArchiver
        .addMuleSrc(getTestResourceFile(REAL_APP + File.separator + META_INF + File.separator + MULE_SRC), null,
                    null);
    muleArchiver
        .addMuleArtifact(getTestResourceFile(REAL_APP + File.separator + META_INF + File.separator + MULE_ARTIFACT),
                         null,
                         null);

    muleArchiver.addMule(getTestResourceFile(REAL_APP + File.separator + MULE), null, null);

    muleArchiver.setDestFile(destinationFile);


    muleArchiver.createArchive();
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

  private void assertCompleteAppContent(File destinationDirectoryForUnzip) {
    List<String> relativePaths = new ArrayList<>();
    relativePaths.add(CLASSES + File.separator + "resource2");
    relativePaths.add(CLASSES + File.separator + "resource1");
    relativePaths.add(CLASSES + File.separator + "class3.clazz");
    relativePaths.add(CLASSES + File.separator + "org.fake.core" + File.separator + "class1.clazz");
    relativePaths.add(CLASSES + File.separator + "org.fake.core" + File.separator + "class2.clazz");
    relativePaths.add(CLASSES + File.separator + "resourceFolder" + File.separator + "resource3");
    relativePaths.add(CLASSES + File.separator + PLUGINS + File.separator + "api" + File.separator + "api.raml");
    relativePaths
        .add(CLASSES + File.separator + PLUGINS + File.separator + "wsdl" + File.separator + "aservice.wsdl");
    relativePaths.add(MULE + File.separator + "mule-config1.xml");
    relativePaths.add(MULE + File.separator + "org.mule.package" + File.separator + "mule-config2.xml");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + "java" + File.separator + "org.fake.core" + File.separator
                          + "class2.clazz");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "test" + File.separator + "java" + File.separator + "org.fake.core" + File.separator
                          + "class2Test.clazz");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + "resources" + File.separator + "resource2");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + "resources" + File.separator + "resourceFolder" + File.separator
                          + "resource3");
    relativePaths
        .add(META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "pom.xml");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "test" + File.separator + "java" + File.separator + "org.fake.core" + File.separator
                          + "class1Test.clazz");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + "resources" + File.separator + "resource1");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator
                          + "catalog"
                          + File.separator
                          + "something.json");
    relativePaths.add(
                      "META-INF" + File.separator + "mule-src" + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "test" + File.separator + "munit" + File.separator + "org.mule.package" + File.separator
                          + "mule-config2-test.xml");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + MULE + File.separator + "mule-config1.xml");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + MULE + File.separator + "org.mule.package" + File.separator
                          + "mule-config2.xml");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "test" + File.separator + "munit" + File.separator + "mule-config1-test.xml");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator
                          + "mule-deploy.properties");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + "java" + File.separator + "org.fake.core" + File.separator
                          + "class1.clazz");
    relativePaths.add(
                      META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "main" + File.separator + "java" + File.separator + "class3.clazz");
    relativePaths.add(
                      "META-INF" + File.separator + "mule-src" + File.separator + "real-app" + File.separator + "src"
                          + File.separator
                          + "test" + File.separator + "java" + File.separator + "class3Test.clazz");
    relativePaths
        .add(META_INF + File.separator + MULE_SRC + File.separator + "real-app" + File.separator
            + "mule-app.properties");

    List<String> expectedFiles = new ArrayList<>();
    for (String relativePath : relativePaths) {
      expectedFiles.add(getDestinationDirectoryForUnzip() + File.separator + relativePath);
    }
    assertThatFolderContainsFiles(destinationDirectoryForUnzip, expectedFiles);
  }

  private File getTestResourceFile(String resource) {
    return new File(getTestResourceFolder(), resource);
  }

  private void assertThatFolderContainsFiles(File destinationDirectoryForUnzip, List<String> expectedFiles) {
    final Collection<String> allFilesInDestination = FileUtils
        .listFiles(destinationDirectoryForUnzip, null, true).stream().map(file -> file.getAbsolutePath())
        .collect(Collectors.toList());

    final Set<String> expectedFilesVerified = new HashSet<>(expectedFiles);
    final Set<String> foundFilesVerified = new HashSet<>(allFilesInDestination);

    foundFilesVerified.removeAll(expectedFilesVerified);
    expectedFilesVerified.removeAll(allFilesInDestination);

    assertThat("Expected files not found:", expectedFilesVerified, empty());
    assertThat("Found files not expected:", foundFilesVerified, empty());
  }

  private File getDestinationDirectoryForUnzip() {
    final File destinationFile = new File(targetFileFolder.getRoot(), "temp");
    destinationFile.mkdirs();
    return destinationFile;
  }

  private File getTestResourceFolder() {
    return new File("target" + File.separator + "test-classes");
  }
}
