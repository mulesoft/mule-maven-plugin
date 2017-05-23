/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import integrationTests.ProjectFactory;


public class MojoTest {

  protected static final String TARGET_FOLDER_NAME = "target";
  protected static final String EMPTY_PROJECT_NAME = "empty-project";
  protected static final String EMPTY_DOMAIN_NAME = "empty-domain-project";
  protected static final String EMPTY_POLICY_NAME = "empty-policy-project";
  protected static final String PROJECT_BASE_DIR_PROPERTY = "project.basedir";
  protected static final String PROJECT_BUILD_DIRECTORY_PROPERTY = "project.build.directory";
  protected static final String INSTALL = "install";
  protected static final String DEPENDENCY_PROJECT_NAME = "dependency-repository-mirror-test";
  protected static final String DEPENDENCY_ORG_ID = "org.apache.maven.plugin.my.dependency";
  protected static final String DEPENDENCY_NAME = "dependency-repository-mirror-project";
  protected static final String DEPENDENCY_VERSION = "1.0-SNAPSHOT";
  protected static final String DEPENDENCY_TYPE = "jar";
  protected static final String DEPENDENCY_A_GROUP_ID = "group.id.a";
  protected static final String DEPENDENCY_A_ARTIFACT_ID = "artifact-id-a";
  protected static final String DEPENDENCY_A_VERSION = "1.0.0-SNAPSHOT";
  protected static final String DEPENDENCY_A_TYPE = "jar";
  protected static final String DEPENDENCY_B_GROUP_ID = "group.id.b";
  protected static final String DEPENDENCY_B_ARTIFACT_ID = "artifact-id-b";
  protected static final String DEPENDENCY_B_VERSION = "1.0.0";
  protected static final String DEPENDENCY_B_TYPE = "jar";
  protected static final String DEPENDENCY_A_PROJECT_NAME = "dependency-a";
  protected static final String DEPENDENCY_B_PROJECT_NAME = "dependency-b";
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
    verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    String mavenSettings = System.getenv("MAVEN_SETTINGS");
    if (mavenSettings != null) {
      verifier.addCliOption("-s " + mavenSettings);
    }
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
    return "/integrationTests/expected-" + this.goal + "-structure";
  }

  protected File getExpectedStructure() throws IOException {
    return ResourceExtractor.simpleExtractResources(getClass(), getExpectedStructureRelativePath());
  }

  protected File getExpectedStructure(String expectedStructurePath) throws IOException {
    return ResourceExtractor.simpleExtractResources(getClass(), expectedStructurePath);
  }

  protected void installThirdPartyArtifact(String groupId, String artifactId, String version, String type,
                                           String dependencyProjectName)
      throws IOException, VerificationException {
    File dependencyProjectRootFolder = builder.createProjectBaseDir(dependencyProjectName, this.getClass());
    Verifier auxVerifier = new Verifier(dependencyProjectRootFolder.getAbsolutePath());
    auxVerifier.deleteArtifact(groupId, artifactId, version, type);
    auxVerifier.assertArtifactNotPresent(groupId, artifactId, version, type);
    auxVerifier.addCliOption("-o");
    auxVerifier.executeGoal(INSTALL);
    auxVerifier.assertArtifactPresent(groupId, artifactId, version, type);
    auxVerifier.verifyErrorFreeLog();
  }
}
