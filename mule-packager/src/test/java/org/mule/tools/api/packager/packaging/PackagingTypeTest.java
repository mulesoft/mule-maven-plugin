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

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_EXAMPLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_TEMPLATE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_PLUGIN;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_POLICY;
import static org.mule.tools.api.packager.structure.FolderNames.MAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.SRC;
import static org.mule.tools.api.packager.structure.FolderNames.TEST;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PackagingTypeTest {

  public static final String TEST_JAR_CLASSIFIER = "-" + Classifier.TEST_JAR.toString();
  public static final String LIGHT_PACKAGE_CLASSIFIER = "-" + Classifier.LIGHT_PACKAGE.toString();

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
    assertThat("The mule application classifiers are not the expected", asList(PackagingType.MULE_APPLICATION.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_APPLICATION,
                                  MULE_PLUGIN,
                                  MULE_APPLICATION_EXAMPLE,
                                  MULE_APPLICATION_TEMPLATE));
  }

  @Test
  public void muleDomainGetClassifiers() {
    assertThat("The mule domain classifiers are not the expected", asList(MULE_DOMAIN.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_DOMAIN));
  }


  @Test
  public void mulePolicyGetClassifiers() {
    assertThat("The mule policy classifiers are not the expected", asList(MULE_POLICY.getClassifiers()),
               containsInAnyOrder(Classifier.MULE_POLICY));
  }

  @Test
  public void muleApplicationFromString() {
    assertThat("Mule application packaging type was not correctly resolved", PackagingType.fromString("mule-application"),
               equalTo(PackagingType.MULE_APPLICATION));
  }

  @Test
  public void muleDomainFromString() {
    assertThat("Mule domain packaging type was not correctly resolved", PackagingType.fromString("mule-domain"),
               equalTo(MULE_DOMAIN));
  }

  @Test
  public void mulePolicyFromString() {
    assertThat("Mule policy packaging type was not correctly resolved", PackagingType.fromString("mule-policy"),
               equalTo(MULE_POLICY));
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
               PackagingType.MULE_APPLICATION.toString(),
               equalTo("mule-application"));
  }

  @Test
  public void muleDomainToString() {
    assertThat("Mule domain packaging type toString return value is not the expected",
               MULE_DOMAIN.toString(),
               equalTo("mule-domain"));
  }

  @Test
  public void mulePolicyToString() {
    assertThat("Mule policy packaging type toString return value is not the expected",
               MULE_POLICY.toString(),
               equalTo("mule-policy"));
  }

  @Test
  public void getSourceFolderLocation() throws IOException {
    Path expectedPath = temporaryFolder.newFolder(SRC.value(), MAIN.value(), MULE.value()).toPath();
    stream(PackagingType.values())
        .forEach(packagingType -> assertThat("The packaging type does not define the expected source folder location",
                                             packagingType.getSourceFolderLocation(projectBasedFolder),
                                             equalTo(expectedPath)));
  }

  @Test
  public void getTestSourceFolderLocation() throws IOException {
    Path expectedPath = temporaryFolder.newFolder(SRC.value(), TEST.value(), MUNIT.value()).toPath();
    stream(PackagingType.values())
        .forEach(packagingType -> assertThat("The packaging type does not define the expected test source folder location",
                                             packagingType.getTestSourceFolderLocation(projectBasedFolder),
                                             equalTo(expectedPath)));
  }

  @Test
  public void muleApplicationGetSourceFolderLocationNullArgument() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.MULE_APPLICATION.getSourceFolderLocation(null);
  }

  @Test
  public void muleApplicationGetTestSourceFolderLocationNullArgument() {
    expectedException.expect(IllegalArgumentException.class);
    PackagingType.MULE_APPLICATION.getTestSourceFolderLocation(null);
  }

  @Test
  public void muleApplicationResolveClassifierHeavyWeight() {
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false, false),
               equalTo("mule-application"));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false, true),
               equalTo("mule-application" + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), false, false),
               equalTo("mule-application-template"));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), false, true),
               equalTo("mule-application-template" + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), false, false),
               equalTo("mule-application-example"));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), false, true),
               equalTo("mule-application-example" + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false, false), equalTo("mule-application"));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false, true), equalTo(
                                                                                                          "mule-application"
                                                                                                              + TEST_JAR_CLASSIFIER));

  }

  @Test
  public void muleApplicationResolveClassifierTestPackage() {
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false, true),
               equalTo("mule-application" + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), false, true),
               equalTo("mule-application-template" + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), false, true),
               equalTo("mule-application-example" + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false, true), equalTo(
                                                                                                          "mule-application"
                                                                                                              + TEST_JAR_CLASSIFIER));
  }

  @Test
  public void muleDomainResolveClassifierHeavyWeight() {
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), false, false), equalTo("mule-domain"));
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, false, false), equalTo("mule-domain"));
  }

  @Test
  public void muleDomainResolveClassifierTestPackage() {
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), false, true), equalTo(
                                                                                                  "mule-domain"
                                                                                                      + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, false, true), equalTo("mule-domain" + TEST_JAR_CLASSIFIER));
  }

  @Test
  public void mulePolicyResolveClassifierHeavyWeight() {
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), false, false), equalTo("mule-policy"));
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, false, false), equalTo("mule-policy"));
  }

  @Test
  public void mulePolicyResolveClassifierTestPackage() {
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), false, true), equalTo(
                                                                                                  "mule-policy"
                                                                                                      + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, false, true), equalTo("mule-policy" + TEST_JAR_CLASSIFIER));
  }

  @Test
  public void muleApplicationResolveClassifierLightWeight() {
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), true, false),
               equalTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), true, false),
               equalTo("mule-application-template" + LIGHT_PACKAGE_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), true, false),
               equalTo("mule-application-example" + LIGHT_PACKAGE_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, true, false),
               equalTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER));
  }

  @Test
  public void muleApplicationResolveClassifierLightWeightTestPackage() {
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), true, true),
               equalTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), true, true),
               equalTo("mule-application-template" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), true, true),
               equalTo("mule-application-example" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, true, true),
               equalTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
  }

  @Test
  public void muleDomainResolveClassifierLightWeight() {
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), true, false),
               equalTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, true, false),
               equalTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER));
  }

  @Test
  public void muleDomainResolveClassifierLightWeightTestPackage() {
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), true, true),
               equalTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, true, true),
               equalTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
  }

  @Test
  public void mulePolicyResolveClassifierLightWeight() {
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), true, false),
               equalTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, true, false),
               equalTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER));
  }

  @Test
  public void mulePolicyResolveClassifierLightWeightTestPackage() {
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), true, true),
               equalTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
    assertThat("Classifier resolution is not as expected",
               MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, true, true),
               equalTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER));
  }
}
