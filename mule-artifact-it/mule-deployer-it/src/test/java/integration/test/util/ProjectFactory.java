/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
