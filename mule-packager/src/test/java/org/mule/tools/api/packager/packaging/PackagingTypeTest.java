/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.packaging;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;
import static org.mule.tools.api.packager.structure.FolderNames.*;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PackagingTypeTest {


  private static final String UNKNOWN_CLASSIFIER = "mule-unknown";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private Path projectBasedFolder;

  @Before
  public void setUp() throws IOException {
    temporaryFolder.create();
    projectBasedFolder = temporaryFolder.getRoot().toPath();
  }

  @Test
  public void muleApplicationGetClassifiers() {
    assertThat("The mule application classifiers are not the expected", asList(MULE.getClassifiers()),
               containsInAnyOrder(Classifier.MULE, Classifier.MULE_APPLICATION, Classifier.MULE_APPLICATION_EXAMPLE,
                                  Classifier.MULE_APPLICATION_TEMPLATE));
  }

  @Test
  public void muleDomainGetClassifiers() {
    assertThat("The mule domain classifiers are not the expected", asList(MULE_DOMAIN.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_DOMAIN));
  }

  @Test
  public void muleApplicationFromString() {
    assertThat("Mule application packaging type was not correctly resolved", PackagingType.fromString("mule"),
               equalTo(MULE));
  }

  @Test
  public void muleDomainFromString() {
    assertThat("Mule domain packaging type was not correctly resolved", PackagingType.fromString("mule-domain"),
               equalTo(MULE_DOMAIN));
  }

  @Test
  public void unknownPackagingTypeFromString() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.fromString(UNKNOWN_CLASSIFIER);
  }

  @Test
  public void emptyPackagingTypeFromString() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.fromString("");
  }

  @Test
  public void nullPackagingTypeFromString() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.fromString(null);
  }

  @Test
  public void muleApplicationToString() {
    assertThat("Mule application packaging type toString return value is not the expected",
               MULE.toString(),
               equalTo("mule"));
  }

  @Test
  public void muleDomainToString() {
    assertThat("Mule domain packaging type toString return value is not the expected",
               MULE_DOMAIN.toString(),
               equalTo("mule-domain"));
  }

  @Test
  public void getSourceFolderLocation() throws IOException {
    Path expectedPath = temporaryFolder.newFolder(SRC.value(), MAIN.value(), APP.value()).toPath();
    for (PackagingType type : PackagingType.values()) {
      assertThat("The packaging type does not define the expected source folder location",
                 type.getSourceFolderLocation(projectBasedFolder),
                 equalTo(expectedPath));
    }
  }

  @Test
  public void getTestSourceFolderLocation() throws IOException {
    Path expectedPath = temporaryFolder.newFolder(SRC.value(), TEST.value(), MUNIT.value()).toPath();
    for (PackagingType type : PackagingType.values()) {
      assertThat("The packaging type does not define the expected test source folder location",
                 type.getTestSourceFolderLocation(projectBasedFolder),
                 equalTo(expectedPath));
    }
  }

  @Test
  public void muleApplicationGetSourceFolderLocationNullArgument() {
    expectedException.expect(IllegalArgumentException.class);
    MULE.getSourceFolderLocation(null);
  }

  @Test
  public void muleApplicationGetTestSourceFolderLocationNullArgument() {
    expectedException.expect(IllegalArgumentException.class);
    MULE.getTestSourceFolderLocation(null);
  }
}
