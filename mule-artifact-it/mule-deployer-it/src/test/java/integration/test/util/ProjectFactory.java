/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.util;

import org.apache.maven.shared.verifier.util.ResourceExtractor;

import java.io.File;
import java.io.IOException;

public class ProjectFactory {

  public static File createProjectBaseDir(String projectName) throws IOException {
    File emptyProject = ResourceExtractor.simpleExtractResources(ProjectFactory.class, "/empty-project");
    File projectBaseDir = new File(emptyProject.getParentFile().getAbsolutePath(), projectName);

    if (projectBaseDir.exists()) {
      projectBaseDir.delete();
    }

    projectBaseDir.mkdir();
    return projectBaseDir;
  }

}
