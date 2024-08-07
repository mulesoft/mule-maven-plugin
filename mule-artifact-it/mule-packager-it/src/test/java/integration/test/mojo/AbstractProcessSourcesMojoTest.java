/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import integration.ProjectFactory;
import org.apache.maven.shared.verifier.VerificationException;
import org.junit.jupiter.api.BeforeEach;

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

abstract class AbstractProcessSourcesMojoTest extends MojoTest {

  protected static final String PROCESS_SOURCES = "process-sources";

  public AbstractProcessSourcesMojoTest() {
    this.goal = PROCESS_SOURCES;
  }

  @BeforeEach
  void before() throws IOException {
    clearResources();
  }

  protected void checkGeneratedRepository(String type) throws IOException {
    File emptySubmoduleRepository = getFile("/multi-module-application/empty-" + type + "/target/repository");
    File expectedSubmoduleRepository = getExpectedStructure("/expected-empty-" + type + "-multimodule-repository");
    assertThat("Repository has not the expected structure", emptySubmoduleRepository,
               hasSameTreeStructure(expectedSubmoduleRepository, new String[] {}));
  }

  protected void processSourcesOnProject(String applicationName) throws IOException, VerificationException {
    processSourcesOnProject(applicationName, emptyList());
  }

  protected void processSourcesOnProject(String applicationName, List<String> cliOptions)
      throws IOException, VerificationException {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(applicationName, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-DskipValidation=true");
    cliOptions.forEach(verifier::addCliArgument);
    verifier.addCliArgument(PROCESS_SOURCES);
    verifier.execute();
  }

  protected String getFileContent(String path) throws IOException {
    File generatedClassloaderModelFile = getFile(path);
    return readFileToString(generatedClassloaderModelFile, Charset.defaultCharset());
  }

}
