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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.DEFAULT_PACKAGE_EXPORT;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.EXPORTED_PACKAGES;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.EXPORTED_RESOURCES;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;

public class ProcessClassesMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String GOAL = "process-classes";

  public ProcessClassesMojoTest() {
    this.goal = GOAL;
  }

  @Before
  public void before() throws IOException {
    clearResources();
  }

  @Test
  public void testProcessClasses() throws IOException, VerificationException {
    verifier.executeGoal(GOAL);
    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludesCompile));
    verifier.verifyErrorFreeLog();

    final String json =
        new String(Files.readAllBytes(targetFolder.toPath().resolve(META_INF.value()).resolve(MULE_ARTIFACT.value())
            .resolve(MULE_ARTIFACT_JSON)));
    MuleApplicationModel muleApplicationModel = new MuleApplicationModelJsonSerializer().deserialize(json);
    assertThat((List<String>) muleApplicationModel.getClassLoaderModelLoaderDescriptor().getAttributes().get(EXPORTED_PACKAGES),
               containsInAnyOrder(DEFAULT_PACKAGE_EXPORT, "org.mule.apackagehere"));

    assertThat((List<String>) muleApplicationModel.getClassLoaderModelLoaderDescriptor().getAttributes().get(EXPORTED_RESOURCES),
               containsInAnyOrder(".placeholder", "some/path/file.txt"));
  }

  @Test(expected = VerificationException.class)
  public void testFailOnEmptyPolicyProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(EMPTY_POLICY_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.executeGoal(GOAL);
  }

  @Test(expected = VerificationException.class)
  public void testFailOnEmptyDomainProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(EMPTY_DOMAIN_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.executeGoal(GOAL);
  }

  @Test(expected = VerificationException.class)
  public void testFailOnEmptyProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(EMPTY_PROJECT_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(GOAL);
  }

  @Test
  public void testDoNotCheckSemverProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(SEMVER_CHECK, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(GOAL);
  }
}
