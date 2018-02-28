/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.archiver;

import static java.nio.file.Paths.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.mule.tools.api.packager.structure.FolderNames.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Generic test for MuleArchiver
 * 
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
    List<Path> expectedPaths = new ArrayList<>();
    for (Path p : getArchiveRelativePaths()) {
      expectedPaths.add(destinationDirectoryForUnzip.toPath().resolve(p));
    }

    assertThatFolderContainsFiles(destinationDirectoryForUnzip, expectedPaths);
  }

  protected void assertThatFolderContainsFiles(File destinationDirectoryForUnzip, List<Path> expectedPaths) {

    List<String> allFilesInDestination = new ArrayList<>();
    Collection<File> files = FileUtils.listFiles(destinationDirectoryForUnzip, null, true);
    for (File file : files) {
      allFilesInDestination.add(file.getAbsolutePath());
    }

    Set<String> expectedFilesVerified = new HashSet<>();
    for (Path expectedPath : expectedPaths) {
      expectedFilesVerified.add(expectedPath.toString());
    }

    Set<String> foundFilesVerified = new HashSet<>(allFilesInDestination);

    foundFilesVerified.removeAll(expectedFilesVerified);
    expectedFilesVerified.removeAll(allFilesInDestination);

    assertThat("Expected files not found:", expectedFilesVerified, empty());
    assertThat("Found files not expected:", foundFilesVerified, empty());
  }

  protected List<Path> getArchiveRelativePaths() {
    List<Path> relativePaths = new ArrayList<>();

    Path tempBasePath = get("");
    relativePaths.add(tempBasePath.resolve("mule-config1.xml"));
    relativePaths.add(tempBasePath.resolve("org.mule.package").resolve("mule-config2.xml"));
    tempBasePath = get(CLASSES.value());
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class1.clazz"));
    relativePaths.add(tempBasePath.resolve("org.fake.core").resolve("class2.clazz"));
    tempBasePath = get(LIB.value());
    relativePaths.add(tempBasePath.resolve("bla.jar"));
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
