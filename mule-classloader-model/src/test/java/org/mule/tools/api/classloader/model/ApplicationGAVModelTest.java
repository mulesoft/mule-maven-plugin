/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationGAVModelTest {

  private static final String GROUP_ID = "group-id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "version";

  @Test
  void modelCreationIsCorrect() {
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);
    assertThat(GROUP_ID).isEqualTo(appGAVModel.getGroupId());
    assertThat(ARTIFACT_ID).isEqualTo(appGAVModel.getArtifactId());
    assertThat(VERSION).isEqualTo(appGAVModel.getVersion());
  }

  @Test
  void cannotCreateModelWithoutGroupId() throws NullPointerException {
    assertThatThrownBy(() -> new ApplicationGAVModel(null, ARTIFACT_ID, VERSION))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("groupId cannot be null");
  }

  @Test
  void cannotCreateModelWithoutArtifactId() throws NullPointerException {
    assertThatThrownBy(() -> new ApplicationGAVModel(GROUP_ID, null, VERSION))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("artifactId cannot be null");
  }

  @Test
  void cannotCreateModelWithoutVersion() throws NullPointerException {
    assertThatThrownBy(() -> new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, null))
        .isExactlyInstanceOf(NullPointerException.class)
        .hasMessage("version cannot be null");
  }
}
