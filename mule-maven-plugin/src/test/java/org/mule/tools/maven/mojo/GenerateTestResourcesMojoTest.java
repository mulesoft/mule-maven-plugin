/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateTestResourcesMojoTest extends AbstractMuleMojoTest {

  private GenerateTestResourcesMojo mojo;

  @BeforeEach
  void before() {
    mojo = new GenerateTestResourcesMojo();
  }

  @Test
  void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder()).isEqualTo("MULE_MAVEN_PLUGIN_GENERATE_TEST_RESOURCES_PREVIOUS_RUN_PLACEHOLDER");
  }
}
