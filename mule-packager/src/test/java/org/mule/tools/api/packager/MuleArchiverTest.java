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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mule.tools.api.packager.PackagerFolders.CLASSES;
import static org.mule.tools.api.packager.PackagerFolders.MAVEN;
import static org.mule.tools.api.packager.PackagerFolders.META_INF;
import static org.mule.tools.api.packager.PackagerFolders.MULE;
import static org.mule.tools.api.packager.PackagerFolders.MULE_ARTIFACT;
import static org.mule.tools.api.packager.PackagerFolders.MULE_SRC;
import static org.mule.tools.api.packager.PackagerFolders.MUNIT;
import static org.mule.tools.api.packager.PackagerFolders.PLUGINS;

import org.mule.tools.api.packager.MuleArchiver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class MuleArchiverTest {

  public static final String REAL_APP_TARGET = "real-app-target";
  public static final String REAL_APP = "real-app";
  public static final String SRC = "src";
  public static final String MAIN = "main";
  public static final String RESOURCES = "resources";
  public static final String TEST = "test";
  public static final String JAVA = "java";

  @Rule
  public TemporaryFolder targetFileFolder = new TemporaryFolder();

  public MuleArchiver muleArchiver;

  @Before
  public void setUp() {
    muleArchiver = new MuleArchiver();
  }

  @Test
  public void createCompleteAppUsingFolders() throws Exception {

    File destinationFile = new File(targetFileFolder.getRoot(), REAL_APP_TARGET + ".zip");

    muleArchiver.addClasses(getTestResourceFile(REAL_APP_TARGET + File.separator + CLASSES), null, null);

    muleArchiver.addMaven(getTestResourceFile(REAL_APP_TARGET + File.separator + META_INF + File.separator + MAVEN), null,
                          null);
    muleArchiver
        .addMuleSrc(getTestResourceFile(REAL_APP_TARGET + File.separator + META_INF + File.separator + MULE_SRC), null,
                    null);
    muleArchiver
        .addMuleArtifact(getTestResourceFile(REAL_APP_TARGET + File.separator + META_INF + File.separator + MULE_ARTIFACT),
                         null,
                         null);

    muleArchiver.addMule(getTestResourceFile(REAL_APP_TARGET + File.separator + MULE), null, null);

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

    Path tempBasePath;

    tempBasePath = Paths.get(CLASSES);
    relativePaths.add(tempBasePath.resolve("resource2").toString());
    relativePaths.add(tempBasePath.resolve("resource1").toString());
    relativePaths.add(tempBasePath.resolve("class3.clazz").toString());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1.clazz").toString());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2.clazz").toString());
    relativePaths.add(tempBasePath.resolve("resourceFolder").resolve("resource3").toString());

    tempBasePath = Paths.get(CLASSES).resolve(PLUGINS);
    relativePaths.add(tempBasePath.resolve("api").resolve("api.raml").toString());
    relativePaths.add(tempBasePath.resolve("wsdl").resolve("aservice.wsdl").toString());

    tempBasePath = Paths.get(MULE);
    relativePaths.add(tempBasePath.resolve("mule-config1.xml").toString());
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2.xml").toString());

    tempBasePath = Paths.get(META_INF).resolve(MAVEN);
    relativePaths.add(tempBasePath.resolve("org.mule.fake").resolve("complete-app").resolve("pom.xml").toString());
    relativePaths.add(tempBasePath.resolve("org.mule.fake").resolve("complete-app").resolve("pom.properties").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_ARTIFACT);
    relativePaths.add(tempBasePath.resolve("mule-artifact.json").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_SRC).resolve(REAL_APP);
    relativePaths.add(tempBasePath.resolve("pom.xml").toString());
    relativePaths.add(tempBasePath.resolve("mule-app.properties").toString());
    relativePaths.add(tempBasePath.resolve("mule-deploy.properties").toString());
    relativePaths.add(tempBasePath.resolve("catalog").resolve("something.json").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_SRC).resolve(REAL_APP).resolve(SRC).resolve(MAIN).resolve(MULE);
    relativePaths.add(tempBasePath.resolve("mule-config1.xml").toString());
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2.xml").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_SRC).resolve(REAL_APP).resolve(SRC).resolve(MAIN).resolve(JAVA);
    relativePaths.add(tempBasePath.resolve("class3.clazz").toString());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1.clazz").toString());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2.clazz").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_SRC).resolve(REAL_APP).resolve(SRC).resolve(MAIN).resolve(RESOURCES);
    relativePaths.add(tempBasePath.resolve("resource1").toString());
    relativePaths.add(tempBasePath.resolve("resource2").toString());
    relativePaths.add(tempBasePath.resolve("resourceFolder").resolve("resource3").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_SRC).resolve(REAL_APP).resolve(SRC).resolve(TEST).resolve(JAVA);
    relativePaths.add(tempBasePath.resolve("class3Test.clazz").toString());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2Test.clazz").toString());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1Test.clazz").toString());

    tempBasePath = Paths.get(META_INF).resolve(MULE_SRC).resolve(REAL_APP).resolve(SRC).resolve(TEST).resolve(MUNIT);
    relativePaths.add(tempBasePath.resolve("mule-config1-test.xml").toString());
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2-test.xml").toString());


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
