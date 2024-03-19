/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileJarExplorerTest {

  @TempDir
  public File temporaryFolder;

  @Test
  public void FileJarExplorerWithoutFileShouldFail() throws URISyntaxException {
    FileJarExplorer fileJarExplorer = new FileJarExplorer();
    assertThatThrownBy(() -> fileJarExplorer.explore(temporaryFolder.toPath().resolve("subfolder").toFile().toURI()))
        .hasMessageContaining("Library file does not exists");
  }

  @Test
  public void FileJarExplorerWithFolder() throws URISyntaxException {
    FileJarExplorer fileJarExplorer = new FileJarExplorer();
    fileJarExplorer.explore(temporaryFolder.toPath().toFile().toURI());
    assertTrue(fileJarExplorer.explore(temporaryFolder.toPath().toFile().toURI()).getPackages().isEmpty());
  }

  @Test
  public void FileJarExplorerWithZipFile() throws URISyntaxException, IOException {
    FileJarExplorer fileJarExplorer = new FileJarExplorer();
    temporaryFolder.toPath().resolve("file.zip").toFile().createNewFile();
    assertTrue(fileJarExplorer.explore(temporaryFolder.toPath().resolve("file.zip").toUri()).getPackages().isEmpty());
  }

  @Test
  public void FileJarExplorerWithJarFile() throws URISyntaxException, IOException {
    FileJarExplorer fileJarExplorer = new FileJarExplorer();
    temporaryFolder.toPath().resolve("file.jar").toFile().createNewFile();
    assertThatThrownBy(() -> fileJarExplorer.explore(temporaryFolder.toPath().resolve("file.jar").toFile().toURI()))
        .hasMessageContaining("Cannot explore URL");;
  }
}
