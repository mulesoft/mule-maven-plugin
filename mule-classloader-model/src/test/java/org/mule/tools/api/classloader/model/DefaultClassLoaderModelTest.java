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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultClassLoaderModelTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final ArtifactCoordinates ARTIFACT_COORDINATES = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);

  @DisplayName("DefaultClassLoaderModel constructor [checking null version]")
  @Test
  public void checkNullVersionTest() {
    Throwable thrown =
        assertThrows(IllegalArgumentException.class, () -> new DefaultClassLoaderModel(null, ARTIFACT_COORDINATES));

    assertThat(thrown.getMessage(), is("Version cannot be null"));
  }

  @DisplayName("DefaultClassLoaderModel constructor [checking null artifact coordinates]")
  @Test
  public void checkNullArtifactCoordinatesTest() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new DefaultClassLoaderModel(VERSION, null));

    assertThat(thrown.getMessage(), is("Artifact coordinates cannot be null"));
  }

  @DisplayName("DefaultClassLoaderModel constructor [checking null arguments]")
  @Test
  public void checkNullArgumentsTest() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new DefaultClassLoaderModel(null, null));

    assertThat(thrown.getMessage(), is("Artifact coordinates cannot be null"));
  }
}
