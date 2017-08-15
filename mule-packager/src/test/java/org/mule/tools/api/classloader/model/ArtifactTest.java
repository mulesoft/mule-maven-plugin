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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class ArtifactTest {

  private static final String RESOURCE_FULL_PATH = "User/lala/repository/aaa/bbb.jar";
  private static final URI EXPECTED_URI = URI.create("repository/aaa/bbb.jar");
  private Artifact artifact;
  private Artifact newArtifact;
  private File newArtifactFile;

  @Before
  public void setUp() {
    artifact = new Artifact(mock(ArtifactCoordinates.class), URI.create(RESOURCE_FULL_PATH));
    newArtifact = new Artifact(mock(ArtifactCoordinates.class), URI.create(RESOURCE_FULL_PATH));
  }

  @Test
  public void setNewArtifactURIWindowsRelativePathTest() throws URISyntaxException {
    newArtifactFile = new File("repository\\aaa\\bbb.jar");
    artifact.setNewArtifactURI(newArtifact, newArtifactFile);
    assertThat("Relative path is not the expected", newArtifact.getUri(), equalTo(EXPECTED_URI));
  }

  @Test
  public void setNewArtifactURIUnixRelativePathTest() throws URISyntaxException {
    newArtifactFile = new File("repository/aaa/bbb.jar");
    artifact.setNewArtifactURI(newArtifact, newArtifactFile);
    assertThat("Relative path is not the expected", newArtifact.getUri(), equalTo(EXPECTED_URI));
  }
}
