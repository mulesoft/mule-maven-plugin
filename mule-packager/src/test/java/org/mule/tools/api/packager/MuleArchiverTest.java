/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.packager.archiver.MuleArchiver;

public class MuleArchiverTest {

  private static final String REAL_APP_TARGET = "real-app-target";

  private static final String REAL_APP = "real-app";
  private static final String SRC = "src";
  private static final String MAIN = "main";
  private static final String RESOURCES = "resources";
  private static final String TEST = "test";
  private static final String JAVA = "java";

  @Rule
  public TemporaryFolder targetFileFolder = new TemporaryFolder();

  public MuleArchiver muleArchiver;

  @Before
  public void setUp() {
    muleArchiver = new MuleArchiver();
  }

  @Test
  public void createCompleteAppUsingFolders() throws Exception {
    Path appBasePath = get(REAL_APP_TARGET);
    Path appMetaInfPath = appBasePath.resolve(META_INF.value());

    File destinationFile = new File(targetFileFolder.getRoot(), REAL_APP_TARGET + ".zip");

    muleArchiver.addToRoot(getTestResourceFile(appBasePath.resolve(CLASSES.value())), null, null);
    muleArchiver.addMaven(getTestResourceFile(appMetaInfPath.resolve(MAVEN.value())), null, null);
    muleArchiver.addMuleSrc(getTestResourceFile(appMetaInfPath.resolve(MULE_SRC.value())), null, null);
    muleArchiver.addMuleArtifact(getTestResourceFile(appMetaInfPath.resolve(MULE_ARTIFACT.value())), null, null);

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
    List<Path> expectedPaths = getArchiveRelativePaths().stream()
        .map(p -> destinationDirectoryForUnzip.toPath().resolve(p))
        .collect(Collectors.toList());

    assertThatFolderContainsFiles(destinationDirectoryForUnzip, expectedPaths);
  }

  private void assertThatFolderContainsFiles(File destinationDirectoryForUnzip, List<Path> expectedPaths) {
    Collection<String> allFilesInDestination = FileUtils
        .listFiles(destinationDirectoryForUnzip, null, true).stream().map(file -> file.getAbsolutePath())
        .collect(Collectors.toList());

    Set<String> expectedFilesVerified = expectedPaths.stream().map(p -> p.toFile().toString()).collect(Collectors.toSet());
    Set<String> foundFilesVerified = new HashSet<>(allFilesInDestination);

    foundFilesVerified.removeAll(expectedFilesVerified);
    expectedFilesVerified.removeAll(allFilesInDestination);

    assertThat("Expected files not found:", expectedFilesVerified, empty());
    assertThat("Found files not expected:", foundFilesVerified, empty());
  }

  private List<Path> getArchiveRelativePaths() {
    List<Path> relativePaths = new ArrayList<>();

    Path tempBasePath = get("");
    relativePaths.add(tempBasePath.resolve("resource2"));
    relativePaths.add(tempBasePath.resolve("resource1"));
    relativePaths.add(tempBasePath.resolve("class3.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2.clazz"));
    relativePaths.add(tempBasePath.resolve("resourceFolder").resolve("resource3"));

    relativePaths.add(tempBasePath.resolve("mule-config1.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2.xml"));

    tempBasePath = get(META_INF.value()).resolve(MAVEN.value());
    relativePaths.add(tempBasePath.resolve("org.mule.fake").resolve("complete-app").resolve("pom.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.fake").resolve("complete-app").resolve("pom.properties"));

    tempBasePath = get(META_INF.value()).resolve(MULE_ARTIFACT.value());
    relativePaths.add(tempBasePath.resolve("mule-artifact.json"));

    tempBasePath = get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP);
    relativePaths.add(tempBasePath.resolve("pom.xml"));
    relativePaths.add(tempBasePath.resolve("catalog").resolve("something.json"));

    tempBasePath =
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC).resolve(MAIN).resolve(MULE.value());
    relativePaths.add(tempBasePath.resolve("mule-config1.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2.xml"));

    tempBasePath = get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC).resolve(MAIN).resolve(JAVA);
    relativePaths.add(tempBasePath.resolve("class3.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2.clazz"));

    tempBasePath =
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC).resolve(MAIN).resolve(RESOURCES);
    relativePaths.add(tempBasePath.resolve("resource1"));
    relativePaths.add(tempBasePath.resolve("resource2"));
    relativePaths.add(tempBasePath.resolve("resourceFolder").resolve("resource3"));

    tempBasePath = get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC).resolve(TEST).resolve(JAVA);
    relativePaths.add(tempBasePath.resolve("class3Test.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2Test.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1Test.clazz"));

    tempBasePath =
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC).resolve(TEST).resolve(MUNIT.value());
    relativePaths.add(tempBasePath.resolve("mule-config1-test.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2-test.xml"));
    return relativePaths;
  }

  private File getTestResourceFile(Path resource) {
    return new File(getTestResourceFolder(), resource.toString());
  }

  private File getDestinationDirectoryForUnzip() {
    File destinationFile = new File(targetFileFolder.getRoot(), "temp");
    destinationFile.mkdirs();
    return destinationFile;
  }

  private File getTestResourceFolder() {
    return new File("target" + File.separator + "test-classes");
  }
}
