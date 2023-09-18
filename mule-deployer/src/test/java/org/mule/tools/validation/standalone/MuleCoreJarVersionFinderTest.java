/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.standalone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MuleCoreJarVersionFinderTest {

  @TempDir
  Path temporaryFolder;
  private File visitingFile;

  private MuleCoreJarVersionFinder finder;
  private BasicFileAttributes fileAttributesMock;
  private File temporaryFolderRoot;

  @BeforeEach
  public void setUp() throws IOException {
    temporaryFolder.toFile();
    finder = new MuleCoreJarVersionFinder();
    fileAttributesMock = mock(BasicFileAttributes.class);
    temporaryFolderRoot = temporaryFolder.toAbsolutePath().toFile();
  }

  @Test
  public void visitFileFindMuleCoreJarTest() throws IOException {
    String muleVersion = "4.0.0-SNAPSHOT";
    visitingFile = temporaryFolder.resolve("mule-core-" + muleVersion + ".jar").toFile();
    finder.visitFile(visitingFile.toPath(), fileAttributesMock);
    assertThat(finder.getMuleCoreVersion()).describedAs("Version was not correctly parsed from file name").isEqualTo(muleVersion);
    assertOtherImplementedMethods(FileVisitResult.TERMINATE);
  }

  @Test
  public void visitFileFindAnyJarTest() throws IOException {
    visitingFile = temporaryFolder.resolve("any-jar.jar").toFile();
    finder.visitFile(visitingFile.toPath(), fileAttributesMock);
    assertThat(finder.getMuleCoreVersion()).describedAs("Version was not correctly parsed from file name").isNull();
    assertOtherImplementedMethods(FileVisitResult.CONTINUE);
  }


  private void assertOtherImplementedMethods(FileVisitResult terminate) throws IOException {
    assertThat(finder.preVisitDirectory(temporaryFolderRoot.toPath(), fileAttributesMock))
        .describedAs("preVisitDirectory method does not return the expected FileVisitResult").isEqualTo(terminate);
    assertThat(finder.visitFileFailed(visitingFile.toPath(), new IOException()))
        .describedAs("visitFileFailed method does not return the expected FileVisitResult").isEqualTo(terminate);
    assertThat(finder.postVisitDirectory(temporaryFolderRoot.toPath(), new IOException()))
        .describedAs("postVisitDirectory method does not return the expected FileVisitResult").isEqualTo(terminate);
  }
}
