/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class CopyFileVisitorTest {

  private static final String FROM_FOLDER = "from-folder";
  private static final String TARGET_FOLDER = "target-folder";

  public static final String NORMAL_FILE = "a-normalFile.odt";

  public static final String HIDDEN_FILE = ".hiddenFile";
  public static final String HIDDEN_FOLDER = ".hiddenFolder";

  @TempDir
  public Path temporaryFolder;

  private File fromFolder;
  private File targetFolder;

  private File normalFile;

  private File hiddenFile;
  private File hiddenFolder;

  private BasicFileAttributes basicFileAttributesMock;

  @BeforeEach
  public void setUp() throws IOException {
    basicFileAttributesMock = mock(BasicFileAttributes.class);

    fromFolder = temporaryFolder.resolve(FROM_FOLDER).toFile();
    fromFolder.mkdirs();

    targetFolder = temporaryFolder.resolve(TARGET_FOLDER).toFile();
    targetFolder.mkdirs();

    normalFile = new File(fromFolder, NORMAL_FILE);
    normalFile.createNewFile();

    hiddenFile = new File(fromFolder, HIDDEN_FILE);
    hiddenFile.createNewFile();
    // Ensure hidden fin in win based systems
    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
      Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true);
    }

    hiddenFolder = new File(fromFolder, HIDDEN_FOLDER);
    hiddenFolder.mkdir();
    // Ensure hidden fin in win based systems
    if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
      Files.setAttribute(hiddenFolder.toPath(), "dos:hidden", true);
    }
  }

  @Test
  public void visitFileFailed() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);

    FileVisitResult fileVisitResult = visitor.visitFileFailed(normalFile.toPath(), new IOException());
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void postVisitDirectory() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);

    FileVisitResult fileVisitResult = visitor.postVisitDirectory(fromFolder.toPath(), new IOException());
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void preVisitDirectoryNormalFolder() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);

    FileVisitResult fileVisitResult = visitor.preVisitDirectory(fromFolder.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void preVisitDirectoryNormalFolderExcluded() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);
    visitor.setExclusions(singletonList(fromFolder.toPath()));

    FileVisitResult fileVisitResult = visitor.preVisitDirectory(fromFolder.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(SKIP_SUBTREE);
  }

  @Test
  public void preVisitDirectoryHiddenFolder() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);

    FileVisitResult fileVisitResult = visitor.preVisitDirectory(hiddenFolder.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void preVisitDirectoryHiddenFolderExcluded() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);
    visitor.setExclusions(singletonList(hiddenFolder.toPath()));

    FileVisitResult fileVisitResult = visitor.preVisitDirectory(hiddenFolder.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(SKIP_SUBTREE);
  }

  @Test
  public void preVisitDirectoryIgnoreHiddenNormalFolder() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder, true, true);

    FileVisitResult fileVisitResult = visitor.preVisitDirectory(fromFolder.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void preVisitDirectoryIgnoreHiddenHiddenFolder() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder, true, true);
    FileVisitResult fileVisitResult = visitor.preVisitDirectory(hiddenFolder.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(SKIP_SUBTREE);
  }

  @Test
  public void visitFileNormalFile() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);

    FileVisitResult fileVisitResult = visitor.visitFile(normalFile.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void visitFileHiddenFile() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder);

    FileVisitResult fileVisitResult = visitor.visitFile(hiddenFile.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void visitFileNormalFileIgnoreHidden() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder, true, true);

    FileVisitResult fileVisitResult = visitor.visitFile(normalFile.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(CONTINUE);
  }

  @Test
  public void visitFileHiddenFileIgnoreHidden() throws IOException {
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder, true, true);

    FileVisitResult fileVisitResult = visitor.visitFile(hiddenFile.toPath(), basicFileAttributesMock);
    assertThat(fileVisitResult).isEqualTo(SKIP_SUBTREE);
  }

  @Test
  public void visitFileNotHiddenInWindowsButInMuleExcludes() throws IOException {
    List<String> muleExclusions = Arrays.asList(".classpath", ".project");
    CopyFileVisitor visitor = new CopyFileVisitor(fromFolder, targetFolder, true, true);
    for (String exclusion : muleExclusions) {
      hiddenFile = new File(fromFolder, exclusion);
      // Ensure file is not hidden in win based systems
      if (System.getProperty("os.name").toLowerCase().equals("win")) {
        Files.setAttribute(hiddenFile.toPath(), "dos:hidden", false);
      }
      FileVisitResult fileVisitResult = visitor.visitFile(hiddenFile.toPath(), basicFileAttributesMock);
      assertThat(fileVisitResult).isEqualTo(SKIP_SUBTREE);
    }
  }

}
