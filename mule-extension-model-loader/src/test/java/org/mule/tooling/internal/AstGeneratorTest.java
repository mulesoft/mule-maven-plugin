/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.nio.file.Paths;

import org.apache.maven.model.Dependency;
import org.junit.Test;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.runtime.ast.api.ArtifactAst;
import static com.google.common.io.Files.createTempDir;
import org.mule.tooling.api.ExtensionModelLoader;
import org.mule.tooling.internal.ExtensionModelLoaderTest;
import org.mule.tooling.internal.DefaultExtensionModelLoader;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.ConfigurationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.mule.runtime.extension.api.model.construct.ImmutableConstructModel;

import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;

public class AstGeneratorTest extends MavenClientTest {

  @Test
  public void generateASTWithExtensionModels() throws Exception {
    final File m2Repo = getM2Repo(getM2Home());
    MavenClient client = getMavenClientInstance(
                                                getMavenConfiguration(m2Repo, Optional.ofNullable(getUserSettings(m2Repo)),
                                                                      Optional.ofNullable(getSettingsSecurity(m2Repo))));
    Path workingPath = Paths.get("src", "test", "resources", "test-project");
    List<Dependency> dependencies = new ArrayList<Dependency>();
    AstGenerator generator = new AstGenerator(client, "4.3.0", dependencies, workingPath, null);
    Path configsBasePath = workingPath.resolve("src/main/mule");
    ArtifactAst artifact =
        generator.generateAST(Arrays.asList("/" + configsBasePath.resolve("mule-config.xml").toFile().getAbsolutePath()),
                              configsBasePath);
    generator.validateAST(artifact);
    String absolutePath = workingPath.toFile().getAbsolutePath();
    assertThat(artifact.topLevelComponents().get(0).getModel(ParameterizedModel.class).isPresent(), is(true));
    assertThat(artifact.topLevelComponents().get(0).getModel(ParameterizedModel.class).get().getClass(),
               equalTo(ImmutableConstructModel.class));
  }

  @Test(expected = ConfigurationException.class)
  public void throwConfigurationExceptionIfMuleConfigHasErrors() throws Exception {
    final File m2Repo = getM2Repo(getM2Home());
    MavenClient client = getMavenClientInstance(
                                                getMavenConfiguration(m2Repo, Optional.ofNullable(getUserSettings(m2Repo)),
                                                                      Optional.ofNullable(getSettingsSecurity(m2Repo))));
    Path workingPath = Paths.get("src", "test", "resources", "test-project");
    List<Dependency> dependencies = new ArrayList<Dependency>();
    AstGenerator generator = new AstGenerator(client, "4.3.0", dependencies, workingPath, null);
    Path configsBasePath = workingPath.resolve("src/main/mule");
    ArtifactAst artifact =
        generator.generateAST(Arrays.asList("/" + configsBasePath.resolve("mule-config2.xml").toFile().getAbsolutePath()),
                              configsBasePath);
    generator.validateAST(artifact);
  }


}
