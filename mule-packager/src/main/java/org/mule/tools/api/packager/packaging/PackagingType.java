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

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.structure.FolderNames.MAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.MUNIT;
import static org.mule.tools.api.packager.structure.FolderNames.SRC;
import static org.mule.tools.api.packager.structure.FolderNames.TEST;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public enum PackagingType {
  MULE_APPLICATION(Classifier.MULE_APPLICATION) {

    @Override
    public Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_APPLICATION,
          Classifier.MULE_PLUGIN,
          Classifier.MULE_APPLICATION_EXAMPLE,
          Classifier.MULE_APPLICATION_TEMPLATE};
    }
  },

  MULE_DOMAIN(Classifier.MULE_DOMAIN) {

    @Override
    public Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_DOMAIN};
    }
  },

  MULE_POLICY(Classifier.MULE_POLICY) {

    @Override
    public Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_POLICY};
    }
  },

  MULE_DOMAIN_BUNDLE(Classifier.MULE_DOMAIN_BUNDLE) {

    @Override
    public Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_DOMAIN_BUNDLE};
    }
  };


  protected Classifier defaultClassifier;

  PackagingType(Classifier defaultClassifier) {
    this.defaultClassifier = defaultClassifier;
  }

  public abstract Classifier[] getClassifiers();

  public String resolveClassifier(String classifierName, boolean lightweight, boolean testPackage) {
    String baseClassifier = Arrays.stream(getClassifiers())
        .filter(allowedClassifier -> allowedClassifier.equals(classifierName))
        .findFirst()
        .orElse(defaultClassifier).toString();

    if (lightweight) {
      baseClassifier += "-" + Classifier.LIGHT_PACKAGE.toString();
    }

    if (testPackage) {
      baseClassifier += "-" + Classifier.TEST_JAR;
    }

    return baseClassifier;
  }

  public static PackagingType fromString(String name) {
    checkArgument(name != null, "Packaging type name should not be null");
    String packagingName = LOWER_HYPHEN.to(UPPER_UNDERSCORE, name);
    return valueOf(packagingName);
  }

  public boolean equals(String name) {
    return name == null ? false : fromString(name).equals(this);
  }

  @Override
  public String toString() {
    return UPPER_UNDERSCORE.to(LOWER_HYPHEN, this.name());
  }

  public Path getSourceFolderLocation(Path projectBaseFolder) {
    return Paths.get(mainFolder(projectBaseFolder).getAbsolutePath(), getSourceFolderName());
  }

  public Path getTestSourceFolderLocation(Path projectBaseFolder) {
    return testFolder(projectBaseFolder).toPath().resolve(getTestFolderName());
  }

  public String getTestFolderName() {
    return MUNIT.value();
  }

  public String getSourceFolderName() {
    return MULE.value();
  }

  private File mainFolder(Path projectBaseFolder) {
    return Paths.get(srcFolder(projectBaseFolder).getAbsolutePath(), MAIN.value()).toFile();
  }

  private File testFolder(Path projectBaseFolder) {
    return Paths.get(srcFolder(projectBaseFolder).getAbsolutePath(), TEST.value()).toFile();
  }

  private File srcFolder(Path projectBaseFolder) {
    checkArgument(projectBaseFolder != null, "Project base folder should not be null");
    return Paths.get(projectBaseFolder.toFile().getAbsolutePath(), SRC.value()).toFile();
  }
}
