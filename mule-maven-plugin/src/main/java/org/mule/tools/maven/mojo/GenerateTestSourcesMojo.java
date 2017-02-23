/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.tools.maven.util.CopyFileVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "generate-test-sources",
    defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateTestSourcesMojo extends AbstractMuleMojo {

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Creating target content with Test Mule source code...");

    try {
      createTestMuleFolderContent();
    } catch (IOException e) {
      throw new MojoFailureException("Fail to generate sources", e);
    }
  }

  private void createTestMuleFolderContent() throws IOException {
    File targetFolder = Paths.get(project.getBuild().getDirectory(), TEST_MULE, MUNIT).toFile();
    Files.walkFileTree(munitSourceFolder.toPath(), new CopyFileVisitor(munitSourceFolder, targetFolder));
  }


}
