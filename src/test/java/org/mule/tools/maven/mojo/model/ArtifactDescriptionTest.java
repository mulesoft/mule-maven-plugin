/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArtifactDescriptionTest {

  private static final String GROUP_ID = "groupId";
  private static final String ARTIFACT_ID = "artifactId";
  private static final String VERSION = "version";
  private static final String TYPE = "type";
  private static final String COORDINATES_SEPARATOR = ":";
  @Rule
  public ExpectedException exception = ExpectedException.none();
  private ArtifactDescription artifactDescription;

  @Before
  public void before() {
    artifactDescription = new ArtifactDescription();
  }

  @Test
  public void setGroupIdTest() {
    exception.expect(IllegalArgumentException.class);
    artifactDescription.setGroupId(null);
  }

  @Test
  public void setArtifactIdTest() {
    exception.expect(IllegalArgumentException.class);
    artifactDescription.setArtifactId(null);
  }

  @Test
  public void setVersionTest() {
    exception.expect(IllegalArgumentException.class);
    artifactDescription.setVersion(null);
  }

  @Test
  public void setTypeTest() {
    exception.expect(IllegalArgumentException.class);
    artifactDescription.setType(null);
  }

  @Test
  public void createInvalidArtifactDescriptionTest() {
    exception.expect(IllegalArgumentException.class);
    artifactDescription = new ArtifactDescription(null, null, null, null);
  }

  @Test
  public void createArtifactFromNullCoordinatesTest() {
    exception.expect(IllegalArgumentException.class);
    artifactDescription = new ArtifactDescription(null);
  }

  @Test
  public void createArtifactFromInvalidCoordinatesTest() {
    exception.expect(IllegalStateException.class);
    String invalidCoordinates = ":::";
    artifactDescription = new ArtifactDescription(invalidCoordinates);
  }

  @Test
  public void createArtifactFromValidCoordinatesTest() {
    String invalidCoordinates =
        GROUP_ID + COORDINATES_SEPARATOR + ARTIFACT_ID + COORDINATES_SEPARATOR + VERSION + COORDINATES_SEPARATOR + TYPE;
    artifactDescription = new ArtifactDescription(invalidCoordinates);
    assertThat("Group id is not the expected", artifactDescription.getGroupId(), equalTo(GROUP_ID));
    assertThat("Artifact id is not the expected", artifactDescription.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat("Version is not the expected", artifactDescription.getVersion(), equalTo(VERSION));
    assertThat("Type is not the expected", artifactDescription.getType(), equalTo(TYPE));
  }
}
