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
import static java.io.File.separator;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.mule.maven.client.api.MavenClientProvider;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.it.VerificationException;
import org.json.JSONException;
import org.junit.Test;

public class ProcessSourcesMojoTest extends AbstractProcessSourcesMojoTest {

  private static final String CLASSLOADER_MODEL_FILE = "classloader-model.json";
  private static final String CLASSLOADER_MODEL_LOCATION = "/target/META-INF/mule-artifact/" + CLASSLOADER_MODEL_FILE;
  private static final String EXPECTED_CLASSLOADER_MODEL_FILE_ROOT_FOLDER = "/expected-files/";

  private static final String GENERATED_CLASSLOADER_MODEL_FILE =
      "/empty-classloader-model-project/target/META-INF/mule-artifact/classloader-model.json";
  private static final String GENERATED_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/empty-lightweight-local-repository-classloader-model-project/target/META-INF/mule-artifact/classloader-model.json";
  private static final String GENERATED_PROVIDED_CLASSLOADER_MODEL_FILE =
      "/provided-mule-plugin-classloader-model-project/target/META-INF/mule-artifact/classloader-model.json";
  private static final String GENERATED_TEST_JAR_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/META-INF/mule-artifact/classloader-model.json";

  private static final String GENERATED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE =
      "/empty-classloader-model-project/target/repository/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE =
      "/empty-classloader-model-project/target/repository/org/mule/group/mule-plugin-b/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_A_TEST_JAR_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/repository/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_B_TEST_JAR_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/repository/org/mule/group/mule-plugin-b/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_D_TEST_JAR_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/repository/org/mule/group/mule-plugin-d/1.0.0/classloader-model.json";
  private static final String EXPECTED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-classloader-model.json";
  private static final String EXPECTED_TEST_JAR_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-test-jar-classloader-model.json";
  private static final String EXPECTED_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-test-jar-lightweight-local-repository-classloader-model.json";

  private static final String EXPECTED_PROVIDED_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-provided-mule-plugin-classloader-model.json";
  private static final String GENERATED_PROVIDED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE =
      "/provided-mule-plugin-classloader-model-project/target/repository/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";

  private static final String EXPECTED_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-lightweight-local-repository-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-a-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-b-classloader-model.json";

  private static final String EXPECTED_MULE_PLUGIN_D_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-d-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_D_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-d-lightweight-local-repository-classloader-model.json";

  private static final String EXPECTED_PROVIDED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-provided-mule-plugin-a-classloader-model.json";

  private static final String GENERATED_MULE_PLUGIN_A_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/META-INF/mule-artifact/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_B_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/META-INF/mule-artifact/org/mule/group/mule-plugin-b/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_D_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/test-jar-classloader-model-project/target/META-INF/mule-artifact/org/mule/group/mule-plugin-d/1.0.0/classloader-model.json";

  private static final String GENERATED_MULE_PLUGIN_A_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/empty-lightweight-local-repository-classloader-model-project/target/META-INF/mule-artifact/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_B_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/empty-lightweight-local-repository-classloader-model-project/target/META-INF/mule-artifact/org/mule/group/mule-plugin-b/1.1.0/classloader-model.json";
  private static final String GENERATED_MULE_PLUGIN_C_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/empty-lightweight-local-repository-classloader-model-project/target/META-INF/mule-artifact/org/mule/group/mule-plugin-c/1.0.0/classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_A_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-a-lightweight-local-repository-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_A_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-a-test-jar-lightweight-local-repository-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_B_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-b-lightweight-local-repository-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_B_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-b-test-jar-lightweight-local-repository-classloader-model.json";
  private static final String EXPECTED_MULE_PLUGIN_C_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE =
      "/expected-files/expected-mule-plugin-c-lightweight-local-repository-classloader-model.json";

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

  @Test
  public void testProcessSources() throws IOException, VerificationException {
    verifier.executeGoal(PROCESS_SOURCES);

    File expectedStructure = getExpectedStructure();

    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testProvidedMulePluginShouldBeExcludedFromApplicationClassLoaderModel()
      throws IOException, VerificationException, JSONException {
    projectBaseDirectory = builder.createProjectBaseDir("provided-mule-plugin-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile = getFile(GENERATED_PROVIDED_CLASSLOADER_MODEL_FILE);
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile(EXPECTED_PROVIDED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);

    File generatedMulePluginAClassloaderModelFile = getFile(GENERATED_PROVIDED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginAClassloaderModelFileContent = readFileToString(generatedMulePluginAClassloaderModelFile);

    File expectedMulePluginAClassloaderModelFile = getFile(EXPECTED_PROVIDED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginAClassloaderModelFileContent = readFileToString(expectedMulePluginAClassloaderModelFile);

    assertEquals("The classloader-model.json file of the mule-plugin-a is different from the expected",
                 generatedMulePluginAClassloaderModelFileContent, expectedMulePluginAClassloaderModelFileContent, true);

    File expectedStructure = getExpectedStructure("/expected-provided-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + separator + TARGET_FOLDER_NAME);
    deleteDirectory(new File(targetStructure, "temp"));

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testProvidedMulePluginShouldBeExcludedFromApplicationClassLoaderModelEvenWithLowerVersions()
      throws IOException, VerificationException, JSONException {
    projectBaseDirectory = builder.createProjectBaseDir("provided-plugin-dependency", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.executeGoal(PROCESS_SOURCES);

    final String expectedProvidedPluginDependencyClassloaderModelFile =
        "/expected-files/expected-provided-plugin-dependency-classloader-model.json";
    final String generatedProvidedPluginDependencyClassloaderModelFile =
        "/provided-plugin-dependency" + CLASSLOADER_MODEL_LOCATION;
    final String expectedAPluginWithBProvidedClassLoaderModelFile =
        "/expected-files/expected-plugin-a-with-b-provided-classloader-model.json";
    final String generatedAPluginWithBProvidedClassLaoderModelFile =
        "/provided-plugin-dependency/target/repository/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json";

    File generatedClassloaderModelFile = getFile(generatedProvidedPluginDependencyClassloaderModelFile);
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile(expectedProvidedPluginDependencyClassloaderModelFile);
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);

    File generatedPluginAClassLoaderModelFile = getFile(generatedAPluginWithBProvidedClassLaoderModelFile);
    String generatedPluginAClassLoaderModelFileContent = readFileToString(generatedPluginAClassLoaderModelFile);

    File expectedPluginAClassloaderModelFile = getFile(expectedAPluginWithBProvidedClassLoaderModelFile);
    String expectedPluginAClassloaderModelFileContent = readFileToString(expectedPluginAClassloaderModelFile);

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedPluginAClassLoaderModelFileContent, expectedPluginAClassloaderModelFileContent, true);

    verifier.verifyErrorFreeLog();
  }


  @Test
  public void testPrettyPrintClassLoaderModel() throws IOException, VerificationException {
    doTestPrettyPrintClassLoaderModel(true);
  }

  @Test
  public void testNoPrettyPrintClassLoaderModel() throws IOException, VerificationException {
    doTestPrettyPrintClassLoaderModel(false);
  }

  private void doTestPrettyPrintClassLoaderModel(boolean prettyPrinting) throws IOException, VerificationException {
    String projectName = "pretty-print-process-sources-app";
    projectBaseDirectory = builder.createProjectBaseDir(projectName, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DprettyPrinting=" + prettyPrinting);
    verifier.executeGoal(PROCESS_SOURCES);

    // Utility to print the same pretty printed json without pretty print
    JsonParser parser = new JsonParser();
    Gson gson = new GsonBuilder().create();

    File generatedClassloaderModelFile = getFile("/" + projectName + "/target/META-INF/mule-artifact/classloader-model.json");
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile("/" + projectName + "/expected-files/classloader-model.json");
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);

    assertThat("The classloader-model.json file is different from the expected",
               generatedClassloaderModelFileContent, equalTo(prettyPrinting ? expectedClassloaderModelFileContent
                   : gson.toJson(parser.parse(expectedClassloaderModelFileContent).getAsJsonObject())));

    File generatedMulePluginAClassloaderModelFile =
        getFile("/" + projectName + "/target/repository/org/mule/group/mule-plugin-a/1.0.0/classloader-model.json");
    String generatedMulePluginAClassloaderModelFileContent = readFileToString(generatedMulePluginAClassloaderModelFile);

    File expectedMulePluginAClassloaderModelFile =
        getFile("/" + projectName + "/expected-files/mule-plugin-a/classloader-model.json");
    String expectedMulePluginAClassloaderModelFileContent = readFileToString(expectedMulePluginAClassloaderModelFile);

    assertThat("The classloader-model.json file of the mule-plugin-a is different from the expected",
               generatedMulePluginAClassloaderModelFileContent,
               equalTo(prettyPrinting ? expectedMulePluginAClassloaderModelFileContent
                   : gson.toJson(parser.parse(expectedMulePluginAClassloaderModelFileContent).getAsJsonObject())));

    File generatedMulePluginBClassloaderModelFile =
        getFile("/" + projectName + "/target/repository/org/mule/group/mule-plugin-b/1.0.0/classloader-model.json");
    String generatedMulePluginBClassloaderModelFileContent = readFileToString(generatedMulePluginBClassloaderModelFile);

    File expectedMulePluginBClassloaderModelFile =
        getFile("/" + projectName + "/expected-files/mule-plugin-b/classloader-model.json");
    String expectedMulePluginBClassloaderModelFileContent = readFileToString(expectedMulePluginBClassloaderModelFile);

    assertThat("The classloader-model.json file of the mule-plugin-b is different from the expected",
               generatedMulePluginBClassloaderModelFileContent,
               equalTo(prettyPrinting ? expectedMulePluginBClassloaderModelFileContent
                   : gson.toJson(parser.parse(expectedMulePluginBClassloaderModelFileContent).getAsJsonObject())));

    File expectedStructure = getExpectedStructure("/expected-pretty-print-process-sources-app");
    File targetStructure = new File(verifier.getBasedir() + separator + TARGET_FOLDER_NAME);
    deleteDirectory(new File(targetStructure, "temp"));

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  // Please be aware that the order that the dependencies are installed is important:
  // For instance, dependency D MUST be installed before C as the former is a transitive dependency of the latter
  // and so it needs to be installed in the local repository in order to be resolved.
  @Test
  public void testProcessSourcesClassloaderModelGeneratedFile() throws IOException, VerificationException, JSONException {
    projectBaseDirectory = builder.createProjectBaseDir("empty-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile = getFile(GENERATED_CLASSLOADER_MODEL_FILE);
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile(EXPECTED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);

    File generatedMulePluginAClassloaderModelFile = getFile(GENERATED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginAClassloaderModelFileContent = readFileToString(generatedMulePluginAClassloaderModelFile);

    File expectedMulePluginAClassloaderModelFile = getFile(EXPECTED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginAClassloaderModelFileContent = readFileToString(expectedMulePluginAClassloaderModelFile);

    assertEquals("The classloader-model.json file of the mule-plugin-a is different from the expected",
                 generatedMulePluginAClassloaderModelFileContent, expectedMulePluginAClassloaderModelFileContent, true);

    File generatedMulePluginBClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginBClassloaderModelFileContent = readFileToString(generatedMulePluginBClassloaderModelFile);

    File expectedMulePluginBClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginBClassloaderModelFileContent = readFileToString(expectedMulePluginBClassloaderModelFile);

    assertEquals("The classloader-model.json file of the mule-plugin-b is different from the expected",
                 generatedMulePluginBClassloaderModelFileContent, expectedMulePluginBClassloaderModelFileContent, true);

    File expectedStructure = getExpectedStructure("/expected-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + separator + TARGET_FOLDER_NAME);
    deleteDirectory(new File(targetStructure, "temp"));

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testProcessSourcesClassloaderModelGeneratedTestJar() throws IOException, VerificationException, JSONException {
    projectBaseDirectory = builder.createProjectBaseDir("test-jar-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DtestJar=true");
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile = getFile(GENERATED_TEST_JAR_CLASSLOADER_MODEL_FILE);
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile(EXPECTED_TEST_JAR_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);

    File generatedMulePluginAClassloaderModelFile = getFile(GENERATED_MULE_PLUGIN_A_TEST_JAR_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginAClassloaderModelFileContent = readFileToString(generatedMulePluginAClassloaderModelFile);

    File expectedMulePluginAClassloaderModelFile = getFile(EXPECTED_MULE_PLUGIN_A_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginAClassloaderModelFileContent = readFileToString(expectedMulePluginAClassloaderModelFile);

    assertEquals("The classloader-model.json file of the mule-plugin-a is different from the expected",
                 generatedMulePluginAClassloaderModelFileContent, expectedMulePluginAClassloaderModelFileContent, true);

    File generatedMulePluginBClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_B_TEST_JAR_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginBClassloaderModelFileContent = readFileToString(generatedMulePluginBClassloaderModelFile);

    File expectedMulePluginBClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_B_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginBClassloaderModelFileContent = readFileToString(expectedMulePluginBClassloaderModelFile);

    assertEquals("The classloader-model.json file of the mule-plugin-b is different from the expected",
                 generatedMulePluginBClassloaderModelFileContent, expectedMulePluginBClassloaderModelFileContent, true);

    // mule-plugin test dependency
    File generatedMulePluginDClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_D_TEST_JAR_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginDClassloaderModelFileContent = readFileToString(generatedMulePluginDClassloaderModelFile);

    File expectedMulePluginDClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_D_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginDClassloaderModelFileContent = readFileToString(expectedMulePluginDClassloaderModelFile);

    assertEquals("The classloader-model.json file of the mule-plugin-d is different from the expected",
                 generatedMulePluginDClassloaderModelFileContent, expectedMulePluginDClassloaderModelFileContent, true);

    File expectedStructure = getExpectedStructure("/expected-test-jar-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + separator + TARGET_FOLDER_NAME);
    deleteDirectory(new File(targetStructure, "temp"));

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testProcessSourcesClassloaderModelGeneratedTestJarLightweightUseLocalRepository()
      throws IOException, VerificationException, JSONException {
    projectBaseDirectory = builder.createProjectBaseDir("test-jar-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DtestJar=true");
    verifier.addCliOption("-DlightweightPackage=true");
    verifier.addCliOption("-DuseLocalRepository=true");
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile = getFile(GENERATED_TEST_JAR_CLASSLOADER_MODEL_FILE);
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile(EXPECTED_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);
    File localRepository = MavenClientProvider.discoverProvider(this.getClass().getClassLoader()).getLocalRepositorySuppliers()
        .environmentMavenRepositorySupplier().get();
    expectedClassloaderModelFileContent =
        expectedClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);

    File generatedMulePluginAClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_A_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginAClassloaderModelFileContent = readFileToString(generatedMulePluginAClassloaderModelFile);

    File expectedMulePluginAClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_A_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginAClassloaderModelFileContent = readFileToString(expectedMulePluginAClassloaderModelFile);
    expectedMulePluginAClassloaderModelFileContent =
        expectedMulePluginAClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file of the mule-plugin-a is different from the expected",
                 generatedMulePluginAClassloaderModelFileContent, expectedMulePluginAClassloaderModelFileContent, true);

    File generatedMulePluginBClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_B_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginBClassloaderModelFileContent = readFileToString(generatedMulePluginBClassloaderModelFile);

    File expectedMulePluginBClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_B_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginBClassloaderModelFileContent = readFileToString(expectedMulePluginBClassloaderModelFile);
    expectedMulePluginBClassloaderModelFileContent =
        expectedMulePluginBClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file of the mule-plugin-b is different from the expected",
                 generatedMulePluginBClassloaderModelFileContent, expectedMulePluginBClassloaderModelFileContent, true);

    // mule-plugin test dependency
    File generatedMulePluginDClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_D_TEST_JAR_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginDClassloaderModelFileContent = readFileToString(generatedMulePluginDClassloaderModelFile);

    File expectedMulePluginDClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_D_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginDClassloaderModelFileContent = readFileToString(expectedMulePluginDClassloaderModelFile);
    expectedMulePluginDClassloaderModelFileContent =
        expectedMulePluginDClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file of the mule-plugin-d is different from the expected",
                 generatedMulePluginDClassloaderModelFileContent, expectedMulePluginDClassloaderModelFileContent, true);

    File expectedStructure = getExpectedStructure("/expected-test-jar-lightweight-local-repository-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + separator + TARGET_FOLDER_NAME);
    deleteDirectory(new File(targetStructure, "temp"));

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  // Please be aware that the order that the dependencies are installed is important:
  // For instance, dependency D MUST be installed before C as the former is a transitive dependency of the latter
  // and so it needs to be installed in the local repository in order to be resolved.
  @Test
  public void testProcessSourcesClassloaderModelLightweightUsingLocalRepository()
      throws IOException, VerificationException, JSONException {
    projectBaseDirectory =
        builder.createProjectBaseDir("empty-lightweight-local-repository-classloader-model-project", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DlightweightPackage=true");
    verifier.addCliOption("-DuseLocalRepository=true");
    verifier.executeGoal(PROCESS_SOURCES);

    File generatedClassloaderModelFile = getFile(GENERATED_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedClassloaderModelFileContent = readFileToString(generatedClassloaderModelFile);

    File expectedClassloaderModelFile = getFile(EXPECTED_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent = readFileToString(expectedClassloaderModelFile);
    File localRepository = MavenClientProvider.discoverProvider(this.getClass().getClassLoader()).getLocalRepositorySuppliers()
        .environmentMavenRepositorySupplier().get();
    expectedClassloaderModelFileContent =
        expectedClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);

    File generatedMulePluginAClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_A_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginAClassloaderModelFileContent = readFileToString(generatedMulePluginAClassloaderModelFile);

    File expectedMulePluginAClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_A_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginAClassloaderModelFileContent = readFileToString(expectedMulePluginAClassloaderModelFile);
    expectedMulePluginAClassloaderModelFileContent =
        expectedMulePluginAClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file of the mule-plugin-a is different from the expected",
                 generatedMulePluginAClassloaderModelFileContent, expectedMulePluginAClassloaderModelFileContent, true);

    File generatedMulePluginBClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_B_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginBClassloaderModelFileContent = readFileToString(generatedMulePluginBClassloaderModelFile);

    File expectedMulePluginBClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_B_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginBClassloaderModelFileContent = readFileToString(expectedMulePluginBClassloaderModelFile);
    expectedMulePluginBClassloaderModelFileContent =
        expectedMulePluginBClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file of the mule-plugin-b is different from the expected",
                 generatedMulePluginBClassloaderModelFileContent, expectedMulePluginBClassloaderModelFileContent, true);

    File generatedMulePluginCClassloaderModelFile =
        getFile(GENERATED_MULE_PLUGIN_C_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String generatedMulePluginCClassloaderModelFileContent = readFileToString(generatedMulePluginCClassloaderModelFile);

    File expectedMulePluginCClassloaderModelFile =
        getFile(EXPECTED_MULE_PLUGIN_C_LIGHTWEIGHT_LOCAL_REPOSITORY_CLASSLOADER_MODEL_FILE);
    String expectedMulePluginCClassloaderModelFileContent = readFileToString(expectedMulePluginCClassloaderModelFile);
    expectedMulePluginCClassloaderModelFileContent =
        expectedMulePluginCClassloaderModelFileContent.replace("${localRepository}", localRepository.getAbsolutePath());

    assertEquals("The classloader-model.json file of the mule-plugin-c is different from the expected",
                 generatedMulePluginCClassloaderModelFileContent, expectedMulePluginCClassloaderModelFileContent, true);

    File expectedStructure = getExpectedStructure("/expected-lightweight-local-repository-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + separator + TARGET_FOLDER_NAME);
    deleteDirectory(new File(targetStructure, "temp"));

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, ArrayUtils.add(excludes, "mule-artifact.json")));

    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testProcessSourcesCorrectCompileScopeTransitivity() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-compile");
    String generatedClassloaderModelFileContent = getFileContent(COMPILED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_COMPILED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-compile project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesChangeDependencyByUserProperty() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-dependency-by-user-property", of("-Dmule.artifact.d.version=1.0.1"));
    String generatedClassloaderModelFileContent = getFileContent(DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY);
    assertEquals("The classloader-model.json file of mule-application-dependency-by-user-property project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesDependencyDefaultProperty() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-dependency-by-user-property");
    String generatedClassloaderModelFileContent = getFileContent(DEPENDENCY_VERSION_CHANGED_BY_USER_PROPERTY);
    String expectedClassloaderModelFileContent = getFileContent(EXPECTED_DEPENDENCY_VERSION_DEFAULT);
    assertEquals("The classloader-model.json file of mule-application-dependency-by-user-property project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesActivateProfileByUserProperty() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-profile-activation-by-user-property", of("-DenablePluginA=true"));
    String generatedClassloaderModelFileContent =
        getFileContent(PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-profile-activation-by-user-property project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
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
  public void testProcessSourcesActivateProfileById() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-profile-activation-by-user-property", of("-PmulePluginAProfileId"));
    String generatedClassloaderModelFileContent =
        getFileContent(PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-profile-activation-by-user-property project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesInactivateProfileById() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-profile-activation-by-user-property",
                            of("-DenablePluginA=true", "-P !mulePluginAProfileId"));
    String generatedClassloaderModelFileContent =
        getFileContent(PROFILE_ACTIVATION_BY_USER_PROPERTY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROFILE_ACTIVATION_BY_USER_PROPERTY_INACTIVE_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-profile-activation-by-user-property project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesCorrectProvidedScopeTransitivity() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-provided");
    String generatedClassloaderModelFileContent = getFileContent(PROVIDED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_PROVIDED_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-provided project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesCorrectRuntimeScopeTransitivity() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-runtime");
    String generatedClassloaderModelFileContent = getFileContent(RUNTIME_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_RUNTIME_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-runtime project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testProcessSourcesCorrectTestScopeTransitivity() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-test");
    String generatedClassloaderModelFileContent = getFileContent(TEST_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_TEST_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of mule-application-test project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void sharedLibrariesAreCorrectlyResolved() throws IOException, VerificationException, JSONException {
    processSourcesOnProject("mule-application-shared-dependency");
    String generatedClassloaderModelFileContent = getFileContent(SHARED_PLUGIN_DEPENDENCY_GENERATED_CLASSLOADER_MODEL_FILE);
    String expectedClassloaderModelFileContent =
        getFileContent(EXPECTED_SHARED_PLUGIN_DEPENDENCY_CLASSLOADER_MODEL_FILE);
    assertEquals("The classloader-model.json file of shared project is different from the expected",
                 generatedClassloaderModelFileContent, expectedClassloaderModelFileContent, true);
  }

  @Test
  public void testMultiModuleRepositoryGeneration() throws IOException, VerificationException {
    processSourcesOnProject("multi-module-application");
    checkGeneratedRepository("app");
    checkGeneratedRepository("policy");
  }

  @Test
  public void muleAppWithApis() throws Exception {
    final String appName = "mule-application-with-apis";
    final String classLoaderModelJsonLocation = "/" + appName + "/target/META-INF/mule-artifact/classloader-model.json";
    final String expectedClassLoaderModelJson = "expected-" + appName + "-classloader-model.json";
    final String expectedClassLoaderModelJsonLocation = "/expected-files/" + expectedClassLoaderModelJson;
    processSourcesOnProject(appName);
    String generatedAppClassLoaderModelFileContent = getFileContent(classLoaderModelJsonLocation);
    String expectedAppClassLoaderModelFileContent = getFileContent(expectedClassLoaderModelJsonLocation);
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
  }

  @Test
  public void appWithSameDependencyWithDifferentClassifier() throws Exception {
    final String appName = "mule-application-with-same-dep-different-classifier";
    processSourcesOnProject(appName);
    String generatedClassloaderModelFileContent = getGeneratedClassloaderModelContent(appName);
    String expectedClassLoaderModelFileContent = getExpectedClassLoaderModelContent(appName);
    assertEquals(generatedClassloaderModelFileContent, expectedClassLoaderModelFileContent, true);
  }

  @Test
  public void appWithSameDependencyWithDifferentClassifierAsTransitive() throws Exception {
    final String appName = "mule-application-with-same-dep-different-classifier-as-transitive";
    processSourcesOnProject(appName);
    String generatedClassloaderModelFileContent = getGeneratedClassloaderModelContent(appName);
    String expectedClassLoaderModelFileContent = getExpectedClassLoaderModelContent(appName);
    assertEquals(generatedClassloaderModelFileContent, expectedClassLoaderModelFileContent, true);
  }

  @Test
  public void noDuplicatesInPluginClassLoaderModel() throws Exception {
    final String appName = "mule-application-depends-on-simple-plugin";
    final String pluginClassLoaderModelLocation =
        "/" + appName + "/target/repository/org/mule/test/simple-plugin/1.0.0/classloader-model.json";
    final String expectedPluginClassLaoderModelFile =
        EXPECTED_CLASSLOADER_MODEL_FILE_ROOT_FOLDER + "expected-simple-plugin-classloader-model.json";
    processSourcesOnProject(appName);
    String generatedClassloaderModelFileContent = getFileContent(pluginClassLoaderModelLocation);
    String expectedClassLoaderModelFileContent = getFileContent(expectedPluginClassLaoderModelFile);
    assertEquals(generatedClassloaderModelFileContent, expectedClassLoaderModelFileContent, true);
  }

  @Test
  public void libWithSameDependenciesAsPluginIsResolvedOk() throws Exception {
    final String appName = "mule-application-depends-on-simple-plugin-and-dep";
    processSourcesOnProject(appName);
    String generatedClassloaderModelFileContent = getGeneratedClassloaderModelContent(appName);
    String expectedClassLoaderModelFileContent = getExpectedClassLoaderModelContent(appName);
    assertEquals(generatedClassloaderModelFileContent, expectedClassLoaderModelFileContent, true);
  }

  private String getGeneratedClassloaderModelContent(String appName) throws Exception {
    return getFileContent("/" + appName + CLASSLOADER_MODEL_LOCATION);
  }

  private String getExpectedClassLoaderModelContent(String appName) throws Exception {
    return getFileContent(EXPECTED_CLASSLOADER_MODEL_FILE_ROOT_FOLDER + "expected-" + appName + "-" + CLASSLOADER_MODEL_FILE);
  }

}
