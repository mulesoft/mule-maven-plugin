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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ClassLoaderModelTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";

  private ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void equals() {
    assertThat(new ClassLoaderModel("1.0", artifactCoordinates).equals(new ClassLoaderModel("2.0", artifactCoordinates)),
               is(true));
  }

  @Test
  public void checkNullVersionTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(null, artifactCoordinates);
  }

  @Test
  public void checkNullArtifactCoordinatesTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(VERSION, null);
  }

  @Test
  public void checkNullArgumentsTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(null, null);
  }
}
