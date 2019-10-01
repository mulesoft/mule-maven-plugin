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

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.junit.Before;

public abstract class AbstractProcessSourcesMojoTest extends MojoTest {

  protected static final String PROCESS_SOURCES = "process-sources";

  public AbstractProcessSourcesMojoTest() {
    this.goal = PROCESS_SOURCES;
  }

  @Before
  public void before() throws IOException {
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
    projectBaseDirectory = builder.createProjectBaseDir(applicationName, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliOption("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliOption("-DskipValidation=true");
    cliOptions.stream().forEach(option -> verifier.addCliOption(option));
    verifier.executeGoal(PROCESS_SOURCES);
  }

  protected String getFileContent(String path) throws IOException {
    File generatedClassloaderModelFile = getFile(path);
    return readFileToString(generatedClassloaderModelFile);
  }

}
