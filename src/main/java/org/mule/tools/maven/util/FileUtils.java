/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.util;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;

public class FileUtils {

  public static void markAsReadOnly(File file) throws IOException {
    boolean succeeded = file.setReadOnly();
    // We need to add this extra logic due to a bug in windows (JDK-6728842)
    if (!succeeded) {
      Path filePath = file.toPath();
      FileStore fileStore = Files.getFileStore(filePath);
      if (fileStore.supportsFileAttributeView(DosFileAttributeView.class)) {
        DosFileAttributeView fileAttributeView = Files.getFileAttributeView(filePath, DosFileAttributeView.class);
        fileAttributeView.setReadOnly(true);
      }
    }
  }

  public static void checkReadOnly(File file) throws IOException, MojoExecutionException {
    // This logic check if the file attribute was mark as read only because File#setReadOnly is platform dependent (JDK-6728842)
    Path repositoryPath = file.toPath();
    FileStore fileStore = Files.getFileStore(repositoryPath);
    if (fileStore.supportsFileAttributeView(DosFileAttributeView.class)) {
      DosFileAttributeView fileAttributeView = Files.getFileAttributeView(repositoryPath, DosFileAttributeView.class);
      if (fileAttributeView.readAttributes().isReadOnly()) {
        throw new IOException("File is not writable");
      }
    }
  }
}
