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
  private static final String GENERATED_HTTP_PLUGIN_CLASSLOADER_MODEL_FILE =
          "/empty-classloader-model-project/target/repository/org/mule/connectors/mule-http-connector/1.0.0-SNAPSHOT/classloader-model.json";
  private static final String GENERATED_SOCKETS_PLUGIN_CLASSLOADER_MODEL_FILE =
          "/empty-classloader-model-project/target/repository/org/mule/connectors/mule-sockets-connector/1.0.0-SNAPSHOT/classloader-model.json";
  private static final String EXPECTED_CLASSLOADER_MODEL_FILE =
          "/expected-files/expected-classloader-model.json";
  private static final String EXPECTED_HTTP_PLUGIN_CLASSLOADER_MODEL_FILE =
          "/expected-files/expected-mule-http-connector-classloader-model.json";
  private static final String EXPECTED_SOCKETS_PLUGIN_CLASSLOADER_MODEL_FILE =
          "/expected-files/expected-mule-sockets-connector-classloader-model.json";

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

  @Test
  public void testProcessSourcesClassloaderModelGeneratedFile() throws IOException, VerificationException {
    // THIS TEST NEEDS TO BE REVIEWED. MULE PLUGINS DECLARATION ORDER SOMETIMES CHANGES IN THE GENERATED CLASSLOADER MODEL
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

    File generatedHttpPluginClassloaderModelFile =
            getFile(GENERATED_HTTP_PLUGIN_CLASSLOADER_MODEL_FILE);
    List<String> generatedHttpPluginClassloaderModelFileContent = Files.readAllLines(generatedHttpPluginClassloaderModelFile.toPath());

    File expectedHttpPluginClassloaderModelFile =
            getFile(EXPECTED_HTTP_PLUGIN_CLASSLOADER_MODEL_FILE);
    List<String> expectedHttpPluginClassloaderModelFileContent = Files.readAllLines(expectedHttpPluginClassloaderModelFile.toPath());

    assertThat("The classloader-model.json file of the mule-http-connector is different from the expected", generatedHttpPluginClassloaderModelFileContent,
            equalTo(expectedHttpPluginClassloaderModelFileContent));


    File generatedSocketsPluginClassloaderModelFile =
            getFile(GENERATED_SOCKETS_PLUGIN_CLASSLOADER_MODEL_FILE);
    List<String> generatedSocketsPluginClassloaderModelFileContent = Files.readAllLines(generatedSocketsPluginClassloaderModelFile.toPath());

    File expectedSocketsPluginClassloaderModelFile =
            getFile(EXPECTED_SOCKETS_PLUGIN_CLASSLOADER_MODEL_FILE);
    List<String> expectedSocketsPluginClassloaderModelFileContent = Files.readAllLines(expectedSocketsPluginClassloaderModelFile.toPath());

    assertThat("The classloader-model.json file of the mule-sockets-connector is different from the expected", generatedSocketsPluginClassloaderModelFileContent,
            equalTo(expectedSocketsPluginClassloaderModelFileContent));

    File expectedStructure = getExpectedStructure("/expected-classloader-model-project");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
            hasSameTreeStructure(expectedStructure, excludes));

    verifier.verifyErrorFreeLog();
  }
}
