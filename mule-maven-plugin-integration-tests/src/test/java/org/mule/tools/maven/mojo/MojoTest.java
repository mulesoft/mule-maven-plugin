/**
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.mule.tools.maven.ProjectFactory;

import java.io.File;
import java.io.IOException;

public class MojoTest {

  protected static final String TARGET_FOLDER_NAME = "target";
  protected static final String MULE_APP_JSON = "mule-app.json";
  protected static final String EMPTY_PROJECT_NAME = "empty-project";
  protected static final String MULE_CONFIG_XML = "mule-config.xml";
  protected static final String MULE_APP_PROPERTIES = "mule-app.properties";
  protected static final String PROJECT_BASE_DIR_PROPERTY = "project.basedir";
  protected static final String MULE_DEPLOY_PROPERTIES = "mule-deploy.properties";
  protected static final String PROJECT_BUILD_DIRECTORY_PROPERTY = "project.build.directory";

  protected ProjectFactory builder;
  protected File projectBaseDirectory;
  protected Verifier verifier;
  protected File targetFolder;
  protected String goal;

  @Before
  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-" + goal + "-project", this.getClass());
    verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
  }

  protected void clearResources() throws IOException {
    targetFolder = new File(projectBaseDirectory.getAbsolutePath(), TARGET_FOLDER_NAME);
    if (targetFolder.exists()) {
      FileUtils.deleteDirectory(targetFolder);
    }
  }

  @After
  public void after() {
    verifier.resetStreams();
  }


  private String getExpectedStructureRelativePath() {
    return "/expected-" + this.goal + "-structure";
  }

  protected File getExpectedStructure() throws IOException {
    return ResourceExtractor.simpleExtractResources(getClass(), getExpectedStructureRelativePath());
  }
}
