/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package integration.test.util;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.util.ResourceExtractor;

public class ProjectFactory {

  public static File createProjectBaseDir(String projectName, Class clazz) throws IOException {
    File emptyProject = ResourceExtractor.simpleExtractResources(clazz, "/empty-project");
    File projectBaseDir = new File(emptyProject.getParentFile().getAbsolutePath(), projectName);
    if (projectBaseDir.exists()) {
      projectBaseDir.delete();
    }
    projectBaseDir.mkdir();
    return projectBaseDir;
  }

}
