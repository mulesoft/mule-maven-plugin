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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


public class PackagingTypeTest {

  public static final String TEST_JAR_CLASSIFIER = "-" + Classifier.TEST_JAR.toString();
  public static final String LIGHT_PACKAGE_CLASSIFIER = "-" + Classifier.LIGHT_PACKAGE.toString();

  private static final String UNKNOWN_CLASSIFIER = "mule-unknown";

  @TempDir
  public Path temporaryFolder;

  private Path projectBasedFolder;

  @BeforeEach
  public void setUp() throws IOException {
    //    temporaryFolder.toFile();
    projectBasedFolder = temporaryFolder.toAbsolutePath();
  }

  @Test
  public void muleApplicationGetClassifiers() {
    assertThat(asList(PackagingType.MULE_APPLICATION.getClassifiers()))
        .describedAs("The mule application classifiers are not the expected").containsAnyOf(Classifier.MULE_APPLICATION,
                                                                                            MULE_PLUGIN,
                                                                                            MULE_APPLICATION_EXAMPLE,
                                                                                            MULE_APPLICATION_TEMPLATE);
  }

  @Test
  public void muleDomainGetClassifiers() {
    assertThat(asList(MULE_DOMAIN.getClassifiers())).describedAs("The mule domain classifiers are not the expected")
        .containsAnyOf(Classifier.MULE_DOMAIN);
  }


  @Test
  public void mulePolicyGetClassifiers() {
    assertThat(asList(MULE_POLICY.getClassifiers())).describedAs("The mule policy classifiers are not the expected")
        .containsAnyOf(Classifier.MULE_POLICY);
  }

  @Test
  public void muleApplicationFromString() {
    assertThat(PackagingType.fromString("mule-application"))
        .describedAs("Mule application packaging type was not correctly resolved").isEqualTo(PackagingType.MULE_APPLICATION);
  }

  @Test
  public void muleDomainFromString() {
    assertThat(PackagingType.fromString("mule-domain")).describedAs("Mule domain packaging type was not correctly resolved")
        .isEqualTo(MULE_DOMAIN);
  }

  @Test
  public void mulePolicyFromString() {
    assertThat(PackagingType.fromString("mule-policy")).describedAs("Mule policy packaging type was not correctly resolved")
        .isEqualTo(MULE_POLICY);
  }

  @Test
  public void unknownPackagingTypeFromString() {
    assertThrows(IllegalArgumentException.class, () -> PackagingType.fromString(UNKNOWN_CLASSIFIER));
  }

  @Test
  public void emptyPackagingTypeFromString() {
    assertThrows(IllegalArgumentException.class, () -> PackagingType.fromString(""));
  }

  @Test
  public void nullPackagingTypeFromString() {
    assertThrows(IllegalArgumentException.class, () -> PackagingType.fromString(null));
  }

  @Test
  public void muleApplicationToString() {
    assertThat(PackagingType.MULE_APPLICATION.toString())
        .describedAs("Mule application packaging type toString return value is not the expected").isEqualTo("mule-application");
  }

  @Test
  public void muleDomainToString() {
    assertThat(MULE_DOMAIN.toString()).describedAs("Mule domain packaging type toString return value is not the expected")
        .isEqualTo("mule-domain");
  }

  @Test
  public void mulePolicyToString() {
    assertThat(MULE_POLICY.toString()).describedAs("Mule policy packaging type toString return value is not the expected")
        .isEqualTo("mule-policy");
  }

  @Test
  public void getSourceFolderLocation() throws IOException {
    Path expectedPath = temporaryFolder.resolve(SRC.value() + "/" + MAIN.value() + "/" + MULE.value());
    stream(PackagingType.values())
        .forEach(packagingType -> assertThat(packagingType.getSourceFolderLocation(projectBasedFolder))
            .describedAs("The packaging type does not define the expected source folder location").isEqualTo(expectedPath));
  }

  @Test
  public void getTestSourceFolderLocation() throws IOException {
    //    Path expectedPath = temporaryFolder.newFolder(SRC.value(), TEST.value(), MUNIT.value()).toPath();
    //    stream(PackagingType.values())
    //        .forEach(packagingType -> assertThat("The packaging type does not define the expected test source folder location",
    //                                             packagingType.getTestSourceFolderLocation(projectBasedFolder),
    //                                             equalTo(expectedPath)));
  }

  @Test
  public void muleApplicationGetSourceFolderLocationNullArgument() {
    assertThrows(IllegalArgumentException.class, () -> PackagingType.MULE_APPLICATION.getSourceFolderLocation(null));
  }

  @Test
  public void muleApplicationGetTestSourceFolderLocationNullArgument() {
    assertThrows(IllegalArgumentException.class, () -> PackagingType.MULE_APPLICATION.getTestSourceFolderLocation(null));
  }

  @Test
  public void muleApplicationResolveClassifierHeavyWeight() {
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application");
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application" + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-template");
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-template" + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-example");
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-example" + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application");
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application" + TEST_JAR_CLASSIFIER);

  }

  @Test
  public void muleApplicationResolveClassifierTestPackage() {
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application" + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-template" + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-example" + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application" + TEST_JAR_CLASSIFIER);
  }

  @Test
  public void muleDomainResolveClassifierHeavyWeight() {
    assertThat(MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-domain");
    assertThat(MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-domain");
  }

  @Test
  public void muleDomainResolveClassifierTestPackage() {
    assertThat(MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-domain" + TEST_JAR_CLASSIFIER);
    assertThat(MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-domain" + TEST_JAR_CLASSIFIER);
  }

  @Test
  public void mulePolicyResolveClassifierHeavyWeight() {
    assertThat(MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-policy");
    assertThat(MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, false, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-policy");
  }

  @Test
  public void mulePolicyResolveClassifierTestPackage() {
    assertThat(MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-policy" + TEST_JAR_CLASSIFIER);
    assertThat(MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, false, true))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-policy" + TEST_JAR_CLASSIFIER);
  }

  @Test
  public void muleApplicationResolveClassifierLightWeight() {
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), true, false))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-application-template" + LIGHT_PACKAGE_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application-example" + LIGHT_PACKAGE_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER);
  }

  @Test
  public void muleApplicationResolveClassifierLightWeightTestPackage() {
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(Classifier.MULE_APPLICATION.name(), true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_TEMPLATE.name(), true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-application-template" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(MULE_APPLICATION_EXAMPLE.name(), true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-application-example" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
    assertThat(PackagingType.MULE_APPLICATION.resolveClassifier(UNKNOWN_CLASSIFIER, true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-application" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
  }

  @Test
  public void muleDomainResolveClassifierLightWeight() {
    assertThat(MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER);
    assertThat(MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER);
  }

  @Test
  public void muleDomainResolveClassifierLightWeightTestPackage() {
    assertThat(MULE_DOMAIN.resolveClassifier(Classifier.MULE_DOMAIN.name(), true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
    assertThat(MULE_DOMAIN.resolveClassifier(UNKNOWN_CLASSIFIER, true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-domain" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
  }

  @Test
  public void mulePolicyResolveClassifierLightWeight() {
    assertThat(MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER);
    assertThat(MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, true, false))
        .describedAs("Classifier resolution is not as expected").isEqualTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER);
  }

  @Test
  public void mulePolicyResolveClassifierLightWeightTestPackage() {
    assertThat(MULE_POLICY.resolveClassifier(Classifier.MULE_POLICY.name(), true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
    assertThat(MULE_POLICY.resolveClassifier(UNKNOWN_CLASSIFIER, true, true))
        .describedAs("Classifier resolution is not as expected")
        .isEqualTo("mule-policy" + LIGHT_PACKAGE_CLASSIFIER + TEST_JAR_CLASSIFIER);
  }
}
