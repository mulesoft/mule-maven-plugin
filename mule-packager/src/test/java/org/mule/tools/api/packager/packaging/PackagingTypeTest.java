/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.packaging;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class PackagingTypeTest {

  private static final String MULE_SOURCE_FOLDER_RELATIVE_PATH = "src/main/mule";
  private static final String MUNIT_SOURCE_FOLDER_RELATIVE_PATH = "src/test/munit";
  private static final String UNKNOWN_CLASSIFIER = "mule-unknown";
  private Path projectBasedFolder;

  @Before
  public void setUp() throws IOException {
    temporaryFolder.create();
    projectBasedFolder = temporaryFolder.getRoot().toPath();
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void muleApplicationGetClassifiersTest() {
    assertThat("The mule application classifiers are not the expected",
               Arrays.asList(PackagingType.MULE_APPLICATION.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_APPLICATION,
                                  Classifier.MULE_APPLICATION_EXAMPLE,
                                  Classifier.MULE_APPLICATION_TEMPLATE));
  }

  @Test
  public void muleDomainGetClassifiersTest() {
    assertThat("The mule domain classifiers are not the expected",
               Arrays.asList(PackagingType.MULE_DOMAIN.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_DOMAIN));
  }


  @Test
  public void mulePolicyGetClassifiersTest() {
    assertThat("The mule policy classifiers are not the expected",
               Arrays.asList(PackagingType.MULE_POLICY.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_POLICY));
  }

  @Test
  public void muleApplicationFromStringTest() {
    assertThat("Mule application packaging type was not correctly resolved",
               PackagingType.fromString("mule-application"),
               equalTo(PackagingType.MULE_APPLICATION));
  }

  @Test
  public void muleDomainFromStringTest() {
    assertThat("Mule domain packaging type was not correctly resolved",
               PackagingType.fromString("mule-domain"),
               equalTo(PackagingType.MULE_DOMAIN));
  }

  @Test
  public void mulePolicyFromStringTest() {
    assertThat("Mule policy packaging type was not correctly resolved",
               PackagingType.fromString("mule-policy"),
               equalTo(PackagingType.MULE_POLICY));
  }

  @Test
  public void unknownPackagingTypeFromStringTest() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.fromString(UNKNOWN_CLASSIFIER);
  }

  @Test
  public void emptyPackagingTypeFromStringTest() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.fromString("");
  }

  @Test
  public void nullPackagingTypeFromStringTest() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.fromString(null);
  }

  @Test
  public void muleApplicationToStringTest() {
    assertThat("Mule application packaging type toString return value is not the expected",
               PackagingType.MULE_APPLICATION.toString(),
               equalTo("mule-application"));
  }

  @Test
  public void muleDomainToStringTest() {
    assertThat("Mule domain packaging type toString return value is not the expected",
               PackagingType.MULE_DOMAIN.toString(),
               equalTo("mule-domain"));
  }

  @Test
  public void mulePolicyToStringTest() {
    assertThat("Mule policy packaging type toString return value is not the expected",
               PackagingType.MULE_POLICY.toString(),
               equalTo("mule-policy"));
  }

  @Test
  public void getSourceFolderLocationTest() {
    Path expectedPath = temporaryFolder.newFolder(MULE_SOURCE_FOLDER_RELATIVE_PATH).toPath();
    Arrays.stream(PackagingType.values())
        .forEach(packagingType -> assertThat("The packaging type does not define the expected source folder location",
                                             packagingType.getSourceFolderLocation(projectBasedFolder),
                                             equalTo(expectedPath)));
  }

  @Test
  public void getTestSourceFolderLocationTest() {
    Path expectedPath = temporaryFolder.newFolder(MUNIT_SOURCE_FOLDER_RELATIVE_PATH).toPath();
    Arrays.stream(PackagingType.values())
        .forEach(packagingType -> assertThat("The packaging type does not define the expected test source folder location",
                                             packagingType.getTestSourceFolderLocation(projectBasedFolder),
                                             equalTo(expectedPath)));
  }

  @Test
  public void muleApplicationGetSourceFolderLocationNullArgumentTest() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.MULE_APPLICATION.getSourceFolderLocation(null);
  }

  @Test
  public void muleApplicationGetTestSourceFolderLocationNullArgumentTest() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.MULE_APPLICATION.getTestSourceFolderLocation(null);
  }

  @Test
  public void muleApplicationResolveClassifierHeavyWeightTest() {
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false),
               equalTo("mule-application"));
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION_TEMPLATE.name(), false),
               equalTo("mule-application-template"));
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION_EXAMPLE.name(), false),
               equalTo("mule-application-example"));
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false), equalTo("mule-application"));

  }

  @Test
  public void muleDomainResolveClassifierHeavyWeightTest() {
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), false), equalTo("mule-domain"));
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, false), equalTo("mule-domain"));
  }

  @Test
  public void mulePolicyResolveClassifierHeavyWeightTest() {
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), false), equalTo("mule-policy"));
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, false), equalTo("mule-policy"));
  }

  @Test
  public void muleApplicationResolveClassifierLightWeightTest() {
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), true),
               equalTo("mule-application-light-package"));
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION_TEMPLATE.name(), true),
               equalTo("mule-application-template-light-package"));
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION_EXAMPLE.name(), true),
               equalTo("mule-application-example-light-package"));
    assertThat("Mule application classifier resolution is not the expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, true),
               equalTo("mule-application-light-package"));

  }

  @Test
  public void muleDomainResolveClassifierLightWeightTest() {
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), true),
               equalTo("mule-domain-light-package"));
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, true), equalTo("mule-domain-light-package"));
  }

  @Test
  public void mulePolicyResolveClassifierLightWeightTest() {
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), true),
               equalTo("mule-policy-light-package"));
    assertThat("Mule domain classifier resolution is not the expected",
               PackagingType.MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, true), equalTo("mule-policy-light-package"));
  }
}
