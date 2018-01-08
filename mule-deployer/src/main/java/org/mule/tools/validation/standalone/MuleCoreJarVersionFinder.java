/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.standalone;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A file visitor for a standalone distribution. It tries to find the mule version declared in the mule-core jar.
 */
public class MuleCoreJarVersionFinder implements FileVisitor<Path> {

  /**
   * Regex to match the mule core jar filename.
   */
  private final Pattern muleCoreJarFileNameRegex = Pattern.compile("mule-core-(\\d+\\.\\d+\\.\\d+.*)\\.jar");

  /**
   * The resolved mule core version.
   */
  private String muleCoreVersion;

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    return shouldContinueTraversing();
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    String fileName = file.getFileName().toString();
    Matcher fileNameMatcher = muleCoreJarFileNameRegex.matcher(fileName);
    if (fileNameMatcher.matches()) {
      muleCoreVersion = fileNameMatcher.group(1);
    }
    return shouldContinueTraversing();
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    return shouldContinueTraversing();
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    return shouldContinueTraversing();
  }

  /**
   * Checks whether the file visitor should continue traversing the file tree.
   * 
   * @return true if the mule core version was not yet found; false otherwise.
   */
  private FileVisitResult shouldContinueTraversing() {
    return isBlank(muleCoreVersion) ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
  }

  /**
   * Retrieves the mule core version.
   * 
   * @return The resolved mule core version.
   */
  public String getMuleCoreVersion() {
    return muleCoreVersion;
  }
}
