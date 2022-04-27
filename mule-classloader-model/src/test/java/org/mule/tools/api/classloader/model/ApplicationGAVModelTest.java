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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApplicationGAVModelTest {

  private static final String GROUP_ID = "group-id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "version";

  @DisplayName("ApplicationGAVModel constructor [OK]")
  @Test
  public void modelCreationIsCorrect() {
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);

    assertThat(GROUP_ID, equalTo(appGAVModel.getGroupId()));
    assertThat(ARTIFACT_ID, equalTo(appGAVModel.getArtifactId()));
    assertThat(VERSION, equalTo(appGAVModel.getVersion()));
  }

  @DisplayName("Cannot create model without groupId")
  @Test
  public void cannotCreateModelWithoutGroupId() {
    Throwable thrown = assertThrows(NullPointerException.class, () -> new ApplicationGAVModel(null, ARTIFACT_ID, VERSION));

    assertThat(thrown.getMessage(), is("groupId cannot be null"));
  }

  @DisplayName("Cannot create model without artifactId")
  @Test
  public void cannotCreateModelWithoutArtifactId() {
    Throwable thrown = assertThrows(NullPointerException.class, () -> new ApplicationGAVModel(GROUP_ID, null, VERSION));

    assertThat(thrown.getMessage(), is("artifactId cannot be null"));
  }

  @DisplayName("Cannot create model without version")
  @Test
  public void cannotCreateModelWithoutVersion() {
    Throwable thrown = assertThrows(NullPointerException.class, () -> new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, null));

    assertThat(thrown.getMessage(), is("version cannot be null"));
  }
}
