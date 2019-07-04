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

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationGAVModelTest {

  private static final String GROUP_ID = "group-id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "version";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void modelCreationIsCorrect() {
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, VERSION);
    assertEquals(GROUP_ID, appGAVModel.getGroupId());
    assertEquals(ARTIFACT_ID, appGAVModel.getArtifactId());
    assertEquals(VERSION, appGAVModel.getVersion());
  }

  @Test
  public void cannotCreateModelWithoutGroupId() throws NullPointerException {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("groupId cannot be null");
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(null, ARTIFACT_ID, VERSION);
  }

  @Test
  public void cannotCreateModelWithoutArtifactId() throws NullPointerException {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("artifactId cannot be null");
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, null, VERSION);
  }

  @Test
  public void cannotCreateModelWithoutVersion() throws NullPointerException {
    expectedException.expect(NullPointerException.class);
    expectedException.expectMessage("version cannot be null");
    ApplicationGAVModel appGAVModel = new ApplicationGAVModel(GROUP_ID, ARTIFACT_ID, null);
  }
}
