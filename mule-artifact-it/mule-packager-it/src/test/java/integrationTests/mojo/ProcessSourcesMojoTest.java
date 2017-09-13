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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static integrationTests.FileTreeMatcher.hasSameTreeStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;

public class ProcessSourcesMojoTest extends MojoTest {

  private static final String PROCESS_SOURCES = "process-sources";
  private static final String GENERATED_CLASSLOADER_MODEL_FILE =
      "/empty-classloader-model-project/target/META-INF/mule-artifact/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE =
      "/empty-classloader-model-project/target/repository/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE =
      "/empty-classloader-model-project/target/repository/org/mule/group/mule-plugin-b/1.0.0/classloader-model.json";
  private static final String EXPECTED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-a-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-b-classloader-model.json";

  public ProcessSourcesMojoTest() {
    this.goal = PROCESS_SOURCES;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testProcessSources() throws IOException, VerificationException {
    installThirdPartyArtifact(DEPENDENCY_ORG_ID, DEPENDENCY_NAME, DEPENDENCY_VERSION, DEPENDENCY_TYPE, DEPENDENCY_PROJECT_NAME);

    verifier.executeGoal(PROCESS_SOURCES);

    File expectedStructure = getExpectedStructure();

    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  // Please be aware that the order that the dependencies are installed is important:
  // For instance, dependency D MUST be installed before C as the former is a transitive dependency of the latter
  // and so it needs to be installed in the local repository in order to be resolved.
  @Test
  public void testProcessSourcesClassloaderModelGeneratedFile() throws IOException, VerificationException {
    installThirdPartyArtifact(DEPENDENCY_ORG_ID, DEPENDENCY_ARTIFACT_ID, DEPENDENCY_VERSION, DEPENDENCY_TYPE,
                              DEPENDENCY_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_A_GROUP_ID, DEPENDENCY_A_ARTIFACT_ID, DEPENDENCY_A_VERSION, DEPENDENCY_A_TYPE,
                              DEPENDENCY_A_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_B_GROUP_ID, DEPENDENCY_B_ARTIFACT_ID, DEPENDENCY_B_VERSION, DEPENDENCY_B_TYPE,
                              DEPENDENCY_B_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_D_GROUP_ID, DEPENDENCY_D_ARTIFACT_ID, DEPENDENCY_D_VERSION, DEPENDENCY_D_TYPE,
                              DEPENDENCY_D_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_C_GROUP_ID, DEPENDENCY_C_ARTIFACT_ID, DEPENDENCY_C_VERSION, DEPENDENCY_C_TYPE,
                              DEPENDENCY_C_PROJECT_NAME);
    installThirdPartyArtifact("org.mule.group", "mule-plugin-b", "1.0.0", DEPENDENCY_TYPE, "mule-plugin-b");
    installThirdPartyArtifact("org.mule.group", "mule-plugin-a", "1.0.0", DEPENDENCY_TYPE, "mule-plugin-a");
    projectBaseDirectory = builder.createProjectBaseDir("empty-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile =
        getFile(GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> generatedClassloaderModelFileContent = Files.readAllLines(generatedClassloaderModelFile.toPath());

    File expectedClassloaderModelFile =
        getFile(EXPECTED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent = Files.readAllLines(expectedClassloaderModelFile.toPath());

    assertThat("The classloader-model.json file is different from the expected", generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));

    File generatedMulePluginAClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    List<String> generatedMulePluginAClassloaderModelFileContent =
        Files.readAllLines(generatedMulePluginAClassloaderModelFile.toPath());

    File expectedMulePluginAClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    List<String> expectedMulePluginAClassloaderModelFileContent =
        Files.readAllLines(expectedMulePluginAClassloaderModelFile.toPath());

    assertThat("The classloader-model.json file of the mule-plugin-a is different from the expected",
               generatedMulePluginAClassloaderModelFileContent,
               equalTo(expectedMulePluginAClassloaderModelFileContent));


    File generatedMulePluginBClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE);
    List<String> generatedMulePluginBClassloaderModelFileContent =
        Files.readAllLines(generatedMulePluginBClassloaderModelFile.toPath());

    File expectedMulePluginBClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE);
    List<String> expectedMulePluginBClassloaderModelFileContent =
        Files.readAllLines(expectedMulePluginBClassloaderModelFile.toPath());

    assertThat("The classloader-model.json file of the mule-plugin-b is different from the expected",
               generatedMulePluginBClassloaderModelFileContent,
               equalTo(expectedMulePluginBClassloaderModelFileContent));

    File expectedStructure = getExpectedStructure("/expected-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }
}
