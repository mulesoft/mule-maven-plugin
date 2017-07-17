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
import static org.mule.tools.maven.FileTreeMatcher.hasSameTreeStructure;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;

import com.google.gson.Gson;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.tools.api.classloader.model.ClassLoaderModel;

public class ProcessSourcesMojoTest extends MojoTest {

  private static final String PROCESS_SOURCES = "process-sources";
  private static final String GENERATED_CLASSLOADER_MODEL_FILE =
      "/integrationTests/empty-classloader-model-project/target/META-INF/mule-artifact/classloader-model.json";
  private static final String EXPECTED_CLASSLOADER_MODEL_FILE =
      "/integrationTests/expected-files/expected-classloader-model.json";

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
    verifier.verifyErrorFreeLog();
  }
}
