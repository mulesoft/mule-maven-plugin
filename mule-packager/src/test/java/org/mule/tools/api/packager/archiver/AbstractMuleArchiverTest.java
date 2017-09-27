/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.archiver;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mule.tools.api.packager.structure.FolderNames.JAVA;
import static org.mule.tools.api.packager.structure.FolderNames.MAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.RESOURCES;
import static org.mule.tools.api.packager.structure.FolderNames.SRC;
import static org.mule.tools.api.packager.structure.FolderNames.TEST;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Generic test for MuleArchiver
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public abstract class AbstractMuleArchiverTest {

  protected static final String REAL_APP = "real-app";
  protected static final String REAL_APP_TARGET = REAL_APP + "-target";

  @Rule
  public TemporaryFolder targetFileFolder = new TemporaryFolder();

  protected MuleArchiver archiver;


  protected void assertCompleteAppContent(File destinationDirectoryForUnzip) {
    List<Path> expectedPaths = getArchiveRelativePaths().stream()
        .map(p -> destinationDirectoryForUnzip.toPath().resolve(p))
        .collect(Collectors.toList());

    assertThatFolderContainsFiles(destinationDirectoryForUnzip, expectedPaths);
  }

  protected void assertThatFolderContainsFiles(File destinationDirectoryForUnzip, List<Path> expectedPaths) {
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

  protected List<Path> getArchiveRelativePaths() {
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
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC.value()).resolve(MAIN.value())
            .resolve(MULE.value());
    relativePaths.add(tempBasePath.resolve("mule-config1.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2.xml"));

    tempBasePath = get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC.value()).resolve(MAIN.value())
        .resolve(JAVA.value());
    relativePaths.add(tempBasePath.resolve("class3.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2.clazz"));

    tempBasePath =
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC.value()).resolve(MAIN.value())
            .resolve(RESOURCES.value());
    relativePaths.add(tempBasePath.resolve("resource1"));
    relativePaths.add(tempBasePath.resolve("resource2"));
    relativePaths.add(tempBasePath.resolve("resourceFolder").resolve("resource3"));

    tempBasePath =
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC.value()).resolve(TEST.value())
            .resolve(JAVA.value());
    relativePaths.add(tempBasePath.resolve("class3Test.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2Test.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1Test.clazz"));

    tempBasePath =
        get(META_INF.value()).resolve(MULE_SRC.value()).resolve(REAL_APP).resolve(SRC.value()).resolve(TEST.value())
            .resolve(MUNIT.value());
    relativePaths.add(tempBasePath.resolve("mule-config1-test.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2-test.xml"));
    return relativePaths;
  }

  protected File getTestResourceFile(Path resource) {
    return new File(getTestResourceFolder(), resource.toString());
  }

  protected File getDestinationDirectoryForUnzip() {
    File destinationFile = new File(targetFileFolder.getRoot(), "temp");
    destinationFile.mkdirs();
    return destinationFile;
  }

  protected File getTestResourceFolder() {
    return new File("target" + File.separator + "test-classes");
  }

}
