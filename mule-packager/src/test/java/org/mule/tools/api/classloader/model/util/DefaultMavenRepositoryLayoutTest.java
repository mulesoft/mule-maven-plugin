/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model.util;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mule.tools.api.classloader.model.util.DefaultMavenRepositoryLayoutUtils.getFormattedFileName;
import static org.mule.tools.api.classloader.model.util.DefaultMavenRepositoryLayoutUtils.getFormattedOutputDirectory;
import static org.mule.tools.api.classloader.model.util.DefaultMavenRepositoryLayoutUtils.getNormalizedVersion;

public class DefaultMavenRepositoryLayoutTest {

  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String SNAPSHOT_VERSION = "SNAPSHOT";
  private static final String SCOPE = "compile";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String TIMESTAMP = "20100101.101010-1";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String POSFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + POSFIX_GROUP_ID;
  private static final String OUTPUT_DIRECTORY =
      PREFIX_GROUP_ID + File.separator + POSFIX_GROUP_ID + File.separator + ARTIFACT_ID + File.separator + VERSION;
  private ArtifactHandler handler;
  private Artifact artifact;

  @Rule
  public TemporaryFolder outputFolder = new TemporaryFolder();

  @Before
  public void before() throws IOException {
    handler = new DefaultArtifactHandler(TYPE);
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, CLASSIFIER, handler);
  }

  @Test
  public void getNormalizedVersionFromBaseVersionTest() {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION + "-" + TIMESTAMP, SCOPE, TYPE, CLASSIFIER, handler);
    String actual = getNormalizedVersion(artifact);
    assertThat("The normalized version is different from the expected", actual, equalTo(VERSION + "-" + SNAPSHOT_VERSION));
  }


  @Test
  public void getNormalizedVersionFromVersionTest() {
    String expected = getNormalizedVersion(artifact);
    assertThat("The normalized version is different from the expected", expected, equalTo(VERSION));
  }

  @Test
  public void getFormattedOutputDirectoryTest() {
    File actual = getFormattedOutputDirectory(outputFolder.getRoot(), artifact);
    File expected = new File(outputFolder.getRoot(),
                             OUTPUT_DIRECTORY);
    assertThat("Actual formatted output directory is not the expected", actual.getAbsolutePath(),
               equalTo(expected.getAbsolutePath()));
  }

  @Test
  public void getFormattedFileNameTest() {
    String actual = getFormattedFileName(artifact);
    String expected = ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void getFormattedFileNameWithoutClassifierTest() {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, null, handler);
    String actual = getFormattedFileName(artifact);
    String expected = ARTIFACT_ID + "-" + VERSION + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

  @Test
  public void getFormattedFileNameWithoutHandlerTest() {
    artifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, SCOPE, TYPE, CLASSIFIER, null);
    String actual = getFormattedFileName(artifact);
    String expected = ARTIFACT_ID + "-" + VERSION + "-" + CLASSIFIER + "." + TYPE;
    assertThat("Formatted file name is different from the expected", actual, equalTo(expected));
  }

}
