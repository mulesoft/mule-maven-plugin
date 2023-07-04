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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.api.meta.model.parameter.ParameterizedModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.extension.api.model.construct.ImmutableConstructModel;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.ConfigurationException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AstGeneratorTest extends MavenClientTest {

  @Test
  void generateASTWithExtensionModels() throws Exception {
    final Pair<AstGenerator, ArtifactAst> elements = getElements("mule-config.xml");
    elements.getLeft().validateAST(elements.getRight());

    assertThat(elements.getRight().topLevelComponents().get(0).getModel(ParameterizedModel.class)).isPresent();
    assertThat(elements.getRight().topLevelComponents().get(0).getModel(ParameterizedModel.class).orElse(null))
        .isInstanceOf(ImmutableConstructModel.class);
  }

  @Test
  void throwConfigurationExceptionIfMuleConfigHasErrors() {
    final Pair<AstGenerator, ArtifactAst> elements = getElements("mule-config2.xml");
    assertThatThrownBy(() -> elements.getLeft().validateAST(elements.getRight()))
        .isExactlyInstanceOf(ConfigurationException.class);
  }

  private Pair<AstGenerator, ArtifactAst> getElements(String muleConfiguration) {
    try {
      final Path workingPath = Paths.get("src", "test", "resources", "test-project");
      final Path configsBasePath = workingPath.resolve("src/main/mule");
      final File m2Repo = getM2Repo(getM2Home());
      final MavenClient client =
          getMavenClientInstance(getMavenConfiguration(m2Repo, getUserSettings(m2Repo), getSettingsSecurity(m2Repo)));
      final AstGenerator generator =
          new AstGenerator(client, "4.3.0", Collections.emptySet(), workingPath, null, Collections.emptyList());
      final ArtifactAst artifact =
          generator.generateAST(Collections.singletonList(configsBasePath.resolve(muleConfiguration).toFile().getAbsolutePath()),
                                configsBasePath);
      return Pair.of(generator, artifact);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
