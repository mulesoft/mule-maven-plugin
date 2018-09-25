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

import static com.google.common.collect.ImmutableList.of;
import static integration.FileTreeMatcher.hasSameTreeStructure;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Ignore;
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

  private static final String GROUP_ID = "org.apache.maven.plugin.my.unit";
  private static final String COMPILED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/mule-application-compile/target/META-INF/mule-artifact/classloader-model.json";

  private static final String DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY =
      "/mule-application-dependency-by-user-property/target/META-INF/mule-artifact/classloader-model.json";

  private static final String PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/mule-application-profile-activation-by-user-property/target/META-INF/mule-artifact/classloader-model.json";

  private static final String PROVIDED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/mule-application-provided/target/META-INF/mule-artifact/classloader-model.json";
  private static final String RUNTIME_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/mule-application-runtime/target/META-INF/mule-artifact/classloader-model.json";
  private static final String TEST_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/mule-application-test/target/META-INF/mule-artifact/classloader-model.json";
  private static final String SHARED_PLUGIN_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/mule-application-shared-dependency/target/META-INF/mule-artifact/classloader-model.json";
  private static final String EXPECTED_SHARED_PLUGIN_DEPENDENCY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-shared-plugin-dependency-classloader-model.json";
  private static final String EXPECTED_COMPILED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-compile-scope-classloader-model.json";
  private static final String EXPECTED_DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY =
      "/expected-files/expected-mule-application-dependency-by-user-property-classloader-model.json";
  private static final String EXPECTED_DEPENDENCY_VERSION_DEFAULT =
      "/expected-files/expected-mule-application-dependency-default-classloader-model.json";
  private static final String EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-profile-activation-by-user-property-classloader-model.json";
  private static final String EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_INACTIVE_GENERATED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-profile-activation-by-user-property-inactive-classloader-model.json";
  private static final String EXPECTED_PROVIDED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-provided-scope-classloader-model.json";
  private static final String EXPECTED_RUNTIME_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-runtime-scope-classloader-model.json";
  private static final String EXPECTED_TEST_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-test-scope-classloader-model.json";

  public ProcessSourcesMojoTest() {
    this.goal = PROCESS_SOURCES;
  }

  @Before
  public void before() throws IOException, VerificationException {
    clearResources();
  }


  @Test
  public void testProcessSources() throws IOException, VerificationException {
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
    projectBaseDirectory = builder.createProjectBaseDir("empty-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile = getFile(GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> generatedClassloaderModelFileContent = Files.readAllLines(generatedClassloaderModelFile.toPath());

    File expectedClassloaderModelFile = getFile(EXPECTED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent = Files.readAllLines(expectedClassloaderModelFile.toPath());

    assertThat("The classloader-model.json file is different from the expected", generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));

    File generatedMulePluginAClassloaderModelFile = getFile(GENERATED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    List<String> generatedMulePluginAClassloaderModelFileContent =
        Files.readAllLines(generatedMulePluginAClassloaderModelFile.toPath());

    File expectedMulePluginAClassloaderModelFile = getFile(EXPECTED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
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

  @Test
  public void testProcessSourcesCorrectCompileScopeTransitivity() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-compile");
    List<String> generatedClassloaderModelFileContent = getFileContent(COMPILED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_COMPILED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-compile project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesChangeDependencyByUserProperty() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-dependency-by-user-property", of("-Dmule.artifact.d.version=1.0.1"));
    List<String> generatedClassloaderModelFileContent = getFileContent(DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY);
    assertThat("The classloader-model.json file of mule-application-dependency-by-user-property project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesDependencyDefaultProperty() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-dependency-by-user-property");
    List<String> generatedClassloaderModelFileContent = getFileContent(DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_DEPENDENCY_VERSION_DEFAULT);
    assertThat("The classloader-model.json file of mule-application-dependency-by-user-property project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesActivateProfileByUserProperty() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-profile-activation-by-user-property", of("-DenablePluginA=true"));
    List<String> generatedClassloaderModelFileContent =
        getFileContent(PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-profile-activation-by-user-property project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesActivateProfileByFileNotSupported() throws IOException, VerificationException {
    try {
      processSourcesOnProject("mule-application-profile-activation-by-file");
    } catch (VerificationException e) {
      verifier
          .verifyTextInLog("java.lang.UnsupportedOperationException: Error while resolving dependencies for org.apache.maven.plugin.my.unit:mule-application-profile-activation-by-file:mule-application:1.0.0-SNAPSHOT due to profiles activation by file are not supported");
    }
  }

  @Test
  public void testProcessSourcesActivateProfileById() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-profile-activation-by-user-property", of("-PmulePluginAProfileId"));
    List<String> generatedClassloaderModelFileContent =
        getFileContent(PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-profile-activation-by-user-property project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesInactivateProfileById() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-profile-activation-by-user-property",
                            of("-DenablePluginA=true", "-P !mulePluginAProfileId"));
    List<String> generatedClassloaderModelFileContent =
        getFileContent(PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_INACTIVE_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-profile-activation-by-user-property project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesCorrectProvidedScopeTransitivity() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-provided");
    List<String> generatedClassloaderModelFileContent = getFileContent(PROVIDED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROVIDED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-provided project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }


  @Ignore // Reenable test - MMP-292
  @Test
  public void testProcessSourcesCorrectRuntimeScopeTransitivity() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-runtime");
    List<String> generatedClassloaderModelFileContent = getFileContent(RUNTIME_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_RUNTIME_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-runtime project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testProcessSourcesCorrectTestScopeTransitivity() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-test");
    List<String> generatedClassloaderModelFileContent = getFileContent(TEST_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_TEST_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of mule-application-test project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void sharedLibrariesAreCorrectlyResolved() throws IOException, VerificationException {
    processSourcesOnProject("mule-application-shared-dependency");
    List<String> generatedClassloaderModelFileContent = getFileContent(SHARED_PLUGIN_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    List<String> expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_SHARED_PLUGIN_DEPENDENCY_CLASSLOADER_MODEL_FILE);
    assertThat("The classloader-model.json file of shared project is different from the expected",
               generatedClassloaderModelFileContent,
               equalTo(expectedClassloaderModelFileContent));
  }

  @Test
  public void testMultiModuleRepositoryGeneration() throws IOException, VerificationException {
    processSourcesOnProject("multi-module-application");
    checkGeneratedRepository("app");
    checkGeneratedRepository("policy");
  }

  private void checkGeneratedRepository(String type) throws IOException {
    File emptySubmoduleRepository = getFile("/multi-module-application/empty-" + type + "/target/repository");
    File expectedSubmoduleRepository = getExpectedStructure("/expected-empty-" + type + "-multimodule-repository");
    assertThat("Repository has not the expected structure", emptySubmoduleRepository,
               hasSameTreeStructure(expectedSubmoduleRepository, new String[] {}));
  }

  protected void processSourcesOnProject(String applicationName) throws IOException, VerificationException {
    processSourcesOnProject(applicationName, emptyList());
  }

  private void processSourcesOnProject(String applicationName, List<String> cliOptions)
      throws IOException, VerificationException {
    projectBaseDirectory = builder.createProjectBaseDir(applicationName, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DskipValidation=true");
    cliOptions.stream().forEach(option -> verifier.addCliOption(option));
    verifier.executeGoal(PROCESS_SOURCES);
  }

  protected List<String> getFileContent(String path) throws IOException {
    File generatedClassloaderModelFile = getFile(path);
    return Files.readAllLines(generatedClassloaderModelFile.toPath());
  }
}
