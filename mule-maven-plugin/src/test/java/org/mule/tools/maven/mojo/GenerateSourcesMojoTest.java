/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.packager.sources.ContentGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

class GenerateSourcesMojoTest {

  static class GenerateSourcesMojoImpl extends GenerateSourcesMojo {

    public GenerateSourcesMojoImpl() {
      this.contentGenerator = mock(ContentGenerator.class);
    }
  }

  private final GenerateSourcesMojoImpl mojo = new GenerateSourcesMojoImpl();

  @Test
  void doExecuteTest() throws Exception {
    reset(mojo.getContentGenerator());

    doThrow(new IllegalArgumentException()).when(mojo.getContentGenerator()).createContent();
    assertThatThrownBy(mojo::doExecute).isExactlyInstanceOf(MojoFailureException.class)
        .hasMessageContaining("Fail to generate sources");

    doNothing().when(mojo.getContentGenerator()).createContent();
    mojo.doExecute();
  }

  @Test
  void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder()).isEqualTo("MULE_MAVEN_PLUGIN_GENERATE_SOURCES_PREVIOUS_RUN_PLACEHOLDER");
  }
}
