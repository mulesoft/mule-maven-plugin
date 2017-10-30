/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.util;

import static java.lang.Boolean.FALSE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

public class CopyFileVisitor implements FileVisitor<Path> {

  private final File fromFolder;
  private final File targetFolder;

  private Boolean ignoreHiddenFiles;
  private Boolean ignoreHiddenFolders;

  private List<Path> exclusions = Collections.emptyList();

  public CopyFileVisitor(File fromFolder, File targetFolder) {
    this(fromFolder, targetFolder, FALSE, FALSE);
  }

  public CopyFileVisitor(File fromFolder, File targetFolder, Boolean ignoreHiddenFiles, Boolean ignoreHiddenFolders) {
    this.fromFolder = fromFolder;
    this.targetFolder = targetFolder;

    this.ignoreHiddenFiles = ignoreHiddenFiles;
    this.ignoreHiddenFolders = ignoreHiddenFolders;
  }

  public void setExclusions(List<Path> exclusions) {
    this.exclusions = exclusions;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    if (exclusions.contains(dir) || (ignoreHiddenFolders && dir.toFile().isHidden())) {
      return FileVisitResult.SKIP_SUBTREE;
    }

    Path targetPath = targetFolder.toPath().resolve(fromFolder.toPath().relativize(dir));
    if (!Files.exists(targetPath)) {
      Files.createDirectory(targetPath);
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (ignoreHiddenFiles && file.toFile().isHidden()) {
      return FileVisitResult.SKIP_SUBTREE;
    }

    Files
        .copy(file, targetFolder.toPath().resolve(fromFolder.toPath().relativize(file)), StandardCopyOption.REPLACE_EXISTING);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return FileVisitResult.CONTINUE;
  }
}
