/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import integration.ProjectFactory;


public class MojoTest implements SettingsConfigurator {

  protected static final String TARGET_FOLDER_NAME = "target";
  protected static final String EMPTY_PROJECT_NAME = "empty-project";
  protected static final String EMPTY_DOMAIN_NAME = "empty-domain-project";
  protected static final String EMPTY_POLICY_NAME = "empty-policy-project";
  protected static final String PROJECT_BASE_DIR_PROPERTY = "project.basedir";
  protected static final String PROJECT_BUILD_DIRECTORY_PROPERTY = "project.build.directory";
  protected static final String INSTALL = "install";
  protected static final String TARGET_FOLDER_SYSTEM_PROPERTY_PLACEHOLDER = "mule.home.test";
  protected ProjectFactory builder;
  protected File projectBaseDirectory;
  protected Verifier verifier;
  protected File targetFolder;
  protected String goal;
  protected static final String[] excludes = new String[] {".placeholder", "log.txt"};

  @Before
  public void initializeContext() throws IOException, VerificationException {
    builder = new ProjectFactory();
    projectBaseDirectory = builder.createProjectBaseDir("empty-" + goal + "-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.setMavenDebug(true);
    copyNecessaryDependencies(new File(verifier.getLocalRepository()));
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

  private void copyNecessaryDependencies(File localRepository) {
    File testMockArtifactsDirectory =
        new File(System.getProperty(TARGET_FOLDER_SYSTEM_PROPERTY_PLACEHOLDER) + File.separator + "test-classes/repository");
    try {
      FileUtils.copyDirectory(testMockArtifactsDirectory, localRepository);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String getExpectedStructureRelativePath() {
    return "/expected-" + this.goal + "-structure";
  }

  protected File getExpectedStructure() throws IOException {
    return ResourceExtractor.simpleExtractResources(getClass(), getExpectedStructureRelativePath());
  }

  protected File getExpectedStructure(String expectedStructurePath) throws IOException {
    return ResourceExtractor.simpleExtractResources(getClass(), expectedStructurePath);
  }

  protected File getFile(String filePath) throws IOException {
    return ResourceExtractor.simpleExtractResources(getClass(), filePath);
  }

  protected void enableVerifierDebugMode() {
    verifier.setEnvironmentVariable("MAVEN_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,address=8002,suspend=y");
  }
}
